package org.codingmatters.poomjobs.http;

import io.undertow.Undertow;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import org.codingmatters.poomjobs.test.utils.ListAddedSemaphore;
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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 12/11/15.
 */
public class SSETest {

    public static final int CONNECTION_TIMEOUT = 5000;
    static private Logger log = LoggerFactory.getLogger(HttpServerTest.class);

    private Undertow server;
    private ServerSentEventConnectionCallback callback;
    private ListAddedSemaphore<ServerSentEventConnection> connections;
    private ListAddedSemaphore<InboundEvent> events;
    private Client httpClient;

    @Before
    public void setUp() throws Exception {
        this.connections = new ListAddedSemaphore<>();
        this.events = new ListAddedSemaphore<>();

        this.callback = this::connected;
        this.server = Undertow.builder()
                .addHttpListener(9999, "localhost")
                .setHandler(new ServerSentEventHandler(this.callback))
                .build();
        this.server.start();

        this.httpClient = ClientBuilder.newBuilder().register(SseFeature.class).build();
    }

    @After
    public void tearDown() throws Exception {
        this.httpClient.close();
        this.server.stop();
    }

    public void connected(ServerSentEventConnection connection, String lastEventId) {
        this.connections.add(connection);
        log.debug("registered connection : {}", connection);
    }

    public void collectEvents(WebTarget target, EventInputRequester requester) {
        new Thread(() -> {
            EventInput eventInput = requester.request(target);
            while (!eventInput.isClosed()) {
                final InboundEvent inboundEvent = eventInput.read();
                if (inboundEvent == null) {
                    // connection has been closed
                    break;
                }
                this.events.add(inboundEvent);
                log.debug(inboundEvent.getName() + "; " + inboundEvent.readData(String.class));
            }
        }).start();
    }

    public interface EventInputRequester {
        EventInput request(WebTarget target);
    }

    @Test
    public void testOneGetClient() throws Exception {
        this.collectEvents(
                httpClient.target("http://localhost:9999/"),
                target -> target.request().get(EventInput.class));

        this.connections.waitAdded(CONNECTION_TIMEOUT);

        this.connections.elements().get(0).send("yopyop");
        assertThat(this.events.waitAdded(200L).get(0).getRawData(), is("yopyop".getBytes("UTF-8")));
    }

    @Test
    public void testOnePostClient() throws Exception {
        Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
        this.collectEvents(
                client.target("http://localhost:9999/"),
                target -> target.request().post(Entity.entity("{\"yop\": \"yop\"}", "application/json"), EventInput.class));

        this.connections.waitAdded(CONNECTION_TIMEOUT);

        this.connections.elements().get(0).send("yopyop");
        assertThat(this.events.waitAdded(200L).get(0).getRawData(), is("yopyop".getBytes("UTF-8")));
    }

    @Test
    public void testOnePutClient() throws Exception {
        Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
        this.collectEvents(
                client.target("http://localhost:9999/"),
                target -> target.request().put(Entity.entity("{\"yop\": \"yop\"}", "application/json"), EventInput.class));

        this.connections.waitAdded(CONNECTION_TIMEOUT);

        this.connections.elements().get(0).send("yopyop");
        assertThat(this.events.waitAdded(200L).get(0).getRawData(), is("yopyop".getBytes("UTF-8")));
    }

    @Test
    public void testClientsWithDifferentMethods() throws Exception {
        Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
        this.collectEvents(
                client.target("http://localhost:9999/"),
                target -> target.request().get(EventInput.class));
        this.collectEvents(
                client.target("http://localhost:9999/"),
                target -> target.request().post(Entity.entity("{\"yop\": \"yop\"}", "application/json"), EventInput.class));
        this.collectEvents(
                client.target("http://localhost:9999/"),
                target -> target.request().put(Entity.entity("{\"yop\": \"yop\"}", "application/json"), EventInput.class));

        this.connections.waitAdded(3, CONNECTION_TIMEOUT);

        for (ServerSentEventConnection connection : this.connections.elements()) {
            connection.send("yopyop");
        }


        Thread.sleep(1000L);
        assertThat(this.events.elements().size(), is(3));
    }
}
