/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.test;

import java.io.IOException;
import java.util.Properties;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.oio.OioEventLoopGroup;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import net.jazdw.jnacan.CanFilter;
import net.jazdw.jnacan.CanFrame;
import net.jazdw.jnacan.CanInterface;
import net.jazdw.jnacan.netty.CanChannel;
import net.jazdw.jnacan.netty.CanChannelOption;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public class TestNettyCan {
    EventLoopGroup group = new OioEventLoopGroup();
    Properties testProps = new Properties();
    
    @BeforeClass
    public void setup() throws IOException {
        testProps.load(getClass().getResourceAsStream("test.properties"));
    }
    
    @After
    public void shutdown() throws InterruptedException {
        group.shutdownGracefully().sync();
    }
    
    @Test
    public void testWrite() throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(group);
        b.channel(CanChannel.class);
        b.option(CanChannelOption.LOOPBACK, true);
        b.option(CanChannelOption.RECV_OWN_MSGS, true);
        b.option(CanChannelOption.FILTERS, new CanFilter[] {
                new CanFilter(0x100, 0xF00),
                new CanFilter(0x200, 0xFFF)
                });
        b.handler(new ChannelInitializer<CanChannel>() {
            @Override
            public void initChannel(CanChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new SimpleChannelInboundHandler<CanFrame>() {
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        fail(cause.toString());
                    }

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, CanFrame msg) throws Exception {
                        System.out.println(msg);
                        ctx.channel().close();
                    }
                });
            }
        });
        
        CanInterface canIf = new CanInterface(testProps.getProperty("virtual.interface", "vcan0"));
        ChannelFuture connectFuture = b.bind(canIf);
        connectFuture.sync();
        connectFuture.channel().writeAndFlush(new CanFrame(0x1A0, new byte[] {1, 2, 3, 4, 5, 6, 7})).sync();
        connectFuture.channel().closeFuture().sync();
    }
}
