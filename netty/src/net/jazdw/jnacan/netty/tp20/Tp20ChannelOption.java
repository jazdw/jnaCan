/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan.netty.tp20;

import io.netty.channel.ChannelOption;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public final class Tp20ChannelOption<T> extends ChannelOption<T> {
    public static final Tp20ChannelOption<Integer> RECEIVE_TIMEOUT =
            new Tp20ChannelOption<Integer>("RECEIVE_TIMEOUT");
    
    public static final Tp20ChannelOption<Tp20ApplicationType> APPLICATION_TYPE =
            new Tp20ChannelOption<Tp20ApplicationType>("APPLICATION_TYPE");
    
    public static final Tp20ChannelOption<Byte> BLOCK_SIZE =
            new Tp20ChannelOption<Byte>("BLOCK_SIZE");
    
    public static final Tp20ChannelOption<Integer> T1 =
            new Tp20ChannelOption<Integer>("T1");
    
    public static final Tp20ChannelOption<Integer> T3 =
            new Tp20ChannelOption<Integer>("T3");

    @SuppressWarnings("deprecation")
    private Tp20ChannelOption(String name) {
        super(name);
    }
}
