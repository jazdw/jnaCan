/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan.netty;

import net.jazdw.jnacan.CanFilter;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public interface CanChannelConfig extends ChannelConfig {
    /**
     * @return true if time-stamping is enabled
     */
    boolean isTimestampEnabled();
    
    /**
     * Sets if the socket should time-stamp frames
     * @param timestamp
     * @return
     */
    CanChannelConfig setTimestampEnabled(boolean timestamp);

    /**
     * @return true if loop-back enabled
     */
    boolean isLoopback();
    
    /**
     * Sets the loop-back behavior
     * @param loopback
     * @return
     */
    CanChannelConfig setLoopback(boolean loopback);

    /**
     * @return true if socket receives its own messages
     */
    boolean isRecvOwnMsgs();
    
    /**
     * Sets the receive own messages behavior
     * @param recvOwnMsgs
     * @return
     */
    CanChannelConfig setRecvOwnMsgs(boolean recvOwnMsgs);

    /**
     * @return the CAN interface filters
     */
    CanFilter[] getFilters();
    
    /**
     * Sets the CAN interface filters
     * @param filters
     * @return
     */
    CanChannelConfig setFilters(CanFilter... filters);

    /**
     * @return the can interface error filter
     */
    int getErrorFilter();
    
    /**
     * Sets the can interface error filter
     * @param errorFilter
     * @return
     */
    CanChannelConfig setErrorFilter(int errorFilter);

    /**
     * Gets the maximal time (in ms) to block while trying to read from the CAN interface.
     * @return
     */
    int getReceiveTimeout();
    
    /**
     * Sets the maximal time (in ms) to block while trying to read from the CAN interface. Default is 1000ms
     */
    CanChannelConfig setReceiveTimeout(int readTimout);
    
    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setConnectTimeoutMillis(int)
     */
    @Override
    CanChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setMaxMessagesPerRead(int)
     */
    @Override
    CanChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setWriteSpinCount(int)
     */
    @Override
    CanChannelConfig setWriteSpinCount(int writeSpinCount);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setAllocator(io.netty.buffer.ByteBufAllocator)
     */
    @Override
    CanChannelConfig setAllocator(ByteBufAllocator allocator);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setRecvByteBufAllocator(io.netty.channel.RecvByteBufAllocator)
     */
    @Override
    CanChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setAutoRead(boolean)
     */
    @Override
    CanChannelConfig setAutoRead(boolean autoRead);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setAutoClose(boolean)
     */
    @Override
    CanChannelConfig setAutoClose(boolean autoClose);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setWriteBufferHighWaterMark(int)
     */
    @Override
    CanChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setWriteBufferLowWaterMark(int)
     */
    @Override
    CanChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    /* (non-Javadoc)
     * @see io.netty.channel.ChannelConfig#setMessageSizeEstimator(io.netty.channel.MessageSizeEstimator)
     */
    @Override
    CanChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);
}