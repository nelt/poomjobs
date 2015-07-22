package org.codingmatters.poomjobs.apis.queue;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.codingmatters.poomjobs.test.utils.Helpers.array;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 15/07/15.
 */
public abstract class JobQueueServiceWorkflowAcceptanceTest {

    private JobQueueService queue;
    private UUID uuid;

    protected abstract TestConfigurationProvider getConfigurationProvider();

    @Before
    public void setUp() throws Exception {
        TestConfigurationProvider config = this.getConfigurationProvider();

        this.queue = PoorMansJob.queue(config.getQueueConfig());
        this.uuid = this.queue.submit(JobSubmission.job("job").submission()).getUuid();
    }

    @Test
    public void testSubmitStartDoneWorkflow() throws Exception {
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.queue.start(uuid);
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.RUNNING));

        this.queue.done(uuid, "r", "e", "s");
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.DONE));
        assertThat(this.queue.get(uuid).getResults(), is(array("r", "e", "s")));
    }

    @Test
    public void testSubmitStartFailWorkflow() throws Exception {
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.queue.start(uuid);
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.RUNNING));

        this.queue.fail(uuid, "e", "r", "r");
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.FAILED));
        assertThat(this.queue.get(uuid).getErrors(), is(array("e", "r", "r")));
    }

    @Test
    public void testSubmitStartCancelWorkflow() throws Exception {
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.queue.start(uuid);
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.RUNNING));

        this.queue.cancel(uuid);
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.CANCELED));
    }

    @Test
    public void testSubmitCancelWorkflow() throws Exception {
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.queue.cancel(uuid);
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.CANCELED));
    }
}
