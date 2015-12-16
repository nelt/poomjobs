package org.codingmatters.poomjobs.apis.monitoring;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.codingmatters.poomjobs.apis.jobs.JobStatus.PENDING;
import static org.codingmatters.poomjobs.apis.jobs.JobStatus.RUNNING;
import static org.codingmatters.poomjobs.test.utils.TestHelpers.assertBefore;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 16/07/15.
 */
public abstract class JobMonitoringServiceAcceptanceTest {

    private JobQueueService queue;
    private JobMonitoringService monitoring;
    private UUID jobUuid;

    protected abstract TestConfigurationProvider getConfigurationProvider();

    @Before
    public void setUp() throws Exception {
        TestConfigurationProvider config = this.getConfigurationProvider();

        this.queue = PoorMansJob.queue(config.getQueueConfig());
        this.monitoring = PoorMansJob.monitor(config.getMonitorConfig());

        this.jobUuid = this.queue.submit(JobSubmission.job("job").submission()).getUuid();
    }

    @Test
    public void testInitialStatus() throws Exception {
        assertThat(this.monitoring.monitorStatus(this.jobUuid, (job, old) -> {}), is(PENDING));
    }

    @Test(expected = NoSuchJobException.class)
    public void testMonitorUnexistentJob() throws Exception {
        this.monitoring.monitorStatus(UUID.randomUUID(), (job, old) -> {});
    }

        @Test
    public void testMonitor() throws Exception {
        final HashMap<String, JobStatus> change = new HashMap<>();

        this.monitoring.monitorStatus(this.jobUuid, ((job, old) -> {
            change.put("from", old);
            change.put("to", job.getStatus());
        }));

        assertThat(change.get("from"), is(nullValue()));
        assertThat(change.get("to"), is(nullValue()));

        this.queue.start(this.jobUuid);

        assertBefore(() -> change.get("from"), is(PENDING), 1000);
        assertBefore(() -> change.get("to"), is(RUNNING), 1000);
    }


}
