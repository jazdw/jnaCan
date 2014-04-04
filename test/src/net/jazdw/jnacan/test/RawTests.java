/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import net.jazdw.jnacan.CanFilter;
import net.jazdw.jnacan.CanFrame;
import net.jazdw.jnacan.CanInterface;
import net.jazdw.jnacan.RawCanSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public class RawTests {
    RawCanSocket txSocket = new RawCanSocket();
    RawCanSocket rxSocket = new RawCanSocket();
    static Properties defaultProps = new Properties();
    static Properties testProps;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        InputStream defaultsStream = RawTests.class.getResourceAsStream("/jnaCan-test-defaults.properties");
        if (defaultsStream == null)
            throw new IOException("Default properties file jnaCan-test-defaults.properties is missing from the classpath");
        
        defaultProps.load(defaultsStream);
        testProps = new Properties(defaultProps);
        
        InputStream overrideStream = RawTests.class.getResourceAsStream("/jnaCan-test.properties");
        if (overrideStream != null) {
            testProps.load(overrideStream);
        }
    }
    
    @Rule
    public Timeout globalTimeout = new Timeout(Integer.valueOf(testProps.getProperty("test.timeout")));

    @Before
    public void setUp() throws Exception {
        txSocket.open();
        rxSocket.open();
        rxSocket.setReceiveTimeout(Integer.valueOf(testProps.getProperty("test.timeout")));
        
        CanInterface txIf = new CanInterface(testProps.getProperty("can.txInterface"));
        CanInterface rxIf = new CanInterface(testProps.getProperty("can.rxInterface"));
        
        txSocket.setLoopback(rxIf.equals(txIf));
        
        txSocket.bind(txIf);
        rxSocket.bind(rxIf);
    }

    @After
    public void tearDown() throws Exception {
        txSocket.close();
        rxSocket.close();
    }
    
    @Test
    public void checkMtu() throws IOException {
        CanInterface txIf = txSocket.getBoundInterface();
        txIf.resolveMTU(txSocket);
        int mtu = txIf.getMtu();
        if (mtu != Integer.valueOf(testProps.getProperty("can.txInterface.mtu"))) {
            fail("TX interface MTU does not match");
        }
        
        CanInterface rxIf = txSocket.getBoundInterface();
        rxIf.resolveMTU(txSocket);
        mtu = rxIf.getMtu();
        if (mtu != Integer.valueOf(testProps.getProperty("can.rxInterface.mtu"))) {
            fail("RX interface MTU does not match");
        }
    }
    
    @Test
    public void timestampTest() throws IOException {
        rxSocket.setTimestamp(true);
        
        List<CanFrame> frames = new ArrayList<CanFrame>();
        
        frames.add(new CanFrame(0x100, new byte[] {1, 2, 3}));
        frames.add(new CanFrame(0x200, new byte[] {1, 2, 3}));
        frames.add(new CanFrame(0x300, new byte[] {1, 2, 3}));
        
        for (int i = 0; i < frames.size(); i++) {
            txSocket.send(frames.get(i));
            CanFrame read = rxSocket.receiveTimestamped();
            System.out.println("Read CanFrame " + i + ": " + read);
        }
    }
    
    @Test
    public void receiveOwnMsgsTest() throws IOException {
        txSocket.setLoopback(true);
        txSocket.setRecvOwnMsgs(true);
        
        txSocket.setReceiveTimeout(Integer.valueOf(testProps.getProperty("can.receiveTimeout")));
        
        CanFrame txFrame = new CanFrame(0x100, new byte[] {1, 2, 3});
        txSocket.send(txFrame);
        CanFrame rxFrame = txSocket.receive();
        if (!rxFrame.equals(txFrame))
            fail("Received frame does not match what was sent");
        
        txSocket.setRecvOwnMsgs(false);
        txSocket.send(txFrame);
        try {
            rxFrame = txSocket.receive();
        }
        catch (SocketTimeoutException e) {
            // good
            return;
        }
        
        fail("Received frame when we shouldn't have");
    }
    
    @Test
    public void sendReceiveTest() throws IOException {
        Random rnd = new Random();
        CanFrame txFrame = new CanFrame(0x100, new byte[8]);
        CanFrame rxFrame;
        int numPackets = Integer.valueOf(testProps.getProperty("test.txRx.numPackets"));
        
        System.out.println("RX TX test, sending " + numPackets + " frames");
        for (int i = 0; i < numPackets; i++) {
            for (int j = 0; j < 8; j++) {
                txFrame.getData()[j] = (byte) rnd.nextInt();
            }
            txSocket.send(txFrame);
            rxFrame = rxSocket.receive();
            if (!rxFrame.equals(txFrame)) {
                fail("Transmitted: " + txFrame + " Received: " + rxFrame);
            }
        }
    }
    
    @Test
    public void filterTest() throws IOException {
        rxSocket.setFilters(
                new CanFilter(0x100, 0xFFF),
                new CanFilter(0x200, 0xFFF),
                new CanFilter(0x300, 0xFF0));
        
        List<CanFrame> matchFrames = new ArrayList<CanFrame>();
        List<CanFrame> noMatchFrames = new ArrayList<CanFrame>();
        
        matchFrames.add(new CanFrame(0x100, new byte[] {1, 2, 3}));
        matchFrames.add(new CanFrame(0x200, new byte[] {1, 2, 3}));
        matchFrames.add(new CanFrame(0x300, new byte[] {1, 2, 3}));
        matchFrames.add(new CanFrame(0x301, new byte[] {1, 2, 3}));
        matchFrames.add(new CanFrame(0x309, new byte[] {1, 2, 3}));
        matchFrames.add(new CanFrame(0x100, new byte[] {1, 2, 3}));
        matchFrames.add(new CanFrame(0x300, new byte[] {1, 2, 3}));
        matchFrames.add(new CanFrame(0x200, new byte[] {1, 2, 3}));
        matchFrames.add(new CanFrame(0x30F, new byte[] {1, 2, 3}));
        matchFrames.add(new CanFrame(0x305, new byte[] {1, 2, 3}));
        
        noMatchFrames.add(new CanFrame(0x500, new byte[] {1, 2, 3}));
        noMatchFrames.add(new CanFrame(0x0FF, new byte[] {1, 2, 3}));
        noMatchFrames.add(new CanFrame(0x101, new byte[] {1, 2, 3}));
        noMatchFrames.add(new CanFrame(0x10F, new byte[] {1, 2, 3}));
        noMatchFrames.add(new CanFrame(0x202, new byte[] {1, 2, 3}));
        noMatchFrames.add(new CanFrame(0x20E, new byte[] {1, 2, 3}));
        noMatchFrames.add(new CanFrame(0x210, new byte[] {1, 2, 3}));
        noMatchFrames.add(new CanFrame(0x3F0, new byte[] {1, 2, 3}));
        noMatchFrames.add(new CanFrame(0x150, new byte[] {1, 2, 3}));
        noMatchFrames.add(new CanFrame(0x1A0, new byte[] {1, 2, 3}));
        
        for (int i = 0; i < matchFrames.size(); i++) {
            txSocket.send(matchFrames.get(i));
            txSocket.send(noMatchFrames.get(i));
        }
        
        for (int i = 0; i < matchFrames.size(); i++) {
            CanFrame read = rxSocket.receive();
            
            if (!read.equals(matchFrames.get(i))) {
                fail("Match failed" + i + ": " + read);
            }
            else {
                System.out.println("Read CanFrame " + i + ": " + read);
            }
        }
    }
}
