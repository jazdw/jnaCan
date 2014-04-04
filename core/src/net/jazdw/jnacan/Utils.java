/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;

import net.jazdw.jnacan.c.timeval;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    
    private Utils() {
    }
    
    public static timeval msToTimeval(long ms) {
        timeval time = new timeval();
        
        long usec = ms * 1000;
        long sec = usec / 1000000;
        usec = usec % 1000000;
        
        time.tv_sec = new NativeLong(sec);
        time.tv_usec = new NativeLong(usec);
        return time;
    }
    
    public static byte[] stringToFixedLengthByteArray(String s, int length) {
        byte[] strBytes;
        try {
            strBytes = s.getBytes(Native.getDefaultStringEncoding());
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Native String encoding not supported, falling back to Java default encoding");
            strBytes = s.getBytes();
        }
        
        if (strBytes.length >= length)
            throw new IllegalArgumentException("String too long to fit in byte array of length " + length);
        byte[] ret = new byte[length];
        System.arraycopy(strBytes, 0, ret, 0, strBytes.length);
        return ret;
    }
}
