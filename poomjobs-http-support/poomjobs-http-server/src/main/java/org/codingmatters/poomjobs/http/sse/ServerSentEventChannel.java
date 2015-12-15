package org.codingmatters.poomjobs.http.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ServerSocketChannel;

/**
 * Created by nel on 12/11/15.
 */
public class ServerSentEventChannel implements ServerSentEventClientManager {

    static public Builder create() {
        return new Builder();
    }

    static public class Builder {

        private ClientRegisterer clientRegisterer = (client, channel) -> {};
        private ClientUnregisterer clientUnregisterer = (client, channel) -> {};

        public Builder onRegister(ClientRegisterer clientRegisterer) {
            this.clientRegisterer = clientRegisterer;
            return this;
        }

        public Builder onUnregister(ClientUnregisterer clientUnregisterer) {
            this.clientUnregisterer = clientUnregisterer;
            return this;
        }

        public ServerSentEventChannel channel() {
            return new ServerSentEventChannel(this.clientRegisterer, this.clientUnregisterer);
        }
    }

    static private final Logger log = LoggerFactory.getLogger(ServerSocketChannel.class);

    private ServerSetEventSender serverSetEventSender = ServerSetEventSender.NOOP;

    private final ClientRegisterer clientRegisterer;
    private final ClientUnregisterer clientUnregisterer;

    private ServerSentEventChannel(ClientRegisterer clientRegisterer, ClientUnregisterer clientUnregisterer) {
        this.clientRegisterer = clientRegisterer;
        this.clientUnregisterer = clientUnregisterer;
    }

    public void setServerSetEventSender(ServerSetEventSender serverSetEventSender) {
        this.serverSetEventSender = serverSetEventSender;
    }

    public ServerSentEventFuture send(ServerSentEvent event, ServerSentEventClient ... to) {
        return this.serverSetEventSender.sendRequested(new ServerSentEventSender(event, to));
    }

    @Override
    public void clientRegistered(ServerSentEventClient client) {
        this.clientRegisterer.clientRegistered(client, this);
    }

    @Override
    public void clientUnregistered(ServerSentEventClient client) {
        this.clientUnregisterer.clientUnregistered(client, this);
    }

    public interface ClientRegisterer {
        void clientRegistered(ServerSentEventClient client, ServerSentEventChannel channel);
    }

    public interface ClientUnregisterer {
        void clientUnregistered(ServerSentEventClient client, ServerSentEventChannel channel);
    }
}
