package org.codingmatters.poomjobs.apis.list;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.codingmatters.poomjobs.apis.list.JobListMatchers.exactlyUUIDS;
import static org.codingmatters.poomjobs.apis.services.list.ListQuery.limit;
import static org.codingmatters.poomjobs.apis.services.queue.JobSubmission.job;
import static org.codingmatters.poomjobs.test.utils.Helpers.range;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 21/07/15.
 */
public abstract class JoblistServiceQueryAcceptanceTest {

    private JobQueueService queue;
    private JobListService list;

    protected abstract TestConfigurationProvider getConfigurationProvider();

    @Before
    public void setUp() throws Exception {
        TestConfigurationProvider conf = this.getConfigurationProvider();
        conf.initialize();

        this.queue = PoorMansJob.queue(conf.getQueueConfig());
        this.list = PoorMansJob.list(conf.getListConfig());
    }

    @Test
    public void testEmptyQuery() throws Exception {
        this.submitRandomJobs(10);

        JobList result = this.list.list(limit(0).query());

        assertThat(result, is(empty()));
    }

    @Test
    public void testEmptyResults() throws Exception {
        JobList result = this.list.list(limit(5).query());

        assertThat(result, is(empty()));
    }

    @Test
    public void testOneElement() throws Exception {
        UUID[] all = this.submitRandomJobs(1);

        JobList result = this.list.list(limit(5).query());

        assertThat(result, exactlyUUIDS(all[0]));
    }

    @Test
    public void testOutoffRange() throws Exception {
        this.submitRandomJobs(1);

        JobList result = this.list.list(limit(5).withOffset(10).query());

        assertThat(result, is(empty()));
    }

    @Test
    public void testExactlyOnePage() {
        UUID[] all = this.submitRandomJobs(5);

        assertThat(
                this.list.list(limit(5).withOffset(0).query()),
                exactlyUUIDS(all));
    }

    @Test
    public void testExactlyTwoPages() {
        UUID[] all = this.submitRandomJobs(10);

        assertThat(
                this.list.list(limit(5).withOffset(0).query()),
                exactlyUUIDS(range(all, 0, 5)));

        assertThat(
                this.list.list(limit(5).withOffset(5).query()),
                exactlyUUIDS(range(all, 5, 10)));
    }

    @Test
    public void testLessThanAPage() throws Exception {
        UUID[] all = this.submitRandomJobs(3);

        assertThat(
                this.list.list(limit(5).withOffset(0).query()),
                exactlyUUIDS(range(all, 0, 3)));

    }

    @Test
    public void testLessThanTwoPage() throws Exception {
        UUID[] all = this.submitRandomJobs(8);

        assertThat(
                this.list.list(limit(5).withOffset(5).query()),
                exactlyUUIDS(range(all, 5, 8)));

    }





    protected UUID[] submitRandomJobs(int count) {
        ArrayList<UUID> results = new ArrayList<>(count);
        for(int i = 0 ; i < count ; i++) {
            results.add(this.queue.submit(job("job").submission()).getUuid());
        }
        return results.toArray(new UUID[count]);
    }
}
