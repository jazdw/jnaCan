/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.IntBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jazdw.jnacan.c.CLibrary;
import net.jazdw.jnacan.c.ifreq;
import net.jazdw.jnacan.c.sockaddr;
import net.jazdw.jnacan.c.sockaddr_can;
import net.jazdw.jnacan.c.timeval;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

import static net.jazdw.jnacan.c.CLibrary.*;

/**
 * CAN bus socket using JNA to access the Linux SocketCan API
 * 
 * Read and write operations are blocking. Use setReceiveTimeout() to cause reads to timeout 
 * 
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public abstract class CanSocket<J extends Structure, M extends CanMessage<J>> implements Closeable {
    static final Logger LOG = LoggerFactory.getLogger(CanSocket.class);
    protected static final CLibrary cLib = CLibrary.INSTANCE;
    
    protected int fd = -1;
    protected CanInterface canInterface = null;
    protected boolean timeoutEnabled = false;
    
    protected final boolean supportsBinding, supportsConnecting;
    
    public enum CanProtocol {
        RAW, BCM;
    }
    
    protected CanSocket(boolean supportsBinding, boolean supportsConnecting) {
        this.supportsBinding = supportsBinding;
        this.supportsConnecting = supportsConnecting;
    }
    
    int ioctl(long request, ifreq ifr) {
        return cLib.ioctl(fd, new NativeLong(request), ifr);
    }
    
    /**
     * Opens the socket
     * 
     * @throws SocketException
     */
    public abstract void open() throws SocketException;
    
    /**
     * Opens the socket
     * 
     * @param protocol
     * @throws SocketException
     */
    protected void open(CanProtocol protocol) throws SocketException {
        int type, protocolInt;
        switch(protocol) {
        case BCM:
            type = __socket_type.SOCK_DGRAM;
            protocolInt = CAN_BCM;
            break;
        case RAW:
            type = __socket_type.SOCK_RAW;
            protocolInt = CAN_RAW;
            break;
        default:
            throw new UnsupportedOperationException("Protocol " + protocol + " is unsupported");
        }
        
        fd = cLib.socket(PF_CAN, type, protocolInt);
        if (fd < 0)
            throw new SocketException("Opening CAN socket failed");
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
        this.canInterface = null;
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
        if (!supportsBinding)
            throw new UnsupportedOperationException("This socket does not support binding");
        
        canIf.resolveIndex(this);
        
        sockaddr_can addr = new sockaddr_can();
        addr.can_family = AF_CAN;
        addr.can_ifindex = canIf.getIndex();
        sockaddr sockAddr = addr.toSockAddr();
    
        if (cLib.bind(fd, sockAddr, sockAddr.size()) != 0)
            throw new SocketException("Could not bind to interface " + canIf);
        
        this.canInterface = canIf;
    }
    
    /**
     * Connects the socket to a can interface
     * 
     * @param canIf
     * @throws SocketException
     */
    public void connect(String canIf) throws SocketException {
        connect(new CanInterface(canIf));
    }
    
    /**
     * Connects the socket to a can interface
     * 
     * @param canIf
     * @throws SocketException
     */
    public void connect(CanInterface canIf) throws SocketException {
        if (!supportsConnecting)
            throw new UnsupportedOperationException("This socket does not support connecting");
        
        canIf.resolveIndex(this);
        
        sockaddr_can addr = new sockaddr_can();
        addr.can_family = AF_CAN;
        addr.can_ifindex = canIf.getIndex();
        sockaddr sockAddr = addr.toSockAddr();
    
        if (cLib.connect(fd, sockAddr, sockAddr.size()) != 0)
            throw new SocketException("Could not connect to interface " + canIf);
        
        this.canInterface = canIf;
    }
    
    /**
     * Receives a single CAN message
     * 
     * Blocks until a message is received or the receive timeout expires (a SocketTimeoutException
     * is thrown)
     * 
     * @throws IOException
     */
    public abstract M receive() throws IOException;
    
    protected void receive(J struct) throws IOException {
        long bytesRead = cLib.read(fd, struct.getPointer(), new NativeSize(struct.size())).longValue();
        
        // TODO import errno.h and find out why read is returning -1 on timeout instead of EWOULDBLOCK
        if (bytesRead < 0) {
            if (timeoutEnabled)
                throw new SocketTimeoutException();
            else
                throw new IOException("Native function read() returned error " + bytesRead);
        }
        if (bytesRead < struct.size())
            throw new IOException("Native function read() did not return a full length message");
        struct.read();
    }
    
    /**
     * Receives a single CAN message from the specified interface
     * 
     * Blocks until a message is received or the receive timeout expires (a SocketTimeoutException
     * is thrown)
     * 
     * @param canIf
     * @return
     * @throws IOException
     */
    public abstract M receiveFrom(CanInterface canIf) throws IOException;
    
    protected void receiveFrom(J struct, CanInterface canIf) throws IOException {
        if (!canInterface.equals(CanInterface.ALL) && !canIf.equals(canInterface)) {
            throw new IllegalArgumentException("Cant receive from unbound interface, use receive()");
        }
        sockaddr_can addr = new sockaddr_can();
        addr.can_family = AF_CAN;
        addr.can_ifindex = canIf.getIndex();
        sockaddr sockAddr = addr.toSockAddr();
        
        IntBuffer addrSize = IntBuffer.wrap(new int[] {sockAddr.size()});
        long bytesRead = cLib.recvfrom(fd, struct.getPointer(), new NativeSize(struct.size()), 0, sockAddr, addrSize).longValue();
        if (bytesRead < 0) {
            if (timeoutEnabled)
                throw new SocketTimeoutException();
            else
                throw new IOException("Native function recvfrom() returned error " + bytesRead);
        }
        if (bytesRead < struct.size())
            throw new IOException("Native function recvfrom() did not return a full length message");
        struct.read();
    }
    
    /**
     * Sends a single CAN message (frame, BCM message etc)
     * 
     * Blocks until the message is sent
     * 
     * @param msg
     * @throws IOException
     */
    public void send(M msg) throws IOException {
        if (canInterface.equals(CanInterface.ALL)) {
            throw new IllegalArgumentException("Cant write to all interfaces, use sendTo()");
        }

        if (isClosed())
            throw new SocketException("Socket is closed");
        
        J struct = msg.toJnaType();
        struct.write();
        
        long bytesWritten = cLib.write(fd, struct.getPointer(), new NativeSize(struct.size())).longValue();
        if (bytesWritten < 0)
            throw new IOException("Native function write() returned error " + bytesWritten);
        if (bytesWritten < struct.size())
            throw new IOException("Native function write() did not write the full length message");
    }
    
    /**
     * Sends a single CAN message on the specified interface
     * 
     * Blocks until the message is sent
     * 
     * @param canIf
     * @param msg
     * @throws IOException
     */
    public void sendTo(CanInterface canIf, M msg) throws IOException {
        if (!canInterface.equals(CanInterface.ALL) && !canIf.equals(canInterface)) {
            throw new IllegalArgumentException("Cant send to unbound interface, use write()");
        }
        
        if (isClosed())
            throw new SocketException("Socket is closed");
        
        sockaddr_can addr = new sockaddr_can();
        addr.can_family = AF_CAN;
        addr.can_ifindex = canIf.getIndex();
        sockaddr sockAddr = addr.toSockAddr();
        
        J struct = msg.toJnaType();
        struct.write();
        
        long bytesWritten = cLib.sendto(fd, struct.getPointer(), new NativeSize(struct.size()), 0, sockAddr, sockAddr.size()).longValue();
        if (bytesWritten < 0)
            throw new IOException("Native function sendto() returned error " + bytesWritten);
        if (bytesWritten < struct.size())
            throw new IOException("Native function sendto() did not write the full length message");
    }

    /**
     * Returns the bound CAN interface
     * @return
     */
    public CanInterface getBoundInterface() {
        if (!supportsBinding)
            throw new UnsupportedOperationException("This socket does not support binding");
        
        return canInterface;
    }
    
    /**
     * Returns the connected CAN interface
     * @return
     */
    public CanInterface getConnectedInterface() {
        if (!supportsConnecting)
            throw new UnsupportedOperationException("This socket does not support connecting");
        
        return canInterface;
    }

    /**
     * @return true if the socket is closed
     */
    public boolean isClosed() {
        return fd < 0;
    }

    /**
     * @return true if the socket is connected
     */
    public boolean isConnected() {
        if (!supportsConnecting)
            throw new UnsupportedOperationException("This socket does not support connecting");
        
        return canInterface != null;
    }
    
    /**
     * @return true if the socket is bound
     */
    public boolean isBound() {
        if (!supportsBinding)
            throw new UnsupportedOperationException("This socket does not support binding");
        
        return canInterface != null;
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
        timeval time = Utils.msToTimeval(timeout);
        time.write();
        
        if (cLib.setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO, time.getPointer(), time.size()) < 0) {
            throw new SocketException("Could not set SO_RCVTIMEO socket option");
        }
        
        timeoutEnabled = timeout == 0 ? false : true;
    }
}
