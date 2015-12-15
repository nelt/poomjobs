package org.codingmatters.poomjobs.http;

import java.util.List;
import java.util.Map;

/**
 * Created by nel on 14/12/15.
 */
public interface RestInput {
    Map<String, List<String>> parameters();
    Map<String, List<String>> pathParameters();

    byte[] requestContent();

}
