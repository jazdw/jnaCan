/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan.netty;

import net.jazdw.jnacan.CanFilter;
import net.jazdw.jnacan.netty.CanChannelConfig.BusSpeed;
import io.netty.channel.ChannelOption;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public final class CanChannelOption<T> extends ChannelOption<T> {
    public static final CanChannelOption<BusSpeed> BUS_SPEED =
            new CanChannelOption<BusSpeed>("BUS_SPEED");

    public static final CanChannelOption<Boolean> TIMESTAMP =
            new CanChannelOption<Boolean>("TIMESTAMP");
    
    public static final CanChannelOption<Boolean> LOOPBACK =
            new CanChannelOption<Boolean>("LOOPBACK");
    
    public static final CanChannelOption<Boolean> RECV_OWN_MSGS =
            new CanChannelOption<Boolean>("RECV_OWN_MSGS");
    
    public static final CanChannelOption<CanFilter[]> FILTERS =
            new CanChannelOption<CanFilter[]>("FILTERS");
            
    public static final CanChannelOption<Integer> ERROR_FILTER =
            new CanChannelOption<Integer>("ERROR_FILTER");
    
    public static final CanChannelOption<Integer> READ_TIMEOUT =
            new CanChannelOption<Integer>("READ_TIMEOUT");

    @SuppressWarnings("deprecation")
    private CanChannelOption(String name) {
        super(name);
    }
}
