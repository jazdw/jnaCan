/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.netty.tp20;

import java.net.SocketAddress;

import net.jazdw.jnacan.CanInterface;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Tp20Address extends SocketAddress {
    private static final long serialVersionUID = -6101090718909393720L;
    
    CanInterface canInterface;
    byte destination;
    
    public Tp20Address(CanInterface canInterface, byte destination) {
        if (canInterface == null) {
            throw new NullPointerException();
        }
        if (canInterface.isAllInterface()) {
            throw new IllegalArgumentException("A CAN interface must be specified");
        }
        this.canInterface = canInterface;
        this.destination = destination;
    }
}
