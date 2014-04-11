/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.netty.tp20.messages;

import net.jazdw.jnacan.Utils.ReverseEnumMap;
import net.jazdw.jnacan.Utils.ValueEnum;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public interface Tp20Message {
    public enum Tp20OpCode implements ValueEnum<Byte> {
        CHANNEL_REQUEST(0xC0),
        CHANNEL_REPLY_POSITIVE(0xD0),
        CHANNEL_REPLY_APPLICATION_TYPE_UNSUPPORTED(0xD6),
        CHANNEL_REPLY_APPLICATION_TYPE_UNAVAILABLE(0xD7),
        CHANNEL_REPLY_OUT_OUT_RESOURCES(0xD8),
        CHANNEL_SETUP(0xA0),
        CONNECTION_ACKNOWLEDGE(0xA1),
        CONNECTION_TEST(0xA3),
        BREAK(0xA4),
        DISCONNECT(0xA8);

        private static ReverseEnumMap<Byte, Tp20OpCode> map = ReverseEnumMap.create(Tp20OpCode.class);
        private byte value;
        
        private Tp20OpCode(int value) {
            this.value = (byte) value;
        }
        
        @Override
        public Byte value() {
            return value;
        }
        
        public static Tp20OpCode fromValue(byte value) {
            return map.get(value);
        }
    }
    
    public Tp20OpCode getOpCode();
    public byte[] encode();
    public void decode(byte[] data);
}
