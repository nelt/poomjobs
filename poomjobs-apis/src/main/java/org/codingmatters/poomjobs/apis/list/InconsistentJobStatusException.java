package org.codingmatters.poomjobs.apis.list;

/**
 * Created by nel on 07/07/15.
 */
public class InconsistentJobStatusException extends Exception {
    public InconsistentJobStatusException(String message) {
        super(message);
    }
}
