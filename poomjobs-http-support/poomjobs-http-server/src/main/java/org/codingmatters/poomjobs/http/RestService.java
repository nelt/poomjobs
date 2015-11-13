package org.codingmatters.poomjobs.http;

import org.codingmatters.poomjobs.http.sse.ServerSentEventChannel;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

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

    private final LinkedHashMap<String, RestResource> resources = new LinkedHashMap<>();

    private RestService() {}

    public RestService resource(String name, RestResource resource) {
        this.resources.put(name, resource);
        return this;
    }

    public void forEachResource(BiConsumer<? super String, ? super RestResource> action) {
        resources.forEach(action);
    }

    public RestService serverSentEventChannel(String name, ServerSentEventChannel channel) {
        return this;
    }
}
