package org.codingmatters.poomjobs.engine.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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

    static public String logError(String msg) {
        String errorPrefix = String.format("[errorId=%s] ", UUID.randomUUID().toString());
        log.info(errorPrefix + msg);
        return errorPrefix;
    }

    static public String logError(String format, Object arg) {
        String errorPrefix = String.format("[errorId=%s] ", UUID.randomUUID().toString());
        log.info(errorPrefix + format, arg);
        return errorPrefix;
    }

    static public String logError(String format, Object arg1, Object arg2) {
        String errorPrefix = String.format("[errorId=%s] ", UUID.randomUUID().toString());
        log.info(errorPrefix + format, arg1, arg2);
        return errorPrefix;
    }

    static public String logError(String format, Object... arguments) {
        String errorPrefix = String.format("[errorId=%s] ", UUID.randomUUID().toString());
        log.info(errorPrefix + format, arguments);
        return errorPrefix;
    }

}
