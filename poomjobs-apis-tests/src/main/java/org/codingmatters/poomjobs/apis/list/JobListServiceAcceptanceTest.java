package org.codingmatters.poomjobs.apis.list;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 15/07/15.
 */
public abstract class JobListServiceAcceptanceTest {

    protected abstract TestConfigurationProvider getConfigurationProvider();

    private JobQueueService queue;
    private JobListService service;

    @Before
    public void setUp() throws Exception {
        TestConfigurationProvider conf = this.getConfigurationProvider();
        conf.initialize();

        this.queue = PoorMansJob.queue(conf.getQueueConfig());
        this.service = PoorMansJob.list(conf.getListConfig());
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

        long waited = 0L;
        while(waited < 10 * 1000 && this.service.list().contains(uuid)) {
            Thread.sleep(100L);
            waited += 100L;
        }
        assertThat(this.service.list().contains(uuid), is(false));
    }
}
