/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan.netty.tp20;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public interface Tp20ChannelConfig extends ChannelConfig {
    /**
     * Gets the maximal time (in ms) to block while trying to read from the CAN interface.
     * @return
     */
    int getReceiveTimeout();
    
    /**
     * Sets the maximal time (in ms) to block while trying to read from the CAN interface. Default is 1000ms
     */
    Tp20ChannelConfig setReceiveTimeout(int readTimout);
    
    /**
     * Gets the application type that the channel will be used for
     * @return
     */
    Tp20ApplicationType getApplicationType();
    
    /**
     * Sets the application type that the channel will be used for
     * 
     * @param applicationType
     * @return
     */
    Tp20ChannelConfig setApplicationType(Tp20ApplicationType applicationType);
    
    /**
     * Gets the block size
     * @return
     */
    Byte getBlockSize();
    
    /**
     * Sets the block size
     * 
     * @param blockSize
     * @return
     */
    Tp20ChannelConfig setBlockSize(byte blockSize);
    
    /**
     * Gets timing parameter 1
     * @return
     */
    Integer getT1();
    
    /**
     * Sets timing paramater 1
     * 
     * @param t1
     * @return
     */
    Tp20ChannelConfig setT1(int t1);
    
    /**
     * Gets timing parameter 3
     * @return
     */
    Integer getT3();
    
    /**
     * Sets timing paramater 3
     * 
     * @param t3
     * @return
     */
    Tp20ChannelConfig setT3(int t3);
    
    
    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setConnectTimeoutMillis(int)
     */
    @Override
    Tp20ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setMaxMessagesPerRead(int)
     */
    @Override
    Tp20ChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setWriteSpinCount(int)
     */
    @Override
    Tp20ChannelConfig setWriteSpinCount(int writeSpinCount);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setAllocator(io.netty.buffer.ByteBufAllocator)
     */
    @Override
    Tp20ChannelConfig setAllocator(ByteBufAllocator allocator);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setRecvByteBufAllocator(io.netty.channel.RecvByteBufAllocator)
     */
    @Override
    Tp20ChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setAutoRead(boolean)
     */
    @Override
    Tp20ChannelConfig setAutoRead(boolean autoRead);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setAutoClose(boolean)
     */
    @Override
    Tp20ChannelConfig setAutoClose(boolean autoClose);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setWriteBufferHighWaterMark(int)
     */
    @Override
    Tp20ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setWriteBufferLowWaterMark(int)
     */
    @Override
    Tp20ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setMessageSizeEstimator(io.netty.channel.MessageSizeEstimator)
     */
    @Override
    Tp20ChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);
}