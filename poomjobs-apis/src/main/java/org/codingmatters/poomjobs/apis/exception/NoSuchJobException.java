package org.codingmatters.poomjobs.apis.exception;

/**
 * Created by nel on 07/07/15.
 */
public class NoSuchJobException extends ServiceException {
    public NoSuchJobException(String msg) {
        super(msg);
    }
}
