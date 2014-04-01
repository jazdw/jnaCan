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
    enum BusSpeed {
        _250KBPS(250),
        _500KBPS(500),
        _1MBPS(1000);

        private final int value;

        BusSpeed(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static BusSpeed valueOf(int value) {
            for (BusSpeed busSpeed : BusSpeed.values()) {
                if (busSpeed.value == value) {
                    return busSpeed;
                }
            }
            throw new IllegalArgumentException("unknown " + BusSpeed.class.getSimpleName() + " value: " + value);
        }
    }

    BusSpeed getBusSpeed();
    
    CanChannelConfig setBusSpeed(BusSpeed busSpeed);

    boolean isTimestamp();
    
    CanChannelConfig setTimestamp(boolean timestamp);

    boolean isLoopback();
    
    CanChannelConfig setLoopback(boolean loopback);

    boolean isRecvOwnMsgs();
    
    CanChannelConfig setRecvOwnMsgs(boolean recvOwnMsgs);

    CanFilter[] getFilters();
    
    CanChannelConfig setFilters(CanFilter... filters);

    int getErrorFilter();
    
    CanChannelConfig setErrorFilter(int errorFilter);

    int getReadTimeout();
    
    /**
     * Sets the maximal time (in ms) to block while try to read from the CAN interface. Default is 1000ms
     */
    CanChannelConfig setReadTimeout(int readTimout);
    
    @Override
    CanChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    CanChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    CanChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    CanChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    CanChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    CanChannelConfig setAutoRead(boolean autoRead);

    @Override
    CanChannelConfig setAutoClose(boolean autoClose);

    @Override
    CanChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    @Override
    CanChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    @Override
    CanChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);
}