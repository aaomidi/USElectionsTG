package com.aaomidi.primaryelections.util;

/**
 * Created by amir on 2016-02-20.
 */
public class NumberTools {

    public static Integer getInteger(String s) {
        try {
            return Integer.valueOf(s);
        } catch (Exception ex) {
            return null;
        }
    }
}
