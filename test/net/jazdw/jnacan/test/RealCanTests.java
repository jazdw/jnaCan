/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

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
public class RealCanTests {
    CanSocket txSocket = new CanSocket();
    CanSocket rxSocket = new CanSocket();
    static Properties testProps = new Properties();

    @BeforeClass
    public static void setUpBeforeClass() {
        try {
            testProps.load(RealCanTests.class.getResourceAsStream("test.properties"));
        } catch (Exception e) {
        }
    }

    @Before
    public void setUp() throws Exception {
        txSocket.openRaw();
        rxSocket.openRaw();
        rxSocket.setReceiveTimeout(Integer.valueOf(testProps.getProperty("rxTimeout", "200")));
        
        CanInterface txIf = new CanInterface(testProps.getProperty("real.txinterface", "can0"));
        CanInterface rxIf = new CanInterface(testProps.getProperty("real.rxinterface", "can1"));
        
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
    public void sendReceiveTest() throws IOException {
        Random rnd = new Random();
        CanFrame txFrame = new CanFrame(0x100, new byte[8]);
        CanFrame rxFrame;
        int numPackets = Integer.valueOf(testProps.getProperty("real.txinterface", "1000"));
        
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
}
