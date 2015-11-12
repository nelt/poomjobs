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

        private ClientRegisterer clientRegisterer = client -> {};
        private ClientUnregisterer clientUnregisterer = client -> {};

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

    private SendingHandler sendingHandler = SendingHandler.NOOP;

    private final ClientRegisterer clientRegisterer;
    private final ClientUnregisterer clientUnregisterer;

    private ServerSentEventChannel(ClientRegisterer clientRegisterer, ClientUnregisterer clientUnregisterer) {
        this.clientRegisterer = clientRegisterer;
        this.clientUnregisterer = clientUnregisterer;
    }

    public void setSendingHandler(SendingHandler sendingHandler) {
        this.sendingHandler = sendingHandler;
    }

    public void send(ServerSentEvent event, ServerSentEventClient ... to) {
        this.sendingHandler.sendRequested(new ServerSentEventSender(event, to));
    }

    @Override
    public void clientRegistered(ServerSentEventClient client) {
        this.clientRegisterer.clientRegistered(client);
    }

    @Override
    public void clientUnregistered(ServerSentEventClient client) {
        this.clientUnregisterer.clientUnregistered(client);
    }

    public interface ClientRegisterer {
        void clientRegistered(ServerSentEventClient client);
    }

    public interface ClientUnregisterer {
        void clientUnregistered(ServerSentEventClient client);
    }
}
