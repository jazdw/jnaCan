/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public class CanSocket implements Closeable {
    static final Logger LOG = LoggerFactory.getLogger(CanSocket.class);
    private static final CLibrary cLib = CLibrary.INSTANCE;
    
    private int fd = -1;
    private CanInterface boundInterface = null;
    private boolean timeoutEnabled = false;
    
    public CanSocket() {
    }
    
    private void open(int type, int protocol) throws IOException {
        fd = cLib.socket(PF_CAN, type, protocol);
        if (fd < 0)
            throw new IOException("Opening CAN socket failed");
    }
    
    int ioctl(long request, ifreq ifr) {
        return cLib.ioctl(fd, new NativeLong(request), ifr);
    }
    
    public void openRaw() throws IOException {
        open(__socket_type.SOCK_RAW, CAN_RAW);
    }
    
    public void openBcm() throws IOException {
        open(__socket_type.SOCK_DGRAM, CAN_BCM);
    }
    
    public void close() throws IOException {
        if (fd < 0)
            return;
        if (cLib.close(fd) != 0)
            throw new IOException("close() failed");
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

    public void bind(String canIf) throws IOException {
        bind(new CanInterface(canIf));
    }
    
    public void bind(CanInterface canIf) throws IOException {
        canIf.resolveIndex(this);
        
        sockaddr_can addr = new sockaddr_can();
        addr.can_family = AF_CAN;
        addr.can_ifindex = canIf.getIndex();
        sockaddr sockAddr = addr.toSockAddr();
    
        if (cLib.bind(fd, sockAddr, sockAddr.size()) != 0)
            throw new IOException("Could not bind to interface " + canIf);
        
        this.boundInterface = canIf;
    }
    
    public CanFrame read() throws IOException {
        can_frame fr = new can_frame();
        
        long bytesRead = cLib.read(fd, fr.getPointer(), new NativeSize(fr.size())).longValue();
        
        // TODO import errno.h and find out why read is returning -1 on timeout instead of EWOULDBLOCK
        if (bytesRead < 0) {
            if (timeoutEnabled)
                throw new SocketTimeoutException();
            else
                throw new IOException("read() failed");
        }
        if (bytesRead < fr.size())
            throw new IOException("read() incomplete");
        fr.read();
        return new CanFrame(fr);
    }
    
    public CanFrame recvFrom(CanInterface canIf) throws IOException {
        if (!boundInterface.equals(CanInterface.ALL) && !canIf.equals(boundInterface)) {
            throw new IllegalArgumentException("Cant receive from unbound interface, use read()");
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
                throw new IOException("recvFrom() failed");
        }
        if (bytesRead < fr.size())
            throw new IOException("recvFrom() incomplete");
        fr.read();
        return new CanFrame(fr);
    }
    
    public TimestampedCanFrame recvMsg() throws IOException {
        return recvMsg(boundInterface);
    }
    
    public TimestampedCanFrame recvMsg(CanInterface canIf) throws IOException {
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
                throw new IOException("recvMsg() failed");
        }
        if (bytesRead < fr.size())
            throw new IOException("recvMsg() incomplete");
        
        fr.read();
        cmsg.read();
        
        return new TimestampedCanFrame(fr, cmsg.time);
    }
    
    public void write(CanFrame frame) throws IOException {
        if (boundInterface.equals(CanInterface.ALL)) {
            throw new IllegalArgumentException("Cant write to all interfaces, use sendTo()");
        }
        can_frame fr = frame.toJnaType();
        fr.write();
        
        long bytesWritten = cLib.write(fd, fr.getPointer(), new NativeSize(fr.size())).longValue();
        if (bytesWritten < 0)
            throw new IOException("write() failed");
        if (bytesWritten < fr.size())
            throw new IOException("write() incomplete");
    }
    
    public void sendTo(CanInterface canIf, CanFrame frame) throws IOException {
        if (!boundInterface.equals(CanInterface.ALL) && !canIf.equals(boundInterface)) {
            throw new IllegalArgumentException("Cant send to unbound interface, use write()");
        }
        
        sockaddr_can addr = new sockaddr_can();
        addr.can_family = AF_CAN;
        addr.can_ifindex = canIf.getIndex();
        sockaddr sockAddr = addr.toSockAddr();
        
        can_frame fr = frame.toJnaType();
        fr.write();
        
        long bytesWritten = cLib.sendto(fd, fr.getPointer(), new NativeSize(fr.size()), 0, sockAddr, sockAddr.size()).longValue();
        if (bytesWritten < 0)
            throw new IOException("write() failed");
        if (bytesWritten < fr.size())
            throw new IOException("write() incomplete");
    }
    
    public void setFilters(CanFilter... filters) throws IOException {
        if (filters.length <= 0)
            throw new IllegalArgumentException("At least one filter must be specified");
        
        can_filter.ByReference filterRef = new can_filter.ByReference();
        can_filter[] filterArray = (can_filter[]) filterRef.toArray(filters.length);
        
        for (int i = 0; i < filters.length; i++) {
            filters[i].copyTo(filterArray[i]);
            filterArray[i].write();
        }
        
        int length = filters.length * filterArray[0].size();
        if (cLib.setsockopt(fd, SOL_CAN_RAW, CAN_RAW_FILTER, filterRef.getPointer(), length) < 0) {
            throw new IOException("Could not set CAN_RAW_FILTER socket option");
        }
    }
    
    public void clearFilters() throws IOException {
        setFilters(new CanFilter(0x000, 0x000));
    }
    
    public void setErrorFilter(int errorFilter) throws IOException {
        IntByReference filter = new IntByReference(errorFilter);
        if (cLib.setsockopt(fd, SOL_CAN_RAW, CAN_RAW_ERR_FILTER, filter.getPointer(), 4) < 0) {
            throw new IOException("Could not set CAN_RAW_ERR_FILTER socket option");
        }
    }
    
    public void setLoopback(boolean loopback) throws IOException {
        int loopbackInt = loopback ? 1 : 0;
        IntByReference loopbackRef = new IntByReference(loopbackInt);
        if (cLib.setsockopt(fd, SOL_CAN_RAW, CAN_RAW_LOOPBACK, loopbackRef.getPointer(), 4) < 0) {
            throw new IOException("Could not set CAN_RAW_LOOPBACK socket option");
        }
    }
    
    public void setRecvOwnMsgs(boolean recvOwnMsgs) throws IOException {
        int recvOwnMsgsInt = recvOwnMsgs ? 1 : 0;
        IntByReference recvOwnMsgsRef = new IntByReference(recvOwnMsgsInt);
        if (cLib.setsockopt(fd, SOL_CAN_RAW, CAN_RAW_RECV_OWN_MSGS, recvOwnMsgsRef.getPointer(), 4) < 0) {
            throw new IOException("Could not set CAN_RAW_RECV_OWN_MSGS socket option");
        }
    }
    
    public void setTimestamp(boolean timestamp) throws IOException {
        int timestampInt = timestamp ? 1 : 0;
        IntByReference timestampRef = new IntByReference(timestampInt);
        if (cLib.setsockopt(fd, SOL_SOCKET, SO_TIMESTAMP, timestampRef.getPointer(), 4) < 0) {
            throw new IOException("Could not set SO_TIMESTAMP socket option");
        }
    }

    public CanInterface getBoundInterface() {
        return boundInterface;
    }

    public boolean isClosed() {
        return fd < 0;
    }

    /**
     * @return
     */
    public boolean isBound() {
        return boundInterface != null;
    }

    public void setTimeout(int timeout) throws IOException {
        timeval time = new timeval();
        
        long usec = timeout * 1000;
        long sec = usec / 1000000;
        usec = usec % 1000000;
        
        time.tv_sec = new NativeLong(sec);
        time.tv_usec = new NativeLong(usec);
        time.write();
        
        if (cLib.setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO, time.getPointer(), time.size()) < 0) {
            throw new IOException("Could not set SO_RCVTIMEO socket option");
        }
        
        timeoutEnabled = timeout == 0 ? false : true;
    }
}
