package org.codingmatters.poomjobs.http;

/**
 * Created by nel on 02/11/15.
 */
public enum RestStatus {
    OK(200, null),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed for Resource"),
    SERVICE_NOT_FOUND(404, "Service Not Found"),
    RESOURCE_NOT_FOUND(404, "Resource Not Found");

    private final int httpStatus;
    private final String message;

    RestStatus(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
