package org.codingmatters.poomjobs.apis.jobs.exception;

/**
 * Created by nel on 09/07/15.
 */
public class JobListException extends Exception {
    public JobListException(String message) {
        super(message);
    }

    public JobListException(String message, Throwable cause) {
        super(message, cause);
    }
}
