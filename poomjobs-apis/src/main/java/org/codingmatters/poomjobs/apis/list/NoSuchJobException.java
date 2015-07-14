package org.codingmatters.poomjobs.apis.list;

/**
 * Created by nel on 07/07/15.
 */
public class NoSuchJobException extends Exception {
    public NoSuchJobException(String msg) {
        super(msg);
    }
}
