/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.netty.tp20.messages;

import net.jazdw.jnacan.CanId;
import net.jazdw.jnacan.netty.tp20.Tp20ApplicationType;
import lombok.Data;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
@Data
public class Tp20ChannelConnect implements Tp20Message {
    byte destination;
    Tp20OpCode opCode;
    CanId txId;
    CanId rxId;
    Tp20ApplicationType applicationType;
    
    @Override
    public byte[] encode() {
        byte[] data = new byte[7];
        data[0] = destination;
        data[1] = opCode.value();
        
        // TODO EFF supported?
        int txId = this.txId.getSFFid();
        int rxId = this.rxId.getSFFid();
        
        data[2] = (byte) txId;
        data[3] = (byte) ((1 << 4) | (txId >> 8));
        data[4] = (byte) rxId;
        data[5] = (byte) (rxId >> 8);
        data[6] = applicationType.value();
        return data;
    }
    
    @Override
    public void decode(byte[] data) {
        if (data.length != 7) {
            throw new IllegalArgumentException("Channel connect data length should be 7 bytes");
        }
        
        opCode = Tp20OpCode.fromValue(data[1]);
        switch (opCode) {
        case CHANNEL_REPLY_APPLICATION_TYPE_UNAVAILABLE:
        case CHANNEL_REPLY_APPLICATION_TYPE_UNSUPPORTED:
        case CHANNEL_REPLY_OUT_OUT_RESOURCES:
        case CHANNEL_REPLY_POSITIVE:
        case CHANNEL_REQUEST:
            break;
        default:
            throw new IllegalArgumentException("Wrong op code for channel connect");
        }
        
        destination = data[0];
        txId = new CanId(((data[3] & 0x0F) << 8) | data[2]);
        rxId = new CanId(((data[5] & 0x0F) << 8) | data[4]);
        applicationType = Tp20ApplicationType.fromValue(data[6]);
    }
}
