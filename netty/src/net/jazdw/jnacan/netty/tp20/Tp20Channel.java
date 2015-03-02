/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan.netty.tp20;

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.EventLoop;
import io.netty.channel.oio.AbstractOioMessageChannel;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import static net.jazdw.jnacan.netty.tp20.Tp20ChannelOption.*;
import net.jazdw.jnacan.CanFilter;
import net.jazdw.jnacan.CanFrame;
import net.jazdw.jnacan.CanId;
import net.jazdw.jnacan.CanInterface;
import net.jazdw.jnacan.RawCanSocket;
import net.jazdw.jnacan.netty.tp20.messages.Tp20ChannelConnect;
import net.jazdw.jnacan.netty.tp20.messages.Tp20ChannelSetup;
import net.jazdw.jnacan.netty.tp20.messages.Tp20Message.Tp20OpCode;

/**
 * Netty 5.0 channel implementation for VW Transport Protocol 2.0
 * 
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public class Tp20Channel extends AbstractOioMessageChannel {
    private static final ChannelMetadata METADATA = new ChannelMetadata(false);
    private final Tp20ChannelConfig config;
    private final RawCanSocket socket;
    
    private Tp20Address remoteAddress;
    private CanId txId;
    private CanId rxId;

    public Tp20Channel(EventLoop eventLoop) {
        super(null, eventLoop);
        this.socket = new RawCanSocket();
        try {
            this.socket.open();
        } catch (IOException e) {
            throw new ChannelException("failed to create a new socket", e);
        }
        config = new DefaultTp20ChannelConfig(this);
    }

    /* (non-Javadoc)
     * @see io.netty.channel.Channel#config()
     */
    @Override
    public Tp20ChannelConfig config() {
        return config;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.Channel#isOpen()
     */
    @Override
    public boolean isOpen() {
        return !socket.isClosed();
    }
    
    /* (non-Javadoc)
     * @see io.netty.channel.oio.AbstractOioChannel#doConnect(java.net.SocketAddress, java.net.SocketAddress)
     */
    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        if (remoteAddress == null || !(remoteAddress instanceof Tp20Address)) {
            throw new IllegalArgumentException("remoteAddress must be a Tp20Address");
        }
        this.remoteAddress = (Tp20Address) remoteAddress;
        
        socket.setReceiveTimeout(config.getReceiveTimeout());
        socket.bind(this.remoteAddress.getCanInterface());

        socket.setFilters(new CanFilter(0x200 + this.remoteAddress.getDestination(), 0xFFF));
        
        Tp20ChannelConnect connectRequest = new Tp20ChannelConnect();
        connectRequest.setOpCode(Tp20OpCode.CHANNEL_REQUEST);
        connectRequest.setApplicationType(config.getApplicationType());
        connectRequest.setDestination(this.remoteAddress.getDestination());
        connectRequest.setRxId(new CanId(0x000));
        connectRequest.setTxId(new CanId(0x300)); // TODO confirm and allow configure
        
        socket.send(new CanFrame(0x200, connectRequest.encode()));
        // TODO retry receive if we get a timeout or response is wrong
        Tp20ChannelConnect connectResponse = new Tp20ChannelConnect();
        connectResponse.decode(socket.receive().getData());
        if (connectResponse.getOpCode() != Tp20OpCode.CHANNEL_REPLY_POSITIVE) {
            throw new SocketException("Connection failed: " + connectResponse.getOpCode());
        }
        
        rxId = connectResponse.getTxId();
        txId = connectResponse.getRxId();
        socket.setFilters(new CanFilter(rxId, 0xFFF));
        
        Tp20ChannelSetup setupRequest = new Tp20ChannelSetup();
        setupRequest.setOpCode(Tp20OpCode.CHANNEL_SETUP);
        setupRequest.setBlockSize(config.getBlockSize());
        setupRequest.setT1(config.getT1());
        setupRequest.setT3(config.getT3());
        
        socket.send(new CanFrame(txId, setupRequest.encode()));
        
        Tp20ChannelSetup setupResponse = new Tp20ChannelSetup();
        setupResponse.decode(socket.receive().getData());
        // TODO do something with params
        
        
    }

    /* (non-Javadoc)
     * @see io.netty.channel.AbstractChannel#localAddress()
     */
    @Override
    public CanInterface localAddress() {
        return (CanInterface) super.localAddress();
    }

    /* (non-Javadoc)
     * @see io.netty.channel.AbstractChannel#remoteAddress()
     */
    @Override
    public Tp20Address remoteAddress() {
        return (Tp20Address) super.remoteAddress();
    }

    /* (non-Javadoc)
     * @see io.netty.channel.AbstractChannel#localAddress0()
     */
    @Override
    protected CanInterface localAddress0() {
        return socket.getBoundInterface();
    }

    /* (non-Javadoc)
     * @see io.netty.channel.AbstractChannel#remoteAddress0()
     */
    @Override
    protected Tp20Address remoteAddress0() {
        return remoteAddress;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.AbstractChannel#doBind(java.net.SocketAddress)
     */
    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see io.netty.channel.AbstractChannel#doDisconnect()
     */
    @Override
    protected void doDisconnect() throws Exception {
        // TODO
    }

    /* (non-Javadoc)
     * @see io.netty.channel.AbstractChannel#doClose()
     */
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
            // TODO
            frame = socket.receive();
            
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
            // TODO
            if (o instanceof CanFrame) {
                socket.send((CanFrame) o);
                in.remove();
            } else {
                throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(o));
            }
        }
    }
    
    /**
     * Set channel options on the fly
     * 
     * @param option
     * @param value
     * @return
     * @throws SocketException
     */
    public <T> boolean setOption(ChannelOption<T> option, T value) throws SocketException {
        if (option == null) {
            throw new NullPointerException("option");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
        
        if (option == RECEIVE_TIMEOUT) {
            socket.setReceiveTimeout((Integer) value);
        }
        else {
            return false;
        }
        
        return true;
    }
}