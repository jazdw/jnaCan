/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan;

import com.sun.jna.Structure;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public interface CanMessage<J extends Structure> {
    public J toJnaType();
}
