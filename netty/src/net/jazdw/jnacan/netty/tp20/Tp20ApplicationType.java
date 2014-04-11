/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.netty.tp20;

import net.jazdw.jnacan.Utils.ReverseEnumMap;
import net.jazdw.jnacan.Utils.ValueEnum;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public enum Tp20ApplicationType implements ValueEnum<Byte> {
    DIAGNOSTICS(0x01),
    INFOTAINMENT(0x20),
    APPROTOCOL(0x20),
    WFS_WIV(0x21);

    private static ReverseEnumMap<Byte, Tp20ApplicationType> map = ReverseEnumMap.create(Tp20ApplicationType.class);
    private byte value;
    
    private Tp20ApplicationType(int value) {
        this.value = (byte) value;
    }

    @Override
    public Byte value() {
        return value;
    }
    
    public static Tp20ApplicationType fromValue(byte value) {
        return map.get(value);
    }
}
