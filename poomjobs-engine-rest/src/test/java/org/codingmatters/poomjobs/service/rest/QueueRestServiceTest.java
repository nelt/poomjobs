package org.codingmatters.poomjobs.service.rest;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.http.RestServiceHandler;
import org.codingmatters.poomjobs.http.TestUndertowServer;
import org.codingmatters.poomjobs.service.rest.api.JsonJobCodec;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.UUID;

import static org.codingmatters.poomjobs.apis.jobs.JobBuilders.from;
import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 05/11/15.
 */
public class QueueRestServiceTest {

    static private Logger log = LoggerFactory.getLogger(QueueRestServiceTest.class);

    @Rule
    public TestUndertowServer server = new TestUndertowServer();
    private HttpClient httpClient;
    private JobQueueService delegate;
    private JsonJobCodec codec = new JsonJobCodec();

    @Before
    public void setUp() throws Exception {
        this.delegate = PoorMansJob.queue(defaults(UUID.randomUUID().toString()).config());
        this.httpClient = new HttpClient();
        this.httpClient.start();
        this.server.setHandler(RestServiceHandler.from(PoomjobRestServices.queueService("/queue", this.delegate)));
    }

    @After
    public void tearDown() throws Exception {
        this.httpClient.stop();
    }

    @Test
    public void testGetJob() throws Exception {
        Job job = this.delegate.submit(JobSubmission.job("j").withArguments("a", "b", "c").submission());
        Job got = this.codec.readJob(
                this.httpClient.GET(this.server.url("/queue/jobs/" + job.getUuid().toString()))
                        .getContentAsString()
        );

        assertThat(from(got), is(from(job)));
    }

    @Test
    public void testSubmitJob() throws Exception {
        StringContentProvider content = new StringContentProvider(
                "application/json",
                this.codec.write(
                        JobSubmission
                                .job("j")
                                .withArguments("a", "b", "c")
                                .submission()
                ),
                Charset.forName("UTF-8"));
        Job got = this.codec.readJob(
                this.httpClient.POST(this.server.url("/queue/jobs"))
                        .content(content)
                        .send()
                        .getContentAsString()
        );

        assertThat(from(got), is(from(this.delegate.get(got.getUuid()))));
    }

    @Test
    public void testStart() throws Exception {
        Job job = this.delegate.submit(JobSubmission.job("j").withArguments("a", "b", "c").submission());

        Job got = this.codec.readJob(this.httpClient.POST(
                this.server.url("/queue/jobs/" + job.getUuid().toString() + "/start"))
                    .send()
                    .getContentAsString()
        );

        assertThat(from(got), is(from(this.delegate.get(got.getUuid()))));
        assertThat(got.getStatus(), is(JobStatus.RUNNING));
    }

    @Test
    public void testCancel() throws Exception {
        Job job = this.delegate.submit(JobSubmission.job("j").withArguments("a", "b", "c").submission());
        this.delegate.start(job.getUuid());

        Job got = this.codec.readJob(this.httpClient.POST(
                this.server.url("/queue/jobs/" + job.getUuid().toString() + "/cancel"))
                .send()
                .getContentAsString()
        );

        assertThat(from(got), is(from(this.delegate.get(job.getUuid()))));
        assertThat(got.getStatus(), is(JobStatus.CANCELED));
    }

    @Test
    public void testDone() throws Exception {
        StringContentProvider content = new StringContentProvider(
                "application/json",
                "[\"result1\", \"result2\"]",
                Charset.forName("UTF-8"));
        Job job = this.delegate.submit(JobSubmission.job("j").withArguments("a", "b", "c").submission());
        this.delegate.start(job.getUuid());

        Job got = this.codec.readJob(this.httpClient.POST(
                this.server.url("/queue/jobs/" + job.getUuid().toString() + "/done"))
                .content(content)
                .send()
                .getContentAsString()
        );

        assertThat(from(got), is(from(this.delegate.get(job.getUuid()))));
        assertThat(got.getStatus(), is(JobStatus.DONE));
        assertThat(got.getResults(), is(array("result1", "result2")));
    }

    @Test
    public void testFail() throws Exception {
        StringContentProvider content = new StringContentProvider(
                "application/json",
                "[\"error1\", \"error2\"]",
                Charset.forName("UTF-8"));
        Job job = this.delegate.submit(JobSubmission.job("j").withArguments("a", "b", "c").submission());
        this.delegate.start(job.getUuid());

        Job got = this.codec.readJob(this.httpClient.POST(
                this.server.url("/queue/jobs/" + job.getUuid().toString() + "/fail"))
                .content(content)
                .send()
                .getContentAsString()
        );

        assertThat(from(got), is(from(this.delegate.get(job.getUuid()))));
        assertThat(got.getStatus(), is(JobStatus.FAILED));
        assertThat(got.getErrors(), is(array("error1", "error2")));
    }

    static private <T> T[] array(T... elements) {
        return elements;
    }

}