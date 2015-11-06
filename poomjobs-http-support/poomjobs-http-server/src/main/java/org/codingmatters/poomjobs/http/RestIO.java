package org.codingmatters.poomjobs.http;

import java.util.List;
import java.util.Map;

/**
 * Created by nel on 02/11/15.
 */
public interface RestIO {
    RestIO status(RestStatus status);
    RestIO contentType(String type);
    RestIO encoding(String enc);
    RestIO content(String content);

    Map<String, List<String>> parameters();
    Map<String, List<String>> pathParameters();

    byte[] requestContent();

    void header(String name, String value);
}
