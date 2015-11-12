package org.codingmatters.poomjobs.http.sse;

/**
 * Created by nel on 12/11/15.
 */
public interface ServerSentEventClientManager {
    void clientRegistered(ServerSentEventClient client);
    void clientUnregistered(ServerSentEventClient client);
}
