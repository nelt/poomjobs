package org.codingmatters.poomjobs.engine.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by nel on 23/08/15.
 */
public class Audit {

    static private final Logger log = LoggerFactory.getLogger("AUDIT");
    static private ErrorAudit ERROR = new ErrorAudit() {

        @Override
        public String log(String msg) {
            String errorPrefix = String.format("[errorId=%s] ", UUID.randomUUID().toString());
            log.error(errorPrefix + msg);
            return errorPrefix;
        }

        @Override
        public String log(String format, Object arg) {
            String errorPrefix = String.format("[errorId=%s] ", UUID.randomUUID().toString());
            log.error(errorPrefix + format, arg);
            return errorPrefix;
        }

        @Override
        public String log(String format, Object arg1, Object arg2) {
            String errorPrefix = String.format("[errorId=%s] ", UUID.randomUUID().toString());
            log.error(errorPrefix + format, arg1, arg2);
            return errorPrefix;
        }

        @Override
        public String log(String format, Object... arguments) {
            String errorPrefix = String.format("[errorId=%s] ", UUID.randomUUID().toString());
            log.error(errorPrefix + format, arguments);
            return errorPrefix;
        }
    };

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

    static public ErrorAudit error() {
        return ERROR;
    }

    public interface ErrorAudit {
        String log(String msg);
        String log(String format, Object arg);
        String log(String format, Object arg1, Object arg2);
        String log(String format, Object... arguments);
    }

}
