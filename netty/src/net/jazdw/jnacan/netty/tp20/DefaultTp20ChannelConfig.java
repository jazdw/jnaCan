/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan.netty.tp20;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;

import java.util.Map;

import static net.jazdw.jnacan.netty.tp20.Tp20ChannelOption.*;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
final class DefaultTp20ChannelConfig extends DefaultChannelConfig implements Tp20ChannelConfig {
    private volatile int receiveTimeout = 1000;
    private volatile Tp20ApplicationType applicationType = Tp20ApplicationType.DIAGNOSTICS;
    private volatile byte blockSize = 0xF;
    private volatile int t1 = 100000; // 100ms
    private volatile int t3 = 10000; // 10ms

    public DefaultTp20ChannelConfig(Tp20Channel channel) {
        super(channel);
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#getOptions()
     */
    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions(), RECEIVE_TIMEOUT, APPLICATION_TYPE, BLOCK_SIZE, T1, T3);
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#getOption(io.netty.channel.ChannelOption)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == RECEIVE_TIMEOUT) {
            return (T) Integer.valueOf(getReceiveTimeout());
        }
        else if (option == APPLICATION_TYPE) {
            return (T) getApplicationType();
        }
        else if (option == BLOCK_SIZE) {
            return (T) getBlockSize();
        }
        else if (option == T1) {
            return (T) getT1();
        }
        else if (option == T3) {
            return (T) getT3();
        }
        return super.getOption(option);
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setOption(io.netty.channel.ChannelOption, java.lang.Object)
     */
    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);
        
        if (option == RECEIVE_TIMEOUT) {
            setReceiveTimeout((Integer) value);
        } else if (option == APPLICATION_TYPE) {
            setApplicationType((Tp20ApplicationType) value);
        } else if (option == BLOCK_SIZE) {
            setBlockSize((Byte) value);
        } else if (option == T1) {
            setT1((Integer) value);
        } else if (option == T3) {
            setT3((Integer) value);
        } else {
            return super.setOption(option, value);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#setReadTimeout(int)
     */
    @Override
    public Tp20ChannelConfig setReceiveTimeout(int receiveTimeout) {
        if (receiveTimeout < 0) {
            throw new IllegalArgumentException("receiveTimeout must be >= 0");
        }
        this.receiveTimeout = receiveTimeout;
        return this;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.CanChannelConfig#getReadTimeout()
     */
    @Override
    public int getReceiveTimeout() {
        return receiveTimeout;
    }
    
    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.tp20.Tp20ChannelConfig#getApplicationType()
     */
    @Override
    public Tp20ApplicationType getApplicationType() {
        return applicationType;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.tp20.Tp20ChannelConfig#setApplicationType(net.jazdw.jnacan.netty.tp20.Tp20ApplicationType)
     */
    @Override
    public Tp20ChannelConfig setApplicationType(Tp20ApplicationType applicationType) {
        this.applicationType = applicationType;
        return this;
    }
    
    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.tp20.Tp20ChannelConfig#getBlockSize()
     */
    @Override
    public Byte getBlockSize() {
        return blockSize;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.tp20.Tp20ChannelConfig#setBlockSize(byte)
     */
    @Override
    public Tp20ChannelConfig setBlockSize(byte blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.tp20.Tp20ChannelConfig#getT1()
     */
    @Override
    public Integer getT1() {
        return t1;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.tp20.Tp20ChannelConfig#setT1(int)
     */
    @Override
    public Tp20ChannelConfig setT1(int t1) {
        this.t1 = t1;
        return this;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.tp20.Tp20ChannelConfig#getT3()
     */
    @Override
    public Integer getT3() {
        return t3;
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.netty.tp20.Tp20ChannelConfig#setT3(int)
     */
    @Override
    public Tp20ChannelConfig setT3(int t3) {
        this.t3 = t3;
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setConnectTimeoutMillis(int)
     */
    @Override
    public Tp20ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setMaxMessagesPerRead(int)
     */
    @Override
    public Tp20ChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setWriteSpinCount(int)
     */
    @Override
    public Tp20ChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setAllocator(io.netty.buffer.ByteBufAllocator)
     */
    @Override
    public Tp20ChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setRecvByteBufAllocator(io.netty.channel.RecvByteBufAllocator)
     */
    @Override
    public Tp20ChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setAutoRead(boolean)
     */
    @Override
    public Tp20ChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setAutoClose(boolean)
     */
    @Override
    public Tp20ChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setWriteBufferHighWaterMark(int)
     */
    @Override
    public Tp20ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setWriteBufferLowWaterMark(int)
     */
    @Override
    public Tp20ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    /* (non-Javadoc)
     * @see io.netty.channel.DefaultChannelConfig#setMessageSizeEstimator(io.netty.channel.MessageSizeEstimator)
     */
    @Override
    public Tp20ChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }
}
