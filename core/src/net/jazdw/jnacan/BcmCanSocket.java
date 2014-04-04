/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan;

import java.io.IOException;
import java.net.SocketException;

import net.jazdw.jnacan.c.bcm_msg_head;
import net.jazdw.jnacan.c.can_frame;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public class BcmCanSocket extends CanSocket<bcm_msg_head, BcmMessage> {
    public BcmCanSocket() {
        super(false, true);
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.CanSocket#open()
     */
    public void open() throws SocketException {
        super.open(CanProtocol.BCM);
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.CanSocket#receive()
     */
    @Override
    public BcmMessage receive() throws IOException {
        bcm_msg_head msg = new bcm_msg_head();
        super.receive(msg);
        readFrames(msg);
        return new BcmMessage(msg);
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.CanSocket#receiveFrom(net.jazdw.jnacan.CanInterface)
     */
    @Override
    public BcmMessage receiveFrom(CanInterface canIf) throws IOException {
        bcm_msg_head msg = new bcm_msg_head();
        super.receiveFrom(msg, canIf);
        readFrames(msg);
        return new BcmMessage(msg);
    }
    
    /**
     * When doing read, bcm_msg_head.frames will be 0 length
     * so the first read() call will not fill the array.
     * This will set the array to the correct length and do the read again.
     * 
     * Not sure if this is actually necessary, will nframes ever be > 0? 
     * 
     * @param msg
     */
    private void readFrames(bcm_msg_head msg) {
        if (msg.nframes > 0) {
            msg.frames = new can_frame[msg.nframes];
            msg.read();
        }
    }
}
