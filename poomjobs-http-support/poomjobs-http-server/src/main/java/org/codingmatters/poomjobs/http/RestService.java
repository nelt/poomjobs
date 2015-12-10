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

    public static ServerSentEventChannel.Builder sseChannel() {
        return ServerSentEventChannel.create();
    }

    private final LinkedHashMap<String, RestResource> resources = new LinkedHashMap<>();
    private final LinkedHashMap<String, ServerSentEventChannel> sseChannels = new LinkedHashMap<>();

    private RestService() {}

    public RestService resource(String path, RestResource resource) {
        this.resources.put(path, resource);
        return this;
    }
    public RestService serverSentEventChannel(String path, ServerSentEventChannel channel) {
        this.sseChannels.put(path, channel);
        return this;
    }

    public void forEachResource(BiConsumer<? super String, ? super RestResource> action) {
        resources.forEach(action);
    }

    public void forEachSSEChannel(BiConsumer<? super String, ? super ServerSentEventChannel> action) {
        sseChannels.forEach(action);
    }

}
