package org.codingmatters.poomjobs.http;

import org.codingmatters.poomjobs.http.internal.ResourcesByName;
import org.codingmatters.poomjobs.http.sse.ServerSentEventChannel;

/**
 * Created by nel on 02/11/15.
 */
public class RestService {


    public static RestService service() {
        return new RestService();
    }

    public static RestResource resource() {
        return RestResource.resource();
    }


    private final ResourcesByName resources = new ResourcesByName();

    private RestService() {}

    public RestService resource(String name, RestResource resource) {
        this.resources.add(name, resource);
        return this;
    }

    public RestService serverSentEventChannel(String name, ServerSentEventChannel channel) {
        return this;
    }


    public RestResourceInvocation getMatchingResource(String name) {
        return this.resources.getMatchingResource(name);
    }
}
