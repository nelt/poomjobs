package org.codingmatters.poomjobs.http;

/**
 * Created by nel on 02/11/15.
 */
public interface RestMethodHandler {
    RestMethodHandler METHOD_NOT_ALLOWED = io -> io.status(RestStatus.METHOD_NOT_ALLOWED);

    void handle(RestIO io) throws RestException;
}
