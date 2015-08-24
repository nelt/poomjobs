package org.codingmatters.poomjobs.apis.exception;

/**
 * Created by nel on 07/07/15.
 */
public class InconsistentJobStatusException extends ServiceException {
    public InconsistentJobStatusException(String message) {
        super(message);
    }
}
