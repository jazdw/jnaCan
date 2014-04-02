/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.IntBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jazdw.jnacan.c.CLibrary;
import net.jazdw.jnacan.c.can_filter;
import net.jazdw.jnacan.c.can_frame;
import net.jazdw.jnacan.c.cmsghdr;
import net.jazdw.jnacan.c.ifreq;
import net.jazdw.jnacan.c.iovec;
import net.jazdw.jnacan.c.msghdr;
import net.jazdw.jnacan.c.sockaddr;
import net.jazdw.jnacan.c.sockaddr_can;
import net.jazdw.jnacan.c.timeval;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;

import static net.jazdw.jnacan.c.CLibrary.*;

/**
 * CAN bus socket using JNA to access the Linux SocketCan API
 * 
 * Read and write operations are blocking. Use setReceiveTimeout() to cause reads to timeout 
 * 
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public class CanSocket implements Closeable {
    static final Logger LOG = LoggerFactory.getLogger(CanSocket.class);
    private static final CLibrary cLib = CLibrary.INSTANCE;
    
    private int fd = -1;
    private CanInterface boundInterface = null;
    private boolean timeoutEnabled = false;
    private CanFilter[] currentFilters = null;
    
    public CanSocket() {
    }
    
    private void open(int type, int protocol) throws SocketException {
        fd = cLib.socket(PF_CAN, type, protocol);
        if (fd < 0)
            throw new SocketException("Opening CAN socket failed");
    }
    
    int ioctl(long request, ifreq ifr) {
        return cLib.ioctl(fd, new NativeLong(request), ifr);
    }
    
    /**
     * Opens a raw CAN socket
     * @throws SocketException
     */
    public void openRaw() throws SocketException {
        open(__socket_type.SOCK_RAW, CAN_RAW);
    }
    
    /**
     * Opens a BCM CAN socket
     * NOTE: Can't do anything with this yet, do not use
     * 
     * @throws SocketException
     */
    public void openBcm() throws SocketException {
        open(__socket_type.SOCK_DGRAM, CAN_BCM);
    }
    
    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    public void close() throws SocketException {
        if (fd < 0)
            return;
        if (cLib.close(fd) != 0)
            throw new SocketException("close() failed");
        fd = -1;
        this.boundInterface = null;
    }

    static byte[] toFixedLengthByteArray(String s, int length) {
        byte[] strBytes;
        try {
            strBytes = s.getBytes(Native.getDefaultStringEncoding());
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Native String encoding not supported, falling back to Java default encoding");
            strBytes = s.getBytes();
        }
        
        if (strBytes.length >= length)
            throw new IllegalArgumentException("String too long to fit in byte array of length " + length);
        byte[] ret = new byte[length];
        System.arraycopy(strBytes, 0, ret, 0, strBytes.length);
        return ret;
    }

    /**
     * Binds the socket to a can interface
     * 
     * @param canIf
     * @throws SocketException
     */
    public void bind(String canIf) throws SocketException {
        bind(new CanInterface(canIf));
    }
    
    /**
     * Binds the socket to a can interface
     * 
     * @param canIf
     * @throws SocketException
     */
    public void bind(CanInterface canIf) throws SocketException {
        canIf.resolveIndex(this);
        
        sockaddr_can addr = new sockaddr_can();
        addr.can_family = AF_CAN;
        addr.can_ifindex = canIf.getIndex();
        sockaddr sockAddr = addr.toSockAddr();
    
        if (cLib.bind(fd, sockAddr, sockAddr.size()) != 0)
            throw new SocketException("Could not bind to interface " + canIf);
        
        this.boundInterface = canIf;
    }
    
    /**
     * Receives a single CAN frame
     * 
     * Blocks until a frame is received or the receive timeout expires (a SocketTimeoutException
     * is thrown)
     * 
     * @return
     * @throws IOException
     */
    public CanFrame receive() throws IOException {
        can_frame fr = new can_frame();
        
        long bytesRead = cLib.read(fd, fr.getPointer(), new NativeSize(fr.size())).longValue();
        
        // TODO import errno.h and find out why read is returning -1 on timeout instead of EWOULDBLOCK
        if (bytesRead < 0) {
            if (timeoutEnabled)
                throw new SocketTimeoutException();
            else
                throw new IOException("Native function read() returned error " + bytesRead);
        }
        if (bytesRead < fr.size())
            throw new IOException("Native function read() did not return a full length message");
        fr.read();
        return new CanFrame(fr);
    }
    
    /**
     * Receives a single CAN frame from the specified interface
     * 
     * Blocks until a frame is received or the receive timeout expires (a SocketTimeoutException
     * is thrown)
     * 
     * @param canIf
     * @return
     * @throws IOException
     */
    public CanFrame receiveFrom(CanInterface canIf) throws IOException {
        if (!boundInterface.equals(CanInterface.ALL) && !canIf.equals(boundInterface)) {
            throw new IllegalArgumentException("Cant receive from unbound interface, use receive()");
        }
        sockaddr_can addr = new sockaddr_can();
        addr.can_family = AF_CAN;
        addr.can_ifindex = canIf.getIndex();
        sockaddr sockAddr = addr.toSockAddr();
        
        can_frame fr = new can_frame();
        
        IntBuffer addrSize = IntBuffer.wrap(new int[] {sockAddr.size()});
        long bytesRead = cLib.recvfrom(fd, fr.getPointer(), new NativeSize(fr.size()), 0, sockAddr, addrSize).longValue();
        if (bytesRead < 0) {
            if (timeoutEnabled)
                throw new SocketTimeoutException();
            else
                throw new IOException("Native function recvfrom() returned error " + bytesRead);
        }
        if (bytesRead < fr.size())
            throw new IOException("Native function recvfrom() did not return a full length message");
        fr.read();
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
        return receiveTimestampedFrom(boundInterface);
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
        if (!boundInterface.equals(CanInterface.ALL) && !canIf.equals(boundInterface)) {
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
     * Sends a single CAN frame
     * 
     * Blocks until the frame is sent
     * 
     * @param frame
     * @throws IOException
     */
    public void send(CanFrame frame) throws IOException {
        if (boundInterface.equals(CanInterface.ALL)) {
            throw new IllegalArgumentException("Cant write to all interfaces, use sendTo()");
        }

        if (isClosed())
            throw new SocketException("Socket is closed");
        
        can_frame fr = frame.toJnaType();
        fr.write();
        
        long bytesWritten = cLib.write(fd, fr.getPointer(), new NativeSize(fr.size())).longValue();
        if (bytesWritten < 0)
            throw new IOException("Native function write() returned error " + bytesWritten);
        if (bytesWritten < fr.size())
            throw new IOException("Native function write() did not write the full length message");
    }
    
    /**
     * Sends a single CAN frame on the specified interface
     * 
     * Blocks until the frame is sent
     * 
     * @param canIf
     * @param frame
     * @throws IOException
     */
    public void sendTo(CanInterface canIf, CanFrame frame) throws IOException {
        if (!boundInterface.equals(CanInterface.ALL) && !canIf.equals(boundInterface)) {
            throw new IllegalArgumentException("Cant send to unbound interface, use write()");
        }
        
        if (isClosed())
            throw new SocketException("Socket is closed");
        
        sockaddr_can addr = new sockaddr_can();
        addr.can_family = AF_CAN;
        addr.can_ifindex = canIf.getIndex();
        sockaddr sockAddr = addr.toSockAddr();
        
        can_frame fr = frame.toJnaType();
        fr.write();
        
        long bytesWritten = cLib.sendto(fd, fr.getPointer(), new NativeSize(fr.size()), 0, sockAddr, sockAddr.size()).longValue();
        if (bytesWritten < 0)
            throw new IOException("Native function sendto() returned error " + bytesWritten);
        if (bytesWritten < fr.size())
            throw new IOException("Native function sendto() did not write the full length message");
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
     * @param timestamp
     * @throws SocketException
     */
    public void setTimestamp(boolean timestamp) throws SocketException {
        int timestampInt = timestamp ? 1 : 0;
        IntByReference timestampRef = new IntByReference(timestampInt);
        if (cLib.setsockopt(fd, SOL_SOCKET, SO_TIMESTAMP, timestampRef.getPointer(), 4) < 0) {
            throw new SocketException("Could not set SO_TIMESTAMP socket option");
        }
    }

    /**
     * Returns the bound CAN interface
     * @return
     */
    public CanInterface getBoundInterface() {
        return boundInterface;
    }

    /**
     * @return true if the socket is closed
     */
    public boolean isClosed() {
        return fd < 0;
    }

    /**
     * @return true if the socket is bound
     */
    public boolean isBound() {
        return boundInterface != null;
    }

    /**
     * Sets the SO_RCVTIMEO option for the socket
     * Causes the socket to timeout when reading
     * Set to 0 to disable
     * 
     * @param timeout in ms
     * @throws IOException
     */
    public void setReceiveTimeout(int timeout) throws SocketException {
        timeval time = msToTimeval(timeout);
        time.write();
        
        if (cLib.setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO, time.getPointer(), time.size()) < 0) {
            throw new SocketException("Could not set SO_RCVTIMEO socket option");
        }
        
        timeoutEnabled = timeout == 0 ? false : true;
    }
    
    private timeval msToTimeval(int ms) {
        timeval time = new timeval();
        
        long usec = ms * 1000;
        long sec = usec / 1000000;
        usec = usec % 1000000;
        
        time.tv_sec = new NativeLong(sec);
        time.tv_usec = new NativeLong(usec);
        return time;
    }
}
