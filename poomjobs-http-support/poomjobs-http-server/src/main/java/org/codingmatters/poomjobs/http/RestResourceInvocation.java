package org.codingmatters.poomjobs.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by nel on 04/11/15.
 */
public class RestResourceInvocation {
    private final Map<String, List<String>> pathParameters;
    private final RestResource resource;

    public RestResourceInvocation(Map<String, List<String>> pathParameters, RestResource resource) {
        this.pathParameters = pathParameters;
        this.resource = resource;
    }
    public RestResourceInvocation(RestResource resource) {
        this(Collections.emptyMap(), resource);
    }

    public Map<String, List<String>> getPathParameters() {
        return pathParameters;
    }

    public RestResource getResource() {
        return resource;
    }
}
