package org.codingmatters.poomjobs.http.sse;

import java.util.Arrays;

/**
 * Created by nel on 12/11/15.
 */
public class ServerSentEventSender {
    private final ServerSentEvent event;
    private final ServerSentEventClient[] clients;

    public ServerSentEventSender(ServerSentEvent event, ServerSentEventClient[] clients) {
        this.event = event;
        this.clients = clients;
    }

    public ServerSentEvent getEvent() {
        return event;
    }

    public Iterable<ServerSentEventClient> getClients() {
        return Arrays.asList(clients);
    }
}
