package org.codingmatters.poomjobs.service.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.http.TestUndertowServer;
import org.codingmatters.poomjobs.test.utils.TestHelpers;
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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;
import static org.codingmatters.poomjobs.http.undertow.RestServiceBundle.services;
import static org.codingmatters.poomjobs.http.undertow.RestServiceHandler.from;
import static org.codingmatters.poomjobs.service.rest.PoomjobRestServices.monitoringService;

/**
 * Created by nel on 14/12/15.
 */
public class JobMonitoringRestServiceTest {

    static private Logger log = LoggerFactory.getLogger(JobMonitoringRestServiceTest.class);

    @Rule
    public TestUndertowServer server = new TestUndertowServer();
    private JobQueueService queueDeleguate;
    private JobMonitoringService monitoringDeleguate;
    private Client httpClient;
    private WebTarget serviceTarget;

    @Before
    public void setUp() throws Exception {
        Configuration config = defaults(UUID.randomUUID().toString()).config();
        this.queueDeleguate = PoorMansJob.queue(config);
        this.monitoringDeleguate = PoorMansJob.monitor(config);
        this.httpClient = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build();

        this.server.setHandler(
                services().service("/queue", from(monitoringService(this.monitoringDeleguate)))
        );

        this.serviceTarget = this.httpClient.target(this.server.url("/queue/jobs"));
    }

    @After
    public void tearDown() throws Exception {
        this.httpClient.close();
    }

    @Test
    public void testRegisterThenSubmitThenMonitorThenChange() throws Exception {
        AtomicString uuid = new AtomicString(null);
        ObjectMapper mapper = new ObjectMapper();
        EventSource eventSource = EventSource.target(this.serviceTarget.path("/monitoring")).build();
        eventSource.register(
                event -> {
                    log.debug("received event: {}", event);
                    String data = null;
                    try {
                        data = new String(event.getRawData(), "UTF-8");
                        log.debug("event decoded data {}", data);
                    } catch (UnsupportedEncodingException e) {
                        log.error("error reading event data", e);
                        return;
                    }

                    if("uuid".equals(event.getName())) {
                        uuid.set(data);
                    } else if("job-status-changed".equals(event.getName())) {

                    } else {
                        log.error("unexpected event: {}", event);
                    }
                }
        );
        eventSource.open();

        TestHelpers.waitUntil(() -> uuid.get() != null, 1000);

        log.debug("client registered with uuid : {}", uuid.get());

        Job job = this.queueDeleguate.submit(JobSubmission.job("test").submission());

        Response response = this.serviceTarget.path(String.format("/%s/monitor/status", job.getUuid().toString()))
                .request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(
                        String.format("{\"clientUuid\": \"%s\"}", uuid.get()),
                        "application/json"));

        log.debug("STATUS:   {}", response.getStatusInfo());

        log.debug("ENTITY:   {}", response.getEntity().toString());
        log.debug("RESPONSE: {}", response.readEntity(String.class));
    }
}