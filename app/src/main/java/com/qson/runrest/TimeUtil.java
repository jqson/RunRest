package com.qson.runrest;

import java.util.Locale;

public class TimeUtil {

    static String secToTimeString(int second) {
        int sec = second % 60;
        int min = (second / 60) % 60;
        int hour = second / 3600;

        if (hour > 0) {
            return String.format(Locale.US, "%1$d:%2$02d:%3$02d", hour, min, sec);
        } else {
            return String.format(Locale.US, "%1$d:%2$02d", min, sec);
        }
    }
}
