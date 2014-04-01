/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan.netty;

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.oio.AbstractOioMessageChannel;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.List;

import net.jazdw.jnacan.CanFrame;
import net.jazdw.jnacan.CanInterface;
import net.jazdw.jnacan.CanSocket;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public class CanChannel extends AbstractOioMessageChannel {
    private static final CanInterface REMOTE_ADDRESS = new CanInterface("None");

    private static final ChannelMetadata METADATA = new ChannelMetadata(false);
    private final CanChannelConfig config;
    private final CanSocket socket;

    public CanChannel() {
        super(null);
        this.socket = new CanSocket();
        try {
            this.socket.openRaw();
            this.socket.setTimeout(SO_TIMEOUT);
        } catch (IOException e) {
            throw new ChannelException("failed to create a new socket", e);
        }
        config = new DefaultCanChannelConfig(this);
    }

    @Override
    public CanChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return !socket.isClosed();
    }
    
    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public CanInterface localAddress() {
        return (CanInterface) super.localAddress();
    }

    @Override
    public CanInterface remoteAddress() {
        return (CanInterface) super.remoteAddress();
    }

    @Override
    protected CanInterface localAddress0() {
        return socket.getBoundInterface();
    }

    @Override
    protected CanInterface remoteAddress0() {
        return REMOTE_ADDRESS;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        CanInterface local = (CanInterface) localAddress;
        
        //socket.setBusSpeed(config.getBusSpeed());
        socket.setTimestamp(config.isTimestamp());
        socket.setLoopback(config.isLoopback());
        socket.setRecvOwnMsgs(config.isRecvOwnMsgs());
        if (config.getFilters().length > 0)
            socket.setFilters(config.getFilters());
        //socket.setErrorFilter(config.getErrorFilter());
        //socket.setReadTimeout(config.getReadTimeout());
        
        socket.bind(local);
    }

    @Override
    protected void doDisconnect() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doClose() throws Exception {
        socket.close();
    }

    /* (non-Javadoc)
     * @see io.netty.channel.Channel#isActive()
     */
    @Override
    public boolean isActive() {
        return isOpen() && socket.isBound();
    }

    /* (non-Javadoc)
     * @see io.netty.channel.Channel#metadata()
     */
    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.oio.AbstractOioMessageChannel#doReadMessages(java.util.List)
     */
    @Override
    protected int doReadMessages(List<Object> msgs) throws Exception {
        CanFrame frame;
        try {
            if (config.isTimestamp())
                frame = socket.recvMsg();
            else
                frame = socket.read();
            
            msgs.add(frame);
            return 1;
        } catch (SocketTimeoutException e) {
            return 0;
        }
        catch (IOException e) {
            return -1;
        }
    }

    /* (non-Javadoc)
     * @see io.netty.channel.AbstractChannel#doWrite(io.netty.channel.ChannelOutboundBuffer)
     */
    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        for (;;) {
            final Object o = in.current();
            if (o == null) {
                break;
            }

            if (o instanceof CanFrame) {
                socket.write((CanFrame) o);
                in.remove();
            } else {
                throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(o));
            }
        }
    }
}