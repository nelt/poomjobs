package org.codingmatters.poomjobs.http;

import io.undertow.Handlers;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import org.codingmatters.poomjobs.test.utils.ListAddedSemaphore;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.codingmatters.poomjobs.test.utils.TestHelpers.assertOccuresBefore;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by nel on 12/11/15.
 */
public class SSETest {

    public static final int CONNECTION_TIMEOUT = 5000;
    static private Logger log = LoggerFactory.getLogger(HttpServerTest.class);

    @Rule
    public TestUndertowServer server = new TestUndertowServer();
    private ServerSentEventConnectionCallback callback;
    private ListAddedSemaphore<ServerSentEventConnection> connections;
    private ListAddedSemaphore<InboundEvent> events;
    private Client httpClient;

    private final AtomicInteger openConnectionCount = new AtomicInteger(0);

    @Before
    public void setUp() throws Exception {
        this.callback = this::connected;
        this.connections = new ListAddedSemaphore<>();
        this.events = new ListAddedSemaphore<>();

        this.server.setHandler(Handlers.serverSentEvents(this.callback));
        this.httpClient = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build();

        this.openConnectionCount.set(0);
    }

    @After
    public void tearDown() throws Exception {
        this.httpClient.close();
    }

    public void connected(ServerSentEventConnection connection, String lastEventId) {
        this.connections.add(connection);
        this.openConnectionCount.incrementAndGet();
        connection.addCloseTask(channel -> this.connectionClosed(channel));
        log.debug("registered connection : {}", connection);
    }

    public void connectionClosed(ServerSentEventConnection channel) {
        log.info("connection close");
        this.openConnectionCount.decrementAndGet();
    }

    public void collectEvents(WebTarget target) {
        EventSource eventSource = EventSource.target(target).build();
        eventSource.register(inboundEvent -> {
            if (inboundEvent != null) {
                this.events.add(inboundEvent);
                log.debug(inboundEvent.getName() + "; " + inboundEvent.readData(String.class));
            }
        });
        eventSource.open();
    }

    public interface EventInputRequester {
        EventInput request(WebTarget target);
    }

    @Test
    public void testOneGetClient() throws Exception {
        this.collectEvents(httpClient.target(this.server.url("/")));

        this.connections.waitAdded(CONNECTION_TIMEOUT);

        this.connections.elements().get(0).send("yopyop");
        assertThat(this.events.waitAdded(200L).get(0).getRawData(), is("yopyop".getBytes("UTF-8")));
    }


    @Test
    public void testClientClosed() throws Exception {
        this.collectEvents(httpClient.target(this.server.url("/")));
        this.connections.waitAdded(CONNECTION_TIMEOUT);

        log.debug("connection established");

        ServerSentEventConnection connection = this.connections.elements().get(0);
        connection.send("yopyop");
        assertThat(this.events.waitAdded(200L).get(0).getRawData(), is("yopyop".getBytes("UTF-8")));

        assertThat(this.openConnectionCount.get(), is(1));

        assertTrue(this.send(connection, "open"));
        httpClient.close();

        int sentBeforeClosed = 0;
        while(this.send(connection, "closed") && sentBeforeClosed < 100) {
            sentBeforeClosed++;
            assertThat(this.openConnectionCount.get(), is(1));
        }
        log.info("closed after " + sentBeforeClosed + " successful send");
        assertOccuresBefore(() -> (this.openConnectionCount.get() == 0), 100);

    }

    protected boolean send(ServerSentEventConnection connection, String data) throws InterruptedException {
        final AtomicBoolean sent = new AtomicBoolean(false);
        final AtomicBoolean result = new AtomicBoolean(true);
        connection.send(data, new ServerSentEventConnection.EventCallback() {
            @Override
            public void done(ServerSentEventConnection connection, String data, String event, String id) {
                log.debug("sent " + data);
                result.set(true);
                sent.set(true);
            }

            @Override
            public void failed(ServerSentEventConnection connection, String data, String event, String id, IOException e) {
                log.debug("failed sending " + data);
                result.set(false);
                sent.set(true);
            }
        });
        while(! sent.get()) {
            Thread.sleep(10);
        }
        return result.get();
    }
}
