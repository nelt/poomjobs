package org.codingmatters.poomjobs.http.undertow;

import io.undertow.server.handlers.sse.ServerSentEventConnection;
import org.codingmatters.poomjobs.http.sse.ServerSentEventClient;

/**
 * Created by nel on 12/11/15.
 */
public class UndertowServerSentEventClient implements ServerSentEventClient {
    public UndertowServerSentEventClient(ServerSentEventConnection connection) {
    }
}
