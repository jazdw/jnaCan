/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.test;

import java.io.IOException;
import java.io.InputStream;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

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
public class NettyTests {
    EventLoopGroup group = new OioEventLoopGroup();
    static Properties defaultProps = new Properties();
    static Properties testProps;
    
    @BeforeClass
    public static void setup() throws IOException {
        InputStream defaultsStream = NettyTests.class.getResourceAsStream("/jnaCan-test-defaults.properties");
        if (defaultsStream == null)
            throw new IOException("Default properties file jnaCan-test-defaults.properties is missing from the classpath");
        
        defaultProps.load(defaultsStream);
        testProps = new Properties(defaultProps);
        
        InputStream overrideStream = NettyTests.class.getResourceAsStream("/jnaCan-test.properties");
        if (overrideStream != null) {
            testProps.load(overrideStream);
        }
    }

    @Rule
    public Timeout globalTimeout = new Timeout(Integer.valueOf(testProps.getProperty("test.timeout", "10000")));

    
    @After
    public void shutdown() throws InterruptedException {
        group.shutdownGracefully().sync();
    }
    
    @Test
    public void testWrite() throws InterruptedException {
        final CanFrame first = new CanFrame(0x1A0, new byte[] {1, 2, 3, 4, 5, 6, 7});
        final CanFrame second = new CanFrame(0x300, new byte[] {1, 2, 3, 4, 5, 6, 7});
        
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
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        ctx.writeAndFlush(first);
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        fail(cause.toString());
                    }

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, CanFrame msg) throws Exception {
                        System.out.println(msg);
                        
                        if (msg.equals(first)) {
                            ((CanChannel) ctx.channel()).setOption(
                                    CanChannelOption.FILTERS,
                                    new CanFilter[] {new CanFilter(0x300, 0xF00)});
                            ctx.channel().writeAndFlush(second);
                        }
                        else if (msg.equals(second)) {
                            ctx.channel().close();
                        }
                        else {
                            fail("Received unknown message");
                        }
                    }
                });
            }
        });
        
        CanInterface canIf = new CanInterface(testProps.getProperty("can.txInterface"));
        ChannelFuture connectFuture = b.bind(canIf).sync();
        connectFuture.channel().closeFuture().sync();
    }
}
