package org.codingmatters.poomjobs.apis.list;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 15/07/15.
 */
public abstract class JobListServiceAcceptanceTest {
    protected abstract Configuration getListServiceConfig() throws ServiceFactoryException;
    protected abstract Configuration getQueueServiceConfig() throws ServiceFactoryException;


    private JobQueueService queue;
    private JobListService service;

    @Before
    public void setUp() throws Exception {
        this.queue = PoorMansJob.queue(getQueueServiceConfig());
        this.service = PoorMansJob.list(getListServiceConfig());
    }


    @Test
    public void testEmptyJobList() throws Exception {
        assertThat(this.service.list(), is(empty()));
    }

    @Test
    public void testOneElementJobList() throws Exception {
        this.queue.submit(JobSubmission.job("job").submission());

        assertThat(this.service.list().size(), is(1));
    }


    @Test
    public void testRetentionDelay() throws Exception {
        UUID uuid = this.queue.submit(JobSubmission.job("job")
                .withRetentionDelay(100L)
                .submission()).getUuid();

        this.queue.start(uuid);
        this.queue.done(uuid);

        assertThat(this.service.list().contains(uuid), is(true));
        Thread.sleep(1000L);
        assertThat(this.service.list().contains(uuid), is(false));
    }
}
