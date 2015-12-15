package org.codingmatters.poomjobs.http;

/**
 * Created by nel on 02/11/15.
 */
public class RestException extends Exception {
    private final RestStatus status;
    private final String content;

    public RestException(RestStatus status, Throwable e) {
        this(status, null, e);
    }

    public RestException(RestStatus status, String content, Throwable e) {
        super(status.getMessage(), e);
        this.content = content;
        this.status = status;
    }

    public RestException(RestStatus status, String content) {
        this.content = content;
        this.status = status;
    }

    public RestStatus getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }
}
