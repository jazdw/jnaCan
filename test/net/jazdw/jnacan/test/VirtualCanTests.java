/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.jazdw.jnacan.CanFilter;
import net.jazdw.jnacan.CanFrame;
import net.jazdw.jnacan.CanInterface;
import net.jazdw.jnacan.CanSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public class VirtualCanTests {
    CanSocket socket = new CanSocket();
    Properties testProps = new Properties();

    @BeforeClass
    public void setup() throws IOException {
        testProps.load(getClass().getResourceAsStream("test.properties"));
    }
    
    @Before
    public void setUp() throws Exception {
        socket.openRaw();
        
        CanInterface canIf = new CanInterface(testProps.getProperty("virtual.interface", "vcan0"));
        socket.bind(canIf);
    }

    @After
    public void tearDown() throws Exception {
        socket.close();
    }
    
    @Test
    public void checkMtu() throws IOException {
        CanInterface canIf = socket.getBoundInterface();
        canIf.resolveMTU(socket);
        if (canIf.getMtu() != 16) {
            fail("MTU is not 16");
        }
    }
    
    @Test
    public void timestampTest() throws IOException {
        socket.setTimestamp(true);
        
        List<CanFrame> frames = new ArrayList<CanFrame>();
        
        frames.add(new CanFrame(0x100, new byte[] {1, 2, 3}));
        frames.add(new CanFrame(0x200, new byte[] {1, 2, 3}));
        frames.add(new CanFrame(0x300, new byte[] {1, 2, 3}));
        
        for (int i = 0; i < frames.size(); i++) {
            socket.send(frames.get(i));
            CanFrame read = socket.receiveTimestamped();
            System.out.println("Read CanFrame " + i + ": " + read);
        }
    }
    
    @Test
    public void filterTest() throws IOException {
        socket.setLoopback(true);
        socket.setRecvOwnMsgs(true);

        socket.setFilters(
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
            socket.send(matchFrames.get(i));
            socket.send(noMatchFrames.get(i));
        }
        
        for (int i = 0; i < matchFrames.size(); i++) {
            CanFrame read = socket.receive();
            
            if (!read.equals(matchFrames.get(i))) {
                fail("Match failed" + i + ": " + read);
            }
            else {
                System.out.println("Read CanFrame " + i + ": " + read);
            }
        }
    }
}
