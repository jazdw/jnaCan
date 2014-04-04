/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan;

import net.jazdw.jnacan.c.can_frame;
import lombok.Data;

/**
 * Represents a CAN frame
 * DLC is set automatically from the length of data
 * 
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
@Data
public class CanFrame implements CanMessage<can_frame> {
    CanId id;
    byte[] data;
    
    protected CanFrame(can_frame jnaFrame) {
        setData(new byte[jnaFrame.can_dlc]);
        this.id = new CanId(jnaFrame.can_id);
        System.arraycopy(jnaFrame.data, 0, this.data, 0, this.data.length);
    }
    
    public CanFrame() {
        this(0);
    }
    
    public CanFrame(int id) {
        this(id, new byte[0]);
    }
    
    public CanFrame(int id, byte... data) {
        this(new CanId(id), data);
    }
    
    public CanFrame(CanId id, byte... data) {
        if (data.length > 8)
            throw new IllegalArgumentException("Data larger than 8 bytes");
        this.data = data;
        this.id = id;
    }
    
    public CanFrame(int id, int... data) {
        this(new CanId(id), data);
    }
    
    public CanFrame(CanId id, int... data) {
        if (data.length > 8)
            throw new IllegalArgumentException("Data larger than 8 bytes");
        
        byte[] byteData = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            byteData[i] = (byte) data[i];
        }
        
        this.data = byteData;
        this.id = id;
    }
    
    public can_frame toJnaType() {
        byte[] jnaData = new byte[8];
        System.arraycopy(this.data, 0, jnaData, 0, this.data.length);
        return new can_frame(id.getId(), (byte) data.length, jnaData);
    }

    public void setData(byte... data) {
        if (data.length > 8)
            throw new IllegalArgumentException("Data larger than 8 bytes");
        this.data = data;
    }
    
    public void setId(int id) {
        this.id.setId(id);
    }

    @Override
    public String toString() {
        String ret = id + " [" + data.length + "] ";
        for (int i = 0; i < data.length; i++) {
            ret += String.format("%02X ", data[i]);
        }
        return ret;
    }
}
