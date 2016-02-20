package com.aaomidi.primaryelections.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by amir on 2016-02-20.
 */
public class Log {
    private static final Logger logger = Logger.getLogger("USElections");

    public static void log(Level level, Object o, Object... args) {
        logger.log(level, o.toString(), args);
    }
}
