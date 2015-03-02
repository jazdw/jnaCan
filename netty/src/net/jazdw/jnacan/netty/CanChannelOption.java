/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan.netty;

import net.jazdw.jnacan.CanFilter;
import io.netty.channel.ChannelOption;

import static io.netty.channel.ChannelOption.*;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public final class CanChannelOption {
	private static final Class<CanChannelOption> T = CanChannelOption.class;
	
    public static final ChannelOption<Boolean> TIMESTAMP_ENABLED = valueOf(T, "TIMESTAMP_ENABLED");
    public static final ChannelOption<Boolean> LOOPBACK = valueOf(T, "LOOPBACK");
    public static final ChannelOption<Boolean> RECV_OWN_MSGS = valueOf(T, "RECV_OWN_MSGS");
    public static final ChannelOption<CanFilter[]> FILTERS = valueOf(T, "FILTERS");
    public static final ChannelOption<Integer> ERROR_FILTER = valueOf(T, "ERROR_FILTER");
    public static final ChannelOption<Integer> RECEIVE_TIMEOUT = valueOf(T, "RECEIVE_TIMEOUT");
    
    private CanChannelOption() { }
}
