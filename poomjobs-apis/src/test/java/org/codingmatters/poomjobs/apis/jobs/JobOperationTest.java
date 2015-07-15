package org.codingmatters.poomjobs.apis.jobs;

import org.codingmatters.poomjobs.apis.jobs.exception.InconsistentJobStatusException;
import org.junit.Test;

import static java.time.LocalDateTime.now;
import static org.codingmatters.poomjobs.apis.jobs.JobBuilders.from;
import static org.codingmatters.poomjobs.apis.jobs.JobOperation.CANCEL;
import static org.codingmatters.poomjobs.apis.jobs.JobOperation.FAIL;
import static org.codingmatters.poomjobs.apis.jobs.JobOperation.STOP;
import static org.codingmatters.poomjobs.apis.jobs.JobStatus.*;
import static org.codingmatters.poomjobs.test.utils.TimeMatchers.near;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 07/07/15.
 */
public class JobOperationTest {

    @Test
    public void testStart() throws Exception {
        Job job = JobBuilders
                .build("job")
                .withStatus(PENDING)
                .withSubmissionTime(now().minusMinutes(5))
                .job();

        job = JobOperation.START.operate(job);

        assertThat(job.getStatus(), is(RUNNING));
        assertThat(job.getStartTime(), near(now()));
        assertThat(job.getEndTime(), is(nullValue()));
    }

    @Test
    public void testStop() throws Exception {
        Job job = JobBuilders
                .build("job")
                .withStatus(RUNNING)
                .withSubmissionTime(now().minusMinutes(5))
                .withStartTime(now().minusMinutes(4))
                .job();

        job = STOP.operate(job);

        assertThat(job.getStatus(), is(DONE));
        assertThat(job.getEndTime(), is(near(now())));
    }

    @Test
    public void testFail() throws Exception {
        Job job = JobBuilders
                .build("job")
                .withStatus(RUNNING)
                .withSubmissionTime(now().minusMinutes(5))
                .withStartTime(now().minusMinutes(4))
                .job();

        job = FAIL.operate(job);

        assertThat(job.getStatus(), is(FAILED));
        assertThat(job.getEndTime(), is(near(now())));
    }

    @Test
    public void testCancel() throws Exception {
        Job job = JobBuilders
                .build("job")
                .withStatus(RUNNING)
                .withSubmissionTime(now().minusMinutes(5))
                .withStartTime(now().minusMinutes(4))
                .job();

        job = CANCEL.operate(job);

        assertThat(job.getStatus(), is(CANCELED));
        assertThat(job.getEndTime(), is(near(now())));
    }

    @Test(expected = InconsistentJobStatusException.class)
    public void testStartFailureOnRunningJob() throws Exception {
        Job job = JobBuilders
                .build("job")
                .withStatus(RUNNING)
                .withSubmissionTime(now().minusMinutes(5))
                .job();

        JobOperation.START.operate(job);
    }

    @Test(expected = InconsistentJobStatusException.class)
    public void testStartFailureOnDoneJob() throws Exception {
        Job job = JobBuilders
                .build("job")
                .withStatus(DONE)
                .withSubmissionTime(now().minusMinutes(5))
                .job();

        JobOperation.START.operate(job);
    }

    @Test(expected = InconsistentJobStatusException.class)
    public void testStartFailureOnCancelledJob() throws Exception {
        Job job = JobBuilders
                .build("job")
                .withStatus(DONE)
                .withSubmissionTime(now().minusMinutes(5))
                .job();

        JobOperation.START.operate(job);
    }

    @Test
    public void testAdditionalMutation() throws Exception {
        Job job = JobBuilders
                .build("job")
                .withStatus(PENDING)
                .withRetentionDelay(12L)
                .withSubmissionTime(now().minusMinutes(5))
                .job();

        job = JobOperation.START.operate(job, j -> from(j).withRetentionDelay(18L).job());

        assertThat(job.getStatus(), is(RUNNING));
        assertThat(job.getRetentionDelay(), is(18L));
    }
}