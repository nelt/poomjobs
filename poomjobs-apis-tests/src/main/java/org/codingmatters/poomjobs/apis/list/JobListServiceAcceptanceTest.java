package org.codingmatters.poomjobs.apis.list;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.codingmatters.poomjobs.apis.services.list.ListQuery.limit;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 15/07/15.
 */
public abstract class JobListServiceAcceptanceTest {

    public static final ListQuery FIRST_100 = limit(100).query();
    private JobQueueService queue;
    private JobListService list;

    protected abstract TestConfigurationProvider getConfigurationProvider();

    @Before
    public void setUp() throws Exception {
        TestConfigurationProvider conf = this.getConfigurationProvider();

        this.queue = PoorMansJob.queue(conf.getQueueConfig());
        this.list = PoorMansJob.list(conf.getListConfig());
    }


    @Test
    public void testEmptyJobList() throws Exception {
        assertThat(this.list.list(FIRST_100), is(empty()));
    }

    @Test
    public void testOneElementJobList() throws Exception {
        this.queue.submit(JobSubmission.job("job").submission());

        assertThat(this.list.list(FIRST_100).size(), is(1));
    }


    @Test
    public void testRetentionDelay() throws Exception {
        UUID uuid = this.queue.submit(JobSubmission.job("job")
                .withRetentionDelay(100L)
                .submission()).getUuid();

        this.queue.start(uuid);

        this.queue.done(uuid);
        assertThat(this.list.list(FIRST_100).contains(uuid), is(true));

        long waited = 0L;
        while(waited < 10 * 1000 && this.list.list(FIRST_100).contains(uuid)) {
            Thread.sleep(100L);
            waited += 100L;
        }
        assertThat(this.list.list(FIRST_100).contains(uuid), is(false));
    }
}
