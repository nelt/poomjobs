package org.codingmatters.poomjobs.service.rest.api;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobBuilders;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Test;

import static org.codingmatters.poomjobs.apis.jobs.JobBuilders.from;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 06/11/15.
 */
public class JsonJobCodecTest {

    private JsonJobCodec codec;

    @Before
    public void setUp() throws Exception {
        this.codec = new JsonJobCodec();
    }

    @Test
    public void testJob() throws Exception {
        Job job = JobBuilders.build("a job type")
                .withArguments("a", "b", "c")
                .job();

        assertThat(from(codec.readJob(codec.write(job))), is(from(job)));
    }

    @Test
    public void testJobSubmission() throws Exception {
        JobSubmission submission = JobSubmission
                .job("job")
                .withArguments("a", "b")
                .withRetentionDelay(12L)
                .submission();

        assertThat(codec.readJobSubmission(codec.write(submission)), is(submission));

    }
}