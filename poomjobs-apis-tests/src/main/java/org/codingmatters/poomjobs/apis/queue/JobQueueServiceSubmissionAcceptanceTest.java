package org.codingmatters.poomjobs.apis.queue;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.codingmatters.poomjobs.test.utils.TestHelpers.array;
import static org.codingmatters.poomjobs.test.utils.TimeMatchers.near;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 15/07/15.
 */
public abstract class JobQueueServiceSubmissionAcceptanceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private JobQueueService queue;

    protected abstract TestConfigurationProvider getConfigurationProvider();

    @Before
    public void setUp() throws Exception {
        TestConfigurationProvider config = this.getConfigurationProvider();

        this.queue = PoorMansJob.queue(config.getQueueConfig());
    }

    @Test
    public void testSubmit() throws Exception {
        Job job = this.queue.submit(JobSubmission.job("job")
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
    public void testGetSubmitted() throws Exception {
        Job job = this.queue.submit(JobSubmission.job("job")
                .withArguments("arg1", "arg2")
                .withRetentionDelay(12L)
                .submission()
        );

        job = this.queue.get(job.getUuid());

        assertThat(job.getJob(), is("job"));
        assertThat(job.getArguments(), is(array("arg1", "arg2")));
        assertThat(job.getRetentionDelay(), is(12L));
        assertThat(job.getSubmissionTime(), near(now()));
        assertThat(job.getStatus(), is(JobStatus.PENDING));
        assertThat(job.getStartTime(), is(nullValue()));
        assertThat(job.getEndTime(), is(nullValue()));
    }

    @Test
    public void testGetOnNotSubmitted() throws Exception {
        UUID uuid = UUID.randomUUID();

        thrown.expect(NoSuchJobException.class);
        thrown.expectMessage("no such job with uuid=" + uuid.toString());

        this.queue.get(uuid);
    }
}