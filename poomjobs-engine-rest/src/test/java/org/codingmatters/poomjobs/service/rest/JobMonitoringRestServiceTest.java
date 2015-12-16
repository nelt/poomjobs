package org.codingmatters.poomjobs.service.rest;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.http.TestUndertowServer;
import org.codingmatters.poomjobs.service.rest.api.JsonJobCodec;
import org.codingmatters.poomjobs.service.rest.api.RestJobStatusChange;
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
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static javax.ws.rs.client.Entity.entity;
import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;
import static org.codingmatters.poomjobs.http.undertow.RestServiceBundle.services;
import static org.codingmatters.poomjobs.http.undertow.RestServiceHandler.from;
import static org.codingmatters.poomjobs.service.rest.PoomjobRestServices.monitoring;
import static org.codingmatters.poomjobs.test.utils.TestHelpers.waitUntil;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
    private JsonJobCodec codec = new JsonJobCodec();

    @Before
    public void setUp() throws Exception {
        Configuration config = defaults(UUID.randomUUID().toString()).config();
        this.queueDeleguate = PoorMansJob.queue(config);
        this.monitoringDeleguate = PoorMansJob.monitor(config);
        this.httpClient = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build();

        this.server.setHandler(
                services().service("/queue", from(monitoring(this.monitoringDeleguate)))
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
        AtomicObjectHolder<RestJobStatusChange> eventJob = new AtomicObjectHolder<>(null);

        EventSource eventSource = EventSource.target(this.serviceTarget.path("/monitoring")).build();
        eventSource.register(
                event -> {
                    String data = null;
                    try {
                        data = new String(event.getRawData(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new AssertionError(e);
                    }

                    if("uuid".equals(event.getName())) {
                        uuid.set(data);
                    } else if("job-status-changed".equals(event.getName())) {
                        try {
                            eventJob.set(this.codec.readStatusChange(data));
                        } catch (IOException e) {
                            throw new AssertionError(e);
                        }
                    } else {
                        fail("unexpected event: " + event);
                    }
                }
        );
        eventSource.open();

        waitUntil(() -> uuid.get(), is(notNullValue()), 1000);

        log.debug("client registered with uuid : {}", uuid.get());

        Job job = this.queueDeleguate.submit(JobSubmission.job("test").submission());

        Response response = this.serviceTarget.path(String.format("/%s/monitor/status", job.getUuid().toString()))
                .request()
                .post(entity("{\"clientUuid\": \"" + uuid.get() + "\"}", "application/json"));

        assertThat(response.getStatusInfo(), is(Response.Status.OK));
        assertThat(this.codec.readJobStatus(response.readEntity(String.class)), is(JobStatus.PENDING));

        this.queueDeleguate.start(job.getUuid());

        waitUntil(() -> eventJob.get(), is(notNullValue()), 1000);

        assertThat(eventJob.get(), is(new RestJobStatusChange(
                job.getStatus(),
                this.queueDeleguate.get(job.getUuid())))
        );
    }
}