package org.codingmatters.poomjobs.apis.list;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.codingmatters.poomjobs.apis.Configuration.defaults;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 06/07/15.
 */
public class JobListServiceTest {

    private JobListService service;

    @Before
    public void setUp() throws Exception {
        this.service = PoorMansJob.list(defaults().config());
    }


    @Test
    public void testEmptyJobList() throws Exception {
        assertThat(this.service.list(), is(empty()));
    }

    @Test
    public void testOneElementJobList() throws Exception {
        this.service.submit(JobSubmission.job("job").submission());

        assertThat(this.service.list().size(), is(1));
    }


    @Test
    public void testRetentionDelay() throws Exception {
        UUID uuid = this.service.submit(JobSubmission.job("job")
                .withRetentionDelay(100L)
                .submission()).getUuid();

        this.service.start(uuid);
        this.service.done(uuid);

        assertThat(this.service.list().contains(uuid), is(true));
        Thread.sleep(500L);
        assertThat(this.service.list().contains(uuid), is(false));
    }
}