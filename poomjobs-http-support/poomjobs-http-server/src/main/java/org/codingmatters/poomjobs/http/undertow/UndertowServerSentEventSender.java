package org.codingmatters.poomjobs.http.undertow;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import org.codingmatters.poomjobs.http.sse.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by nel on 12/11/15.
 */
public class UndertowServerSentEventSender implements ServerSetEventSender {
    static private final Logger log = LoggerFactory.getLogger(UndertowServerSentEventSender.class);

    private final Map<ServerSentEventClient, ServerSentEventConnection> clientConnections;

    private final ServerSentEventHandler sseHandler = Handlers.serverSentEvents(this::connected);
    private final HttpHandler handler = new HttpHandler() {
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            if (exchange.isInIoThread()) {
                exchange.dispatch(this);
                return;
            }
            sseHandler.handleRequest(exchange);
        }
    };

    private ServerSentEventClientManager clientManager;

    public UndertowServerSentEventSender() {
        this.clientConnections = Collections.synchronizedMap(new HashMap<>());
    }

    public HttpHandler getHandler() {
        return handler;
    }

    private void connected(ServerSentEventConnection connection, String lastEventId) {
        try {
            ServerSentEventClient client = new UndertowServerSentEventClient(connection);
            connection.addCloseTask(serverSentEventConnection -> this.clientConnections.remove(client));

            this.clientConnections.put(client, connection);
            this.clientManager.clientRegistered(client);

            connection.addCloseTask(connectionChannelListener -> this.unregister(client, connectionChannelListener));
            log.debug("registered connection {} for client {}", connection, client);
        } catch(IOException e) {
            log.error("error creating undertow server sent event client", e);
        }
    }

    private void unregister(ServerSentEventClient client, ServerSentEventConnection connection) {
        this.clientConnections.remove(client);
        this.clientManager.clientUnregistered(client);
        log.debug("unregistered connection {} for client {}", connection, client);
    }

    @Override
    public ServerSentEventFuture sendRequested(ServerSentEventSender sending) {
        ServerSentEvent event = sending.getEvent();
        List<ServerSentEventConnection> connections = this.getClientConnections(sending.getClients());


        ServerSentEventFuture result = new ServerSentEventFuture(connections.size());

        for (ServerSentEventConnection connection : connections) {
            log.debug("sending event {} to {}", event, connection);
            connection.send(event.getData(), event.getEvent(), event.getId(), new ServerSentEventConnection.EventCallback() {
                @Override
                public void done(ServerSentEventConnection connection, String data, String event, String id) {
                    log.debug("sent");
                    result.success();
                }

                @Override
                public void failed(ServerSentEventConnection connection, String data, String event, String id, IOException e) {
                    log.debug("failed");
                    result.failure();
                }
            });
        }
        return result;
    }

    private List<ServerSentEventConnection> getClientConnections(Iterable<ServerSentEventClient> clients) {
        Iterator<ServerSentEventClient> clnts = clients.iterator();
        if(! clnts.hasNext()) {
            return new ArrayList<>(this.clientConnections.values());
        } else {
            List<ServerSentEventConnection> results = new LinkedList<>();
            while(clnts.hasNext()) {
                ServerSentEventClient client = clnts.next();
                if (this.clientConnections.containsKey(client)) {
                    results.add(this.clientConnections.get(client));
                } else {
                    log.error("no connection associated to client");
                }
            }
            return results;
        }

    }

    public void setClientManager(ServerSentEventClientManager clientManager) {
        this.clientManager = clientManager;
    }
}
