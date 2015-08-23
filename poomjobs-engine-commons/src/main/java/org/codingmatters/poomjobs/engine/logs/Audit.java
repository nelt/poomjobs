package org.codingmatters.poomjobs.engine.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nel on 23/08/15.
 */
public class Audit {

    static private final Logger log = LoggerFactory.getLogger("AUDIT");

    static public void log(String msg) {
        log.info(msg);
    }

    static public void log(String format, Object arg) {
        log.info(format, arg);
    }

    static public void log(String format, Object arg1, Object arg2) {
        log.info(format, arg1, arg2);
    }

    static public void log(String format, Object... arguments) {
        log.info(format, arguments);
    }

    static public void log(String msg, Throwable t) {
        log.info(msg, t);
    }
//
//    void info(String var1, Object var2, Object var3);
//
//    void info(String var1, Object... var2);
//
//    void info(String var1, Throwable var2);

}
