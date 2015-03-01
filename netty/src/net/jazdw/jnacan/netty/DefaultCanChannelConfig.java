/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan.netty;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;

import java.util.Map;

import net.jazdw.jnacan.CanFilter;
import static net.jazdw.jnacan.netty.CanChannelOption.*;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
final class DefaultCanChannelConfig extends DefaultChannelConfig implements CanChannelConfig {
    private volatile boolean timestamp = false;
    private volatile boolean loopback = true;
    private volatile boolean recvOwnMsgs = false;
    private volatile CanFilter[] filters = new CanFilter[0];
    private volatile int errorFilter = 0;
    private volatile int readTimeout = 1000;

    public DefaultCanChannelConfig(CanChannel channel) {
        super(channel);
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#getOptions()
     */
    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions(), TIMESTAMP_ENABLED, LOOPBACK, RECV_OWN_MSGS, FILTERS, ERROR_FILTER, RECEIVE_TIMEOUT);
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#getOption(io.netty.channel.ChannelOption)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == TIMESTAMP_ENABLED) {
            return (T) Boolean.valueOf(isTimestampEnabled());
        }
        if (option == LOOPBACK) {
            return (T) Boolean.valueOf(isLoopback());
        }
        if (option == RECV_OWN_MSGS) {
            return (T) Boolean.valueOf(isRecvOwnMsgs());
        }
        if (option == FILTERS) {
            return (T) getFilters();
        }
        if (option == ERROR_FILTER) {
            return (T) Integer.valueOf(getErrorFilter());
        }
        if (option == RECEIVE_TIMEOUT) {
            return (T) Integer.valueOf(getReceiveTimeout());
        }
        return super.getOption(option);
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setOption(io.netty.channel.ChannelOption, java.lang.Object)
     */
    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);
        
        if (option == TIMESTAMP_ENABLED) {
            setTimestampEnabled((Boolean) value);
        } else if (option == LOOPBACK) {
            setLoopback((Boolean) value);
        } else if (option == RECV_OWN_MSGS) {
            setRecvOwnMsgs((Boolean) value);
        } else if (option == FILTERS) {
            setFilters((CanFilter[]) value);
        } else if (option == ERROR_FILTER) {
            setErrorFilter((Integer) value);
        } else if (option == RECEIVE_TIMEOUT) {
            setReceiveTimeout((Integer) value);
        } else {
            return super.setOption(option, value);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#isTimestamp()
     */
    @Override
    public boolean isTimestampEnabled() {
        return timestamp;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#setTimestamp(boolean)
     */
    @Override
    public CanChannelConfig setTimestampEnabled(boolean timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#isLoopback()
     */
    @Override
    public boolean isLoopback() {
        return loopback;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#setLoopback(boolean)
     */
    @Override
    public CanChannelConfig setLoopback(boolean loopback) {
        this.loopback = loopback;
        return this;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#isRecvOwnMsgs()
     */
    @Override
    public boolean isRecvOwnMsgs() {
        return recvOwnMsgs;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#setRecvOwnMsgs(boolean)
     */
    @Override
    public CanChannelConfig setRecvOwnMsgs(boolean recvOwnMsgs) {
        this.recvOwnMsgs = recvOwnMsgs;
        return this;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#getFilters()
     */
    @Override
    public CanFilter[] getFilters() {
        return filters;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#setFilters(net.jazdw.jnacan.CanFilter[])
     */
    @Override
    public CanChannelConfig setFilters(CanFilter... filters) {
        this.filters = filters;
        return this;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#getErrorFilter()
     */
    @Override
    public int getErrorFilter() {
        return errorFilter;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#setErrorFilter(int)
     */
    @Override
    public CanChannelConfig setErrorFilter(int errorFilter) {
        this.errorFilter = errorFilter;
        return this;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#setReadTimeout(int)
     */
    @Override
    public CanChannelConfig setReceiveTimeout(int readTimeout) {
        if (readTimeout < 0) {
            throw new IllegalArgumentException("readTimeout must be >= 0");
        }
        this.readTimeout = readTimeout;
        return this;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#getReadTimeout()
     */
    @Override
    public int getReceiveTimeout() {
        return readTimeout;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setConnectTimeoutMillis(int)
     */
    @Override
    public CanChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setMaxMessagesPerRead(int)
     */
    @Override
    public CanChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setWriteSpinCount(int)
     */
    @Override
    public CanChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setAllocator(io.netty.buffer.ByteBufAllocator)
     */
    @Override
    public CanChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setRecvByteBufAllocator(io.netty.channel.RecvByteBufAllocator)
     */
    @Override
    public CanChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setAutoRead(boolean)
     */
    @Override
    public CanChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setAutoClose(boolean)
     */
    @Override
    public CanChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setWriteBufferHighWaterMark(int)
     */
    @Override
    public CanChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setWriteBufferLowWaterMark(int)
     */
    @Override
    public CanChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setMessageSizeEstimator(io.netty.channel.MessageSizeEstimator)
     */
    @Override
    public CanChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }
}
