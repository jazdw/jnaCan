/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan;

import static net.jazdw.jnacan.c.CLibrary.*;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.ptr.IntByReference;

import net.jazdw.jnacan.c.can_filter;
import net.jazdw.jnacan.c.can_frame;
import net.jazdw.jnacan.c.cmsghdr;
import net.jazdw.jnacan.c.iovec;
import net.jazdw.jnacan.c.msghdr;
import net.jazdw.jnacan.c.sockaddr;
import net.jazdw.jnacan.c.sockaddr_can;

/**
 * A raw CAN socket which sends and receives CAN frames
 * 
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public class RawCanSocket extends CanSocket<can_frame, CanFrame> {
    public RawCanSocket() {
        super(true, false);
    }
    
    protected CanFilter[] currentFilters = null;
    protected boolean timestampEnabled = false;
    
    /* (non-Javadoc)
     * @see net.jazdw.jnacan.CanSocket#open()
     */
    @Override
    public void open() throws SocketException {
        super.open(CanProtocol.RAW);
    }
    
    /* (non-Javadoc)
     * @see net.jazdw.jnacan.CanSocket#receive()
     */
    @Override
    public CanFrame receive() throws IOException {
        can_frame fr = new can_frame();
        super.receive(fr);
        return new CanFrame(fr);
    }
    
    /* (non-Javadoc)
     * @see net.jazdw.jnacan.CanSocket#receiveFrom(net.jazdw.jnacan.CanInterface)
     */
    @Override
    public CanFrame receiveFrom(CanInterface canIf) throws IOException {
        can_frame fr = new can_frame();
        super.receiveFrom(fr, canIf);
        return new CanFrame(fr);
    }
    
    /**
     * Receives a single time-stamped CAN frame
     * 
     * Blocks until a frame is received or the receive timeout expires (a SocketTimeoutException
     * is thrown)
     * 
     * @return
     * @throws IOException
     */
    public TimestampedCanFrame receiveTimestamped() throws IOException {
        return receiveTimestampedFrom(canInterface);
    }
    
    /**
     * Receives a single time-stamped CAN frame from the specified interface
     * 
     * Blocks until a frame is received or the receive timeout expires (a SocketTimeoutException
     * is thrown)
     * 
     * @param canIf
     * @return
     * @throws IOException
     */
    public TimestampedCanFrame receiveTimestampedFrom(CanInterface canIf) throws IOException {
        if (!canInterface.equals(CanInterface.ALL) && !canIf.equals(canInterface)) {
            throw new IllegalArgumentException("Cant receive from unbound interface, use read()");
        }
        sockaddr_can addr = new sockaddr_can();
        addr.can_family = AF_CAN;
        addr.can_ifindex = canIf.getIndex();
        sockaddr sockAddr = addr.toSockAddr();
        sockAddr.write();
        
        iovec.ByReference iov = new iovec.ByReference();
        can_frame fr = new can_frame();
        iov.iov_base = fr.getPointer();
        iov.iov_len = new NativeSize(fr.size());
        iov.write();
        
        msghdr msg = new msghdr();
        msg.msg_name = sockAddr.getPointer();
        msg.msg_namelen = sockAddr.size();
        msg.msg_iov = iov;
        msg.msg_iovlen = new NativeSize(1);
        
        cmsghdr.cmsgtimeval cmsg = new cmsghdr.cmsgtimeval();
        msg.msg_control = cmsg.getPointer();
        msg.msg_controllen = new NativeSize(cmsg.size());
        
        long bytesRead = cLib.recvmsg(fd, msg, 0).longValue();
        if (bytesRead < 0) {
            if (timeoutEnabled)
                throw new SocketTimeoutException();
            else
                throw new IOException("Native function recvMsg() returned error " + bytesRead);
        }
        if (bytesRead < fr.size())
            throw new IOException("Native function recvMsg() did not return a full length message");
        
        fr.read();
        cmsg.read();
        
        return new TimestampedCanFrame(fr, cmsg.time);
    }
    
    /**
     * Sets the CAN filters for the socket
     * If filters is null or is zero length then the filters are cleared
     * 
     * @param filters
     * @throws SocketException
     */
    public void setFilters(CanFilter... filters) throws SocketException {
        if (filters == null || filters.length <= 0) {
            clearFilters();
            return;
        }
        
        can_filter.ByReference filterRef = new can_filter.ByReference();
        can_filter[] filterArray = (can_filter[]) filterRef.toArray(filters.length);
        
        for (int i = 0; i < filters.length; i++) {
            filters[i].copyTo(filterArray[i]);
            filterArray[i].write();
        }
        
        int length = filters.length * filterArray[0].size();
        if (cLib.setsockopt(fd, SOL_CAN_RAW, CAN_RAW_FILTER, filterRef.getPointer(), length) < 0) {
            throw new SocketException("Could not set CAN_RAW_FILTER socket option");
        }
        
        currentFilters = filters;
    }
    
    /**
     * Gets the last applied filters
     * Will return null for a socket which has no filters applied
     * 
     * @return
     */
    public CanFilter[] getFilters() {
        return currentFilters;
    }

    /**
     * Clears the CAN filters on the socket
     * 
     * @throws SocketException
     */
    public void clearFilters() throws SocketException {
        setFilters(new CanFilter(0x000, 0x000));
    }
    
    /**
     * Sets the error filter on the socket
     * 
     * @param errorFilter
     * @throws IOException
     */
    public void setErrorFilter(int errorFilter) throws SocketException {
        IntByReference filter = new IntByReference(errorFilter);
        if (cLib.setsockopt(fd, SOL_CAN_RAW, CAN_RAW_ERR_FILTER, filter.getPointer(), 4) < 0) {
            throw new SocketException("Could not set CAN_RAW_ERR_FILTER socket option");
        }
    }
    
    /**
     * Sets the loop-back mode
     * This allows other sockets bound to the same interface to receive the frames
     * that this socket writes
     * 
     * @param loopback
     * @throws SocketException
     */
    public void setLoopback(boolean loopback) throws SocketException {
        int loopbackInt = loopback ? 1 : 0;
        IntByReference loopbackRef = new IntByReference(loopbackInt);
        if (cLib.setsockopt(fd, SOL_CAN_RAW, CAN_RAW_LOOPBACK, loopbackRef.getPointer(), 4) < 0) {
            throw new SocketException("Could not set CAN_RAW_LOOPBACK socket option");
        }
    }
    
    /**
     * Specifies if the socket should receive its own messages
     * Note: Loop back needs to be enabled
     * 
     * @param recvOwnMsgs
     * @throws SocketException
     */
    public void setRecvOwnMsgs(boolean recvOwnMsgs) throws SocketException {
        int recvOwnMsgsInt = recvOwnMsgs ? 1 : 0;
        IntByReference recvOwnMsgsRef = new IntByReference(recvOwnMsgsInt);
        if (cLib.setsockopt(fd, SOL_CAN_RAW, CAN_RAW_RECV_OWN_MSGS, recvOwnMsgsRef.getPointer(), 4) < 0) {
            throw new SocketException("Could not set CAN_RAW_RECV_OWN_MSGS socket option");
        }
    }

    /**
     * Set this to enable time-stamping of can frames
     * @param timestampEnabled
     * @throws SocketException
     */
    public void setTimestampEnabled(boolean timestampEnabled) throws SocketException {
        int timestampInt = timestampEnabled ? 1 : 0;
        IntByReference timestampRef = new IntByReference(timestampInt);
        if (cLib.setsockopt(fd, SOL_SOCKET, SO_TIMESTAMP, timestampRef.getPointer(), 4) < 0) {
            throw new SocketException("Could not set SO_TIMESTAMP socket option");
        }
        this.timestampEnabled = timestampEnabled;
    }
    
    public boolean isTimestampEnabled() {
        return timestampEnabled;
    }
}
