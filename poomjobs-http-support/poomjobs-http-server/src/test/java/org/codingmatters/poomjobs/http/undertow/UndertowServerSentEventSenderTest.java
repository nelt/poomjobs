package org.codingmatters.poomjobs.http.undertow;

import io.undertow.Undertow;
import org.codingmatters.poomjobs.http.sse.ServerSentEvent;
import org.codingmatters.poomjobs.http.sse.ServerSentEventChannel;
import org.codingmatters.poomjobs.http.sse.ServerSentEventClient;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.codingmatters.poomjobs.test.utils.TestHelpers.assertOccuresBefore;
import static org.codingmatters.poomjobs.test.utils.TestHelpers.waitUntil;

/**
 * Created by nel on 12/11/15.
 */
public class UndertowServerSentEventSenderTest {

    static private Logger log = LoggerFactory.getLogger(UndertowServerSentEventSenderTest.class);

    private Undertow server;
    private UndertowServerSentEventSender sseHandler;
    private ServerSentEventChannel sseChannel;

    private List<ServerSentEventClient> clients;

    private Client httpClient;

    @Before
    public void setUp() throws Exception {
        this.clients = Collections.synchronizedList(new LinkedList<>());

        this.sseHandler = new UndertowServerSentEventSender();
        this.sseChannel = ServerSentEventChannel.create()
                .onRegister(client -> this.clients.add(client))
                .onUnregister(client -> this.clients.remove(client))
                .channel();

        this.sseChannel.setServerSetEventSender(this.sseHandler);
        this.sseHandler.setClientManager(this.sseChannel);

        this.server = Undertow.builder()
                .addHttpListener(9999, "localhost")
                .setHandler(this.sseHandler.getHandler())
                .build();
        this.server.start();

        this.httpClient = ClientBuilder.newBuilder().register(SseFeature.class).build();
    }

    @After
    public void tearDown() throws Exception {
        this.httpClient.close();
        this.server.stop();
    }

    @Test
    public void testRegister() throws Exception {
        this.httpClient.target("http://localhost:9999/").request().get();

        assertOccuresBefore(() -> this.clients.size() == 1, 1000);
    }

    @Test
    public void testSend() throws Exception {
        EventInput eventInput = this.httpClient.target("http://localhost:9999/").request().get(EventInput.class);
        waitUntil(() -> this.clients.size() == 1, 1000);

        List<InboundEvent> events = Collections.synchronizedList(new LinkedList<>());

        new Thread(() -> {
            while (!eventInput.isClosed()) {
                final InboundEvent inboundEvent = eventInput.read();
                if (inboundEvent == null) {
                    // connection has been closed
                    break;
                }
                events.add(inboundEvent);
                log.debug(inboundEvent.getName() + "; " + inboundEvent.readData(String.class));
            }
        }).start();

        this.sseChannel.send(
                ServerSentEvent.data("hello world !").withEvent("hello-event").withId(UUID.randomUUID().toString()).event(),
                this.clients.get(0)
        );

        assertOccuresBefore(() -> events.size() == 1, 1000);
    }
}