package org.codingmatters.poomjobs.http;

import org.codingmatters.poomjobs.http.internal.ResourcesByName;
import org.codingmatters.poomjobs.http.sse.ServerSentEventChannel;

/**
 * Created by nel on 02/11/15.
 */
public class RestService {


    public static RestService root(String rootPath) {
        return new RestService(rootPath);
    }

    public static RestResource resource() {
        return RestResource.resource();
    }


    private final String rootPath;

    private final ResourcesByName resources = new ResourcesByName();

    private RestService(String rootPath) {
        this.rootPath = rootPath;
    }

    public RestService resource(String name, RestResource resource) {
        this.resources.add(name, resource);
        return this;
    }

    public RestService serverSentEventChannel(String name, ServerSentEventChannel channel) {
        return this;
    }

    public String getRootPath() {
        return rootPath;
    }

    public RestResourceInvocation getMatchingResource(String name) {
        return this.resources.getMatchingResource(name);
    }
}
