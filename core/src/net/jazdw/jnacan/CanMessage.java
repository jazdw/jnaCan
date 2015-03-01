/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan;

import com.sun.jna.Structure;

/**
 * A CanMessage represents a generic message which is sent and received by a
 * CanSocket. It could be a CAN frame, a BCM message or a transport protocol
 * packet.
 * 
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public interface CanMessage<J extends Structure> {
    /**
     * Returns a JNA Structure that represents this message
     * @return
     */
    public J toJnaType();
}
