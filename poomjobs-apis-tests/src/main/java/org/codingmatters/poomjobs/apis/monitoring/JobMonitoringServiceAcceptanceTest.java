package org.codingmatters.poomjobs.apis.monitoring;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 16/07/15.
 */
public abstract class JobMonitoringServiceAcceptanceTest {

    protected abstract Configuration getMonitoringServiceConfig() throws ServiceFactoryException;
    protected abstract Configuration getQueueServiceConfig() throws ServiceFactoryException;

    private JobQueueService queue;
    private JobMonitoringService service;
    private UUID jobUuid;

    @Before
    public void setUp() throws Exception {
        this.queue = PoorMansJob.queue(this.getQueueServiceConfig());
        this.service = PoorMansJob.monitor(this.getMonitoringServiceConfig());
        this.jobUuid = this.queue.submit(JobSubmission.job("job").submission()).getUuid();
    }

    @Test
    public void testInitialStatus() throws Exception {
        assertThat(this.service.monitorStatus(this.jobUuid, (job, old) -> {}), is(JobStatus.PENDING));
    }

    @Test
    public void testMonitor() throws Exception {
        final HashMap<String, JobStatus> change = new HashMap<>();

        this.service.monitorStatus(this.jobUuid, ((job, old) -> {
            change.put("from", old);
            change.put("to", job.getStatus());
        }));

        assertThat(change.get("from"), is(nullValue()));
        assertThat(change.get("to"), is(nullValue()));

        this.queue.start(this.jobUuid);
        assertThat(change.get("from"), is(JobStatus.PENDING));
        assertThat(change.get("to"), is(JobStatus.RUNNING));
    }
}
