/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan.netty.tp20;

import io.netty.channel.ChannelOption;
import static io.netty.channel.ChannelOption.*;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public final class Tp20ChannelOption {
	private static final Class<Tp20ChannelOption> T = Tp20ChannelOption.class;
	
    public static final ChannelOption<Integer> RECEIVE_TIMEOUT = valueOf(T, "RECEIVE_TIMEOUT");
    public static final ChannelOption<Tp20ApplicationType> APPLICATION_TYPE = valueOf(T, "APPLICATION_TYPE");
    public static final ChannelOption<Byte> BLOCK_SIZE = valueOf(T, "BLOCK_SIZE");
    public static final ChannelOption<Integer> T1 = valueOf(T, "T1");
    public static final ChannelOption<Integer> T3 = valueOf(T, "T3");
    
    private Tp20ChannelOption() { }
}
