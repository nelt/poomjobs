package org.codingmatters.poomjobs.http;

/**
 * Created by nel on 02/11/15.
 */
public interface RestIO {
    RestIO status(RestStatus status);
    RestIO contentType(String type);
    RestIO encoding(String enc);
    RestIO content(String content);
}
