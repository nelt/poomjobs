package org.codingmatters.poomjobs.http.undertow;

import io.undertow.server.handlers.sse.ServerSentEventConnection;
import org.codingmatters.poomjobs.http.RestInput;
import org.codingmatters.poomjobs.http.sse.ServerSentEventClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by nel on 12/11/15.
 */
public class UndertowServerSentEventClient implements ServerSentEventClient {

    private final String uuid = UUID.randomUUID().toString();
    private final RestInput restInput;

    public UndertowServerSentEventClient(ServerSentEventConnection connection) throws IOException {
        this.restInput = new UndertowRestInput(connection.getQueryParameters(), new HashMap<>(), new ByteArrayInputStream(new byte[0]));
    }

    @Override
    public RestInput connectionInput() {
        return this.restInput;
    }

    @Override
    public String uuid() {
        return this.uuid;
    }
}
