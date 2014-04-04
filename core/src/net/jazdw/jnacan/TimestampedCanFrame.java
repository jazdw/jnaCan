/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jazdw.jnacan.c.can_frame;
import net.jazdw.jnacan.c.timeval;

/**
 * A can frame with a microsecond resolution timestamp
 * Timestamp is in seconds and microseconds since 1 January 1970
 * 
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class TimestampedCanFrame extends CanFrame {
    long seconds;
    long microseconds;

    public TimestampedCanFrame(can_frame jnaFrame, timeval time) {
        super(jnaFrame);
        this.seconds = time.tv_sec.longValue();
        this.microseconds = time.tv_usec.longValue();
    }
    
    /**
     * Converts to Unix epoc time
     * @return milliseconds since 1 January 1970
     */
    public long toEpocTime() {
        return Utils.microsToMillis(seconds, microseconds);
    }

    @Override
    public String toString() {
        return String.format("%010d.%06d %s", seconds, microseconds, super.toString());
    }
}
