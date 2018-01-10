package com.github.teocci.opuslib.model;

import java.io.Serializable;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Jan-02
 */
public class AudioTime implements Serializable
{
    private String format = "%02d:%02d:%02d";

    private int hour = 0;
    private int minute = 0;
    private int second = 0;

    public AudioTime() {}

    public AudioTime(long seconds)
    {
        setTimeInSecond(seconds);
    }

    /**
     * get time in the format of "HH:MM:SS"
     *
     * @return string
     */
    public String getTime()
    {
        return String.format(format, hour, minute, second);
    }

    public void setTimeInSecond(long seconds)
    {
        second = (int) (seconds % 60);
        long m = seconds / 60;
        minute = (int) (m % 60);
        hour = (int) (m / 60);
    }

    public void add(int seconds)
    {
        second += seconds;
        if (second >= 60) {
            second %= 60;
            minute++;

            if (minute >= 60) {
                minute %= 60;
                hour++;
            }
        }
    }
}
