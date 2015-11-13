package org.codingmatters.poomjobs.http.undertow;

import io.undertow.Handlers;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import org.codingmatters.poomjobs.http.sse.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by nel on 12/11/15.
 */
public class UndertowServerSentEventHandler implements SendingHandler {
    static private final Logger log = LoggerFactory.getLogger(UndertowServerSentEventHandler.class);

    private final Map<ServerSentEventClient, ServerSentEventConnection> clientConnections;
    private final ServerSentEventHandler handler;

    private ServerSentEventClientManager clientManager;

    public UndertowServerSentEventHandler() {
        this.clientConnections = Collections.synchronizedMap(new HashMap<>());
        this.handler = Handlers.serverSentEvents(this::connected);
    }

    public ServerSentEventHandler getHandler() {
        return handler;
    }

    private void connected(ServerSentEventConnection connection, String lastEventId) {
        ServerSentEventClient client = new UndertowServerSentEventClient(connection);
        connection.addCloseTask(serverSentEventConnection -> this.clientConnections.remove(client));

        this.clientConnections.put(client, connection);
        this.clientManager.clientRegistered(client);
        log.debug("registered connection {} for client {}", connection, client);
    }

    private void unregister(ServerSentEventClient client) {
        this.clientConnections.remove(client);
        this.clientManager.clientUnregistered(client);
    }

    @Override
    public void sendRequested(ServerSentEventSender sending) {
        ServerSentEvent event = sending.getEvent();
        List<ServerSentEventConnection> connections = this.getClientConnections(sending.getClients());
        for (ServerSentEventConnection connection : connections) {
            connection.send(event.getData(), event.getEvent(), event.getId(), null);
        }
    }

    private List<ServerSentEventConnection> getClientConnections(Iterable<ServerSentEventClient> clients) {
        List<ServerSentEventConnection> results = new LinkedList<>();
        for (ServerSentEventClient client : clients) {
            if(this.clientConnections.containsKey(client)) {
                results.add(this.clientConnections.get(client));
            } else {
                log.error("no connection associated to client");
            }
        }

        return results;
    }

    public void setClientManager(ServerSentEventClientManager clientManager) {
        this.clientManager = clientManager;
    }
}
