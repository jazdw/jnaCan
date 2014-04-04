/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.jazdw.jnacan.c.CLibrary;
import net.jazdw.jnacan.c.bcm_msg_head;
import net.jazdw.jnacan.c.can_frame;
import lombok.Data;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
@Data
public class BcmMessage implements CanMessage<bcm_msg_head> {
    BcmOperation operation;
    EnumSet<BcmFlag> flags;
    int count = 0;
    long interval1 = 0;
    long interval2 = 0;
    CanId id = null;
    List<CanFrame> frames;
    
    public BcmMessage() {
    }
    
    protected BcmMessage(bcm_msg_head msg) {
        // TODO
    }
    
    public enum BcmOperation {
        // transmit path
        TX_SETUP(CLibrary.TX_SETUP), TX_DELETE(CLibrary.TX_DELETE), TX_READ(CLibrary.TX_READ), TX_SEND(CLibrary.TX_SEND),
        // receive path
        RX_SETUP(CLibrary.RX_SETUP), RX_DELETE(CLibrary.RX_DELETE), RX_READ(CLibrary.RX_READ),
        // response codes
        TX_STATUS(CLibrary.TX_STATUS), TX_EXPIRED(CLibrary.TX_EXPIRED),
        RX_STATUS(CLibrary.RX_STATUS), RX_TIMEOUT(CLibrary.RX_TIMEOUT), RX_CHANGED(CLibrary.RX_CHANGED);
        
        int code;
        
        BcmOperation(int code) {
            this.code = code;
        }
        
        public int code() {
            return code;
        }
    }
    
    public enum BcmFlag {
        SETTIMER(CLibrary.SETTIMER), STARTTIMER(CLibrary.STARTTIMER),
        TX_COUNTEVT(CLibrary.TX_COUNTEVT), TX_ANNOUNCE(CLibrary.TX_ANNOUNCE), TX_CP_CAN_ID(CLibrary.TX_CP_CAN_ID), TX_RESET_MULTI_IDX(CLibrary.TX_RESET_MULTI_IDX),
        RX_FILTER_ID(CLibrary.RX_FILTER_ID), RX_RTR_FRAME(CLibrary.RX_RTR_FRAME), RX_CHECK_DLC(CLibrary.RX_CHECK_DLC), RX_NO_AUTOTIMER(CLibrary.RX_NO_AUTOTIMER), RX_ANNOUNCE_RESUME(CLibrary.RX_ANNOUNCE_RESUME);
        
        int flag;
        
        BcmFlag(int flag) {
            this.flag = flag;
        }
        
        public int flag() {
            return flag;
        }
    }
    
    public void setFrames(CanFrame... frames) {
        this.frames = Arrays.asList(frames);
    }
    
    @Override
    public bcm_msg_head toJnaType() {
        bcm_msg_head msg = new bcm_msg_head();
        
        msg.opcode = operation.code();
        
        int flagsValue = 0;
        for (BcmFlag flag : flags) {
            flagsValue |= flag.flag();
        }
        msg.flags = flagsValue;
        
        msg.count = count;
        msg.ival1 = Utils.msToTimeval(interval1);
        msg.ival2 = Utils.msToTimeval(interval2);
        msg.can_id = id == null ? 0 : id.getId();

        msg.nframes = frames.size();
        msg.frames = new can_frame[frames.size()];
        
        for (int i = 0; i < frames.size(); i++) {
            msg.frames[i] = frames.get(i).toJnaType();
        }
        
        return msg;
    }
    
    /**
     * @param canId
     */
    public void setId(int canId) {
        id = new CanId(canId);
    }
}
