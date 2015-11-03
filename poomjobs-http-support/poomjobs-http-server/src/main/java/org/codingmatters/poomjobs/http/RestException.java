package org.codingmatters.poomjobs.http;

/**
 * Created by nel on 02/11/15.
 */
public class RestException extends Exception {
    private final RestStatus status;

    public RestException(RestStatus status, Throwable e) {
        super(status.getMessage(), e);
        this.status = status;
    }

    public RestStatus getStatus() {
        return status;
    }
}
