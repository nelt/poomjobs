package org.codingmatters.poomjobs.apis.queue;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
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
    protected abstract Configuration getQueueServiceConfig() throws ServiceFactoryException;

    private JobQueueService service;
    private UUID uuid;

    @Before
    public void setUp() throws Exception {
        this.service = PoorMansJob.queue(this.getQueueServiceConfig());
        this.uuid = this.service.submit(JobSubmission.job("job").submission()).getUuid();
    }

    @Test
    public void testSubmitStartDoneWorkflow() throws Exception {
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.service.start(uuid);
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.RUNNING));

        this.service.done(uuid, "r", "e", "s");
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.DONE));
        assertThat(this.service.get(uuid).getResults(), is(array("r", "e", "s")));
    }

    @Test
    public void testSubmitStartFailWorkflow() throws Exception {
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.service.start(uuid);
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.RUNNING));

        this.service.fail(uuid, "e", "r", "r");
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.FAILED));
        assertThat(this.service.get(uuid).getErrors(), is(array("e", "r", "r")));
    }

    @Test
    public void testSubmitStartCancelWorkflow() throws Exception {
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.service.start(uuid);
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.RUNNING));

        this.service.cancel(uuid);
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.CANCELED));
    }

    @Test
    public void testSubmitCancelWorkflow() throws Exception {
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.service.cancel(uuid);
        assertThat(this.service.get(uuid).getStatus(), is(JobStatus.CANCELED));
    }
}
