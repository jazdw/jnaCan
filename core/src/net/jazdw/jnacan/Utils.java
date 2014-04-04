/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
package net.jazdw.jnacan;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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
    
    public static timeval millisToTimeval(long millis) {
        timeval time = new timeval();
        
        long usec = millis * 1000;
        long sec = usec / 1000000;
        usec = usec % 1000000;
        
        time.tv_sec = new NativeLong(sec);
        time.tv_usec = new NativeLong(usec);
        return time;
    }
    
    public static long timevalToMillis(timeval time) {
        return microsToMillis(time.tv_sec.longValue(), time.tv_usec.longValue());
    }
    
    public static long microsToMillis(long sec, long usec) {
        return sec * 1000 + usec / 1000;
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
    
    public interface ValueEnum<V> {
        public V value();
    }
    
    public static class ReverseEnumMap<V, E extends Enum<E> & ValueEnum<V>> {
        private Map<V, E> map = new HashMap<V, E>();
        Class<E> valueType;
        
        public ReverseEnumMap(Class<E> valueType) {
            this.valueType = valueType;
            
            for (E e : valueType.getEnumConstants()) {
                map.put(e.value(), e);
            }
        }

        public E get(V v) {
            E enumObject = map.get(v);
            if (enumObject == null)
                throw new IllegalArgumentException("Cant convert " + v.toString() + " to " + valueType.getSimpleName());
            return enumObject;
        }
        
        public static <V, E extends Enum<E> & ValueEnum<V>> ReverseEnumMap<V, E> create(Class<E> clazz) {
            return new ReverseEnumMap<V, E>(clazz);
        }
    }
}
