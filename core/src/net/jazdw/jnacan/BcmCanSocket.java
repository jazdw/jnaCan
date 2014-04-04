/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan;

import java.io.IOException;
import java.net.SocketException;

import net.jazdw.jnacan.c.bcm_msg_head;

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
        return new BcmMessage(msg);
    }

    /* (non-Javadoc)
     * @see net.jazdw.jnacan.CanSocket#receiveFrom(net.jazdw.jnacan.CanInterface)
     */
    @Override
    public BcmMessage receiveFrom(CanInterface canIf) throws IOException {
        bcm_msg_head msg = new bcm_msg_head();
        super.receiveFrom(msg, canIf);
        return new BcmMessage(msg);
    }
}
