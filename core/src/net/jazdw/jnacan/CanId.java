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
 * Represents a CAN ID with support for standard and extended frame formats (SFF/EFF)
 * 
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
@Data
public class CanId {
    int id;
    
    public CanId(int id) {
        this.id = id;
    }
    
    /**
     * Gets the ID as standard frame format
     * @return
     */
    public int getSFFid() {
        return id & CAN_SFF_MASK;
    }
    
    /**
     * Sets the standard frame format ID
     * @param id
     */
    public void setSFFid(int id) {
        int newId = this.id & ~CAN_SFF_MASK;
        this.id = newId | (id & CAN_SFF_MASK);
    }
    
    /**
     * Gets the ID as extended frame format
     * @return
     */
    public int getEFFid() {
        return id & CAN_SFF_MASK;
    }
    
    /**
     * Sets the extended frame format ID
     * @param id
     */
    public void setEFFid(int id) {
        int newId = this.id & ~CAN_EFF_MASK;
        this.id = newId | (id & CAN_EFF_MASK);
    }
    
    /**
     * Sets this ID to extended frame format mode
     */
    public void setEFF() {
        id |= CAN_EFF_FLAG;
    }
    
    /**
     * Sets this ID back to standard frame format mode
     */
    public void clearEFF() {
        id &= ~CAN_EFF_FLAG;
    }
    
    /**
     * @return true if extended frame format
     */
    public boolean isEFF() {
        return (id & CAN_EFF_FLAG) != 0 ? true : false;
    }
    
    /**
     * Sets the RTR (remote response) flag
     */
    public void setRTR() {
        id |= CAN_RTR_FLAG;
    }
    
    /**
     * Clears the RTR (remote response) flag
     */
    public void clearRTR() {
        id &= ~CAN_RTR_FLAG;
    }
    
    /**
     * @return true if is remote response
     */
    public boolean isRTR() {
        return (id & CAN_RTR_FLAG) != 0 ? true : false;
    }
    
    /**
     * Sets the error flag
     */
    public void setERR() {
        id |= CAN_ERR_FLAG;
    }
    
    /**
     * Clears the error flag
     */
    public void clearERR() {
        id &= ~CAN_ERR_FLAG;
    }
    
    /**
     * @return true if is an error frame
     */
    public boolean isERR() {
        return (id & CAN_ERR_FLAG) != 0 ? true : false;
    }
    
    /**
     * Used when specifying inverted CAN ID filters
     * Do not use for sending CAN frames
     */
    public void setInverted() {
        id |= CLibrary.CAN_INV_FILTER;
    }
    
    /**
     * Clears the inverted property of a CAN ID filter
     */
    public void clearInverted() {
        id &= ~CLibrary.CAN_INV_FILTER;
    }
    
    /**
     * Used when specifying inverted CAN ID filters
     * @return
     */
    public boolean isInverted() {
        return (id & CLibrary.CAN_INV_FILTER) != 0;
    }

    @Override
    public String toString() {
        String ret;
        if (isEFF()) {
            ret = String.format("%08X", getEFFid());
        }
        else {
            ret = String.format("%03X", getSFFid());
        }
        if (isRTR()) {
            ret += " RTR";
        }
        return ret;
    }
}
