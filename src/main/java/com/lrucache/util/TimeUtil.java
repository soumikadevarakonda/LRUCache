package com.lrucache.util;

/**
 * Utility class for time measurements and conversions.
 */
public final class TimeUtil {
    private TimeUtil() {
        // Prevent instantiation
    }

    /**
     * Converts a duration in nanoseconds to milliseconds.
     *
     * @param nanos duration in nanoseconds
     * @return duration in milliseconds
     */
    public static double nanosToMillis(long nanos) {
        return (double) nanos / 1_000_000.0;
    }
}
