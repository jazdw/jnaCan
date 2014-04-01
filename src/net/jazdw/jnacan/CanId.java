/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan;

import static net.jazdw.jnacan.c.CLibrary.CAN_EFF_FLAG;
import static net.jazdw.jnacan.c.CLibrary.CAN_EFF_MASK;
import static net.jazdw.jnacan.c.CLibrary.CAN_ERR_FLAG;
import static net.jazdw.jnacan.c.CLibrary.CAN_RTR_FLAG;
import static net.jazdw.jnacan.c.CLibrary.CAN_SFF_MASK;
import net.jazdw.jnacan.c.CLibrary;
import lombok.Data;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
@Data
public class CanId {
    int id;
    
    public CanId(int id) {
        this.id = id;
    }
    
    public int getSffid() {
        return id & CAN_SFF_MASK;
    }
    
    public void setSffid(int id) {
        int newId = this.id & ~CAN_SFF_MASK;
        this.id = newId | (id & CAN_SFF_MASK);
    }
    
    public int getEffid() {
        return id & CAN_SFF_MASK;
    }
    
    public void setEffid(int id) {
        int newId = this.id & ~CAN_EFF_MASK;
        this.id = newId | (id & CAN_EFF_MASK);
    }
    
    public void setEFF() {
        id |= CAN_EFF_FLAG;
    }
    
    public void clearEFF() {
        id &= ~CAN_EFF_FLAG;
    }
    
    public boolean isEFF() {
        return (id & CAN_EFF_FLAG) != 0 ? true : false;
    }
    
    public void setRTR() {
        id |= CAN_RTR_FLAG;
    }
    
    public void clearRTR() {
        id &= ~CAN_RTR_FLAG;
    }
    
    public boolean isRTR() {
        return (id & CAN_RTR_FLAG) != 0 ? true : false;
    }
    
    public void setERR() {
        id |= CAN_ERR_FLAG;
    }
    
    public void clearERR() {
        id &= ~CAN_ERR_FLAG;
    }
    
    public boolean isERR() {
        return (id & CAN_ERR_FLAG) != 0 ? true : false;
    }
    
    public void setInverted() {
        id |= CLibrary.CAN_INV_FILTER;
    }
    
    public void clearInverted() {
        id &= ~CLibrary.CAN_INV_FILTER;
    }
    
    public boolean isInverted() {
        return (id & CLibrary.CAN_INV_FILTER) != 0;
    }

    @Override
    public String toString() {
        String ret;
        if (isEFF()) {
            ret = String.format("%08X", getEffid());
        }
        else {
            ret = String.format("%03X", getSffid());
        }
        if (isRTR()) {
            ret += " RTR";
        }
        return ret;
    }
}
