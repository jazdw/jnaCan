/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.EnumSet;
import java.util.Properties;

import net.jazdw.jnacan.BcmMessage;
import static net.jazdw.jnacan.BcmMessage.BcmOperation.*;
import static net.jazdw.jnacan.BcmMessage.BcmFlag.*;
import net.jazdw.jnacan.BcmCanSocket;
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
public class BcmTests {
    BcmCanSocket txSocket = new BcmCanSocket();
    RawCanSocket rxSocket = new RawCanSocket();
    static Properties defaultProps = new Properties();
    static Properties testProps;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        InputStream defaultsStream = BcmTests.class.getResourceAsStream("/jnaCan-test-defaults.properties");
        if (defaultsStream == null)
            throw new IOException("Default properties file jnaCan-test-defaults.properties is missing from the classpath");
        
        defaultProps.load(defaultsStream);
        testProps = new Properties(defaultProps);
        
        InputStream overrideStream = BcmTests.class.getResourceAsStream("/jnaCan-test.properties");
        if (overrideStream != null) {
            testProps.load(overrideStream);
        }
    }

    @Rule
    public Timeout globalTimeout = new Timeout(Integer.valueOf(testProps.getProperty("test.timeout")));

    @Before
    public void setUp() throws SocketException {
        txSocket.open();
        rxSocket.open();
        rxSocket.setReceiveTimeout(Integer.valueOf(testProps.getProperty("test.timeout")));
        rxSocket.setTimestampEnabled(true);
        
        CanInterface txIf = new CanInterface(testProps.getProperty("can.txInterface"));
        CanInterface rxIf = new CanInterface(testProps.getProperty("can.rxInterface"));
                
        txSocket.connect(txIf);
        rxSocket.bind(rxIf);
    }

    @After
    public void tearDown() throws SocketException {
        txSocket.close();
        rxSocket.close();
    }

    @Test(timeout = 20000)
    public void bcmSend() throws IOException {
        final int id = 0x300;
        final int frameId = 0x200;
        final int numToReceive = 8;
        
        CanFrame frame1 = new CanFrame(frameId, 0xAA, 0xBB, 0xCC);
        CanFrame frame2 = new CanFrame(frameId, 0, 1, 2, 3, 4, 5, 6, 7);
        
        BcmMessage msg = new BcmMessage();
        msg.setOperation(TX_SETUP);
        msg.setFlags(EnumSet.of(SETTIMER, STARTTIMER, TX_CP_CAN_ID));
        msg.setId(id);
        msg.setCount(numToReceive/2);
        msg.setInterval1(100);
        msg.setInterval2(1000);
        msg.setFrames(frame1, frame2);
        
        txSocket.send(msg);
        
        int i = 0;
        try {
            for (i = 0; i < numToReceive; i++) {
                CanFrame frame = rxSocket.receiveTimestamped();
                if (frame.getId().getId() != id)
                    fail("TX_CP_CAN_ID flag didn't work");
                System.out.println("Read CanFrame " + i + ": " + frame);
            }
        }
        catch (SocketTimeoutException e) {
            fail("Socket read timed out while trying to read " + (i+1) + " of " + numToReceive + " frames");
        }
        
        BcmMessage delete = new BcmMessage(TX_DELETE, id);
        txSocket.send(delete);
        
        // dont set TX_CP_CAN_ID
        msg.setFlags(EnumSet.of(SETTIMER, STARTTIMER));
        txSocket.send(msg);
        
        i = 0;
        try {
            for (i = 0; i < numToReceive; i++) {
                CanFrame frame = rxSocket.receiveTimestamped();
                if (frame.getId().getId() != frameId)
                    fail("Got wrong can ID");
                System.out.println("Read CanFrame " + i + ": " + frame);
            }
        }
        catch (SocketTimeoutException e) {
            fail("Socket read timed out while trying to read " + (i+1) + " of " + numToReceive + " frames");
        }
        
        txSocket.send(delete);
    }
}
