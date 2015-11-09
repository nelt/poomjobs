package org.codingmatters.poomjobs.apis.queue;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

/**
 * Created by nel on 09/11/15.
 */
public abstract class JobQueueServiceRetentionDelayAcceptanceTest {

    private JobQueueService queue;

    protected abstract TestConfigurationProvider getConfigurationProvider();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        TestConfigurationProvider config = this.getConfigurationProvider();

        this.queue = PoorMansJob.queue(config.getQueueConfig());
    }

    @Test
    public void testRetention() throws Exception {
        UUID uuid = this.queue.submit(JobSubmission.job("job")
                .withRetentionDelay(1000L)
                .submission()).getUuid();

        this.queue.start(uuid);
        this.queue.done(uuid);

        this.queue.get(uuid);

        Thread.sleep(500);
        this.queue.get(uuid);

        Thread.sleep(700);
        thrown.expect(NoSuchJobException.class);
        thrown.expectMessage("no such job with uuid=" + uuid.toString());
        this.queue.get(uuid);
    }
}
