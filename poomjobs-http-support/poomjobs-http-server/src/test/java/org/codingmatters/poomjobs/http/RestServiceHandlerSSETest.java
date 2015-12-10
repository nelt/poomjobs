package org.codingmatters.poomjobs.http;

import org.codingmatters.poomjobs.http.sse.ServerSentEventChannel;
import org.glassfish.jersey.media.sse.EventSource;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.codingmatters.poomjobs.http.RestService.sseChannel;
import static org.codingmatters.poomjobs.http.sse.ServerSentEvent.data;
import static org.codingmatters.poomjobs.http.undertow.RestServiceBundle.services;
import static org.codingmatters.poomjobs.http.undertow.RestServiceHandler.from;
import static org.codingmatters.poomjobs.test.utils.TestHelpers.assertOccuresBefore;
import static org.codingmatters.poomjobs.test.utils.TestHelpers.waitUntil;

/**
 * Created by nel on 12/11/15.
 */
public class RestServiceHandlerSSETest {

    static private Logger log = LoggerFactory.getLogger(HttpServerTest.class);

    @Rule
    public TestUndertowServer server = new TestUndertowServer();
    private Client httpClient;

    @Before
    public void setUp() throws Exception {
        this.httpClient = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build();
    }

    @After
    public void tearDown() throws Exception {
        this.httpClient.close();
    }

    @Test
    public void testSend() throws Exception {
        AtomicBoolean registered = new AtomicBoolean(false);
        List<String> events = Collections.synchronizedList(new LinkedList<>());
        ServerSentEventChannel channel = sseChannel()
                .onRegister(client -> registered.set(true))
                .channel();

        this.server.setHandler(services().service("/service", from(RestService.service()
                .serverSentEventChannel("sse", channel)
        )));

        EventSource eventSource = EventSource.target(this.httpClient.target(this.server.url("/service/sse"))).build();
        eventSource.register(
                inboundEvent ->
                        events.add(inboundEvent.readData())
        );
        eventSource.open();
        waitUntil(() -> registered.get(), 1000);

        channel.send(data("yop").event()).get();
        assertOccuresBefore(() -> events.contains("yop"), 1000);
    }

    @Test
    public void testRegister() throws Exception {
        AtomicBoolean registered = new AtomicBoolean(false);

        ServerSentEventChannel channel = sseChannel()
                .onRegister(client -> registered.set(true))
                .channel();
        this.server.setHandler(services().service("/service", from(RestService.service()
                .serverSentEventChannel(
                        "sse",
                        channel)
        )));

        WebTarget target = this.httpClient.target(this.server.url("/service/sse"));

        EventSource eventSource = EventSource.target(target).build();
        eventSource.open();

        assertOccuresBefore(() -> registered.get(), 1000);
    }

    @Test(timeout = 5000)
    public void testUnregister() throws Exception {
        AtomicBoolean unregistered = new AtomicBoolean(false);
        ServerSentEventChannel channel = sseChannel()
                .onUnregister(client -> unregistered.set(true))
                .channel();

        this.server.setHandler(services().service("/service", from(RestService.service()
                .serverSentEventChannel("sse", channel)
        )));

        EventSource eventSource = EventSource.target(this.httpClient.target(this.server.url("/service/sse"))).build();
        eventSource.open();

        this.httpClient.close();
        while(channel.send(data("closed ?").event()).get().getFailureCount() != 1) {Thread.sleep(10);}

        assertOccuresBefore(() -> unregistered.get(), 100);
    }

}
