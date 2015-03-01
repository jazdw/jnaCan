/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.netty.tp20.messages;

import lombok.Data;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
@Data
public class Tp20ChannelSetup implements Tp20Message {  
    Tp20OpCode opCode;
    byte blockSize;
    /**
     * Time to wait for ACK in microseconds. Should be greater than 4*T3
     */
    int t1;
    /**
     * Interval between packets in microseconds
     */
    int t3;
    
    private int decodeTiming(byte t) {
        byte scale = (byte) (t >> 6);
        byte value = (byte) (t & 0x3F);
        switch(scale) {
        case 0x0: // 100 usec
            return value * 100;
        case 0x1: // 1 msec
            return value * 1000;
        case 0x2: // 10 msec
            return value * 10000;
        case 0x3: // 100 msec
            return value * 100000;
        default:
            throw new UnsupportedOperationException();
        }
    }
    
    private byte encodeTiming(int usec) {
        if (usec > 6300000)
            throw new IllegalArgumentException("Can't be greater than 6300000 microseconds");
        if (usec < 100)
            throw new IllegalArgumentException("Can't be less than 100 microseconds");
        
        if (usec <= 6300) {
            return (byte) (usec / 100);
        }
        if (usec <= 63000) {
            return (byte) ((0x01 << 6) | (usec / 1000));
        }
        if (usec <= 630000) {
            return (byte) ((0x02 << 6) | (usec / 10000));
        }
        return (byte) ((0x03 << 6) | (usec / 100000));
    }
    
    @Override
    public byte[] encode() {
        byte[] data = new byte[6];
        data[0] = opCode.value();
        data[1] = blockSize;
        data[2] = encodeTiming(t1);
        data[3] = (byte) 0xFF;
        data[4] = encodeTiming(t3);
        data[5] = (byte) 0xFF;
        return data;
    }
    
    @Override
    public void decode(byte[] data) {
        if (data.length != 6) {
            throw new IllegalArgumentException("Channel setup data length should be 6 bytes");
        }
        
        opCode = Tp20OpCode.fromValue(data[0]);
        switch (opCode) {
        case CHANNEL_SETUP:
        case CONNECTION_ACKNOWLEDGE:
            break;
        default:
            throw new IllegalArgumentException("Wrong op code for channel setup");
        }
        
        blockSize = data[1];
        t1 = decodeTiming(data[2]);
        t3 = decodeTiming(data[4]);
    }
}
