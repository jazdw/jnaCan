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
    private volatile BusSpeed busSpeed = BusSpeed._500KBPS;
    private volatile boolean timestamp = false;
    private volatile boolean loopback = true;
    private volatile boolean recvOwnMsgs = false;
    private volatile CanFilter[] filters = new CanFilter[0];
    private volatile int errorFilter = 0;
    private volatile int readTimeout = 1000;

    public DefaultCanChannelConfig(CanChannel channel) {
        super(channel);
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions(), BUS_SPEED, TIMESTAMP, LOOPBACK, RECV_OWN_MSGS, FILTERS, ERROR_FILTER, READ_TIMEOUT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == BUS_SPEED) {
            return (T) getBusSpeed();
        }
        if (option == TIMESTAMP) {
            return (T) Boolean.valueOf(isTimestamp());
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
        if (option == READ_TIMEOUT) {
            return (T) Integer.valueOf(getReadTimeout());
        }
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == BUS_SPEED) {
            setBusSpeed((BusSpeed) value);
        } else if (option == TIMESTAMP) {
            setTimestamp((Boolean) value);
        } else if (option == LOOPBACK) {
            setLoopback((Boolean) value);
        } else if (option == RECV_OWN_MSGS) {
            setRecvOwnMsgs((Boolean) value);
        } else if (option == FILTERS) {
            setFilters((CanFilter[]) value);
        } else if (option == ERROR_FILTER) {
            setErrorFilter((Integer) value);
        } else if (option == READ_TIMEOUT) {
            setReadTimeout((Integer) value);
        } else {
            return super.setOption(option, value);
        }
        return true;
    }

    @Override
    public BusSpeed getBusSpeed() {
        return busSpeed;
    }

    @Override
    public CanChannelConfig setBusSpeed(BusSpeed busSpeed) {
        this.busSpeed = busSpeed;
        return this;
    }

    @Override
    public boolean isTimestamp() {
        return timestamp;
    }

    @Override
    public CanChannelConfig setTimestamp(boolean timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public boolean isLoopback() {
        return loopback;
    }

    @Override
    public CanChannelConfig setLoopback(boolean loopback) {
        this.loopback = loopback;
        return this;
    }

    @Override
    public boolean isRecvOwnMsgs() {
        return recvOwnMsgs;
    }

    @Override
    public CanChannelConfig setRecvOwnMsgs(boolean recvOwnMsgs) {
        this.recvOwnMsgs = recvOwnMsgs;
        return this;
    }

    @Override
    public CanFilter[] getFilters() {
        return filters;
    }

    @Override
    public CanChannelConfig setFilters(CanFilter... filters) {
        this.filters = filters;
        return this;
    }

    @Override
    public int getErrorFilter() {
        return errorFilter;
    }

    @Override
    public CanChannelConfig setErrorFilter(int errorFilter) {
        this.errorFilter = errorFilter;
        return this;
    }

    @Override
    public CanChannelConfig setReadTimeout(int readTimeout) {
        if (readTimeout < 0) {
            throw new IllegalArgumentException("readTime must be >= 0");
        }
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public CanChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    public CanChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    @Override
    public CanChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    @Override
    public CanChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public CanChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public CanChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    public CanChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    @Override
    public CanChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    @Override
    public CanChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    @Override
    public CanChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }
}
