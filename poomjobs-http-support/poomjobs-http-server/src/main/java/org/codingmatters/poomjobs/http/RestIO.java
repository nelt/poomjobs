package org.codingmatters.poomjobs.http;

/**
 * Created by nel on 02/11/15.
 */
public interface RestIO extends RestInput {
    RestIO status(RestStatus status);
    RestIO contentType(String type);
    RestIO encoding(String enc);
    RestIO content(String content);
    RestIO header(String name, String value);
}
