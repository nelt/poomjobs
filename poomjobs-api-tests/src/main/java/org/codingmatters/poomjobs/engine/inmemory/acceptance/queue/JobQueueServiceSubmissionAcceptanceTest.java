package org.codingmatters.poomjobs.engine.inmemory.acceptance.queue;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.junit.Before;
import org.junit.Test;

import static java.time.LocalDateTime.now;
import static org.codingmatters.poomjobs.test.utils.Helpers.array;
import static org.codingmatters.poomjobs.test.utils.TimeMatchers.near;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 15/07/15.
 */
public abstract class JobQueueServiceSubmissionAcceptanceTest {

    protected abstract Configuration getQueueServiceConfig() throws ServiceFactoryException;
    protected abstract Long getExpectedDefaultRetentionDelay();

    private JobQueueService service;

    @Before
    public void setUp() throws Exception {
        this.service = PoorMansJob.queue(getQueueServiceConfig());
    }

    @Test
    public void testSubmit() throws Exception {
        Job job = this.service.submit(JobSubmission.job("job")
                        .withArguments("arg1", "arg2")
                        .withRetentionDelay(12L)
                        .submission()
        );

        assertThat(job.getJob(), is("job"));
        assertThat(job.getArguments(), is(array("arg1", "arg2")));
        assertThat(job.getRetentionDelay(), is(12L));
        assertThat(job.getSubmissionTime(), near(now()));
        assertThat(job.getStatus(), is(JobStatus.PENDING));
        assertThat(job.getStartTime(), is(nullValue()));
        assertThat(job.getEndTime(), is(nullValue()));

    }

    @Test
    public void testSubmitWithDefaultRetentionDelay() throws Exception {
        Job job = this.service.submit(JobSubmission.job("job")
                        .withArguments("arg1", "arg2")
                        .submission()
        );

        assertThat(job.getRetentionDelay(), is(this.getExpectedDefaultRetentionDelay()));
    }


}
