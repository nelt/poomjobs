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

    @Before
    public void setUp() throws Exception {
        this.queue = PoorMansJob.queue(this.getQueueServiceConfig());
        this.service = PoorMansJob.monitoring(this.getMonitoringServiceConfig());
    }

    @Test
    public void testStatusMonitoring() throws Exception {
        UUID uuid = this.queue.submit(JobSubmission.job("job").submission()).getUuid();

        final HashMap<String, JobStatus> change = new HashMap<>();

        JobStatus startStatus = this.service.monitorStatus(uuid, ((job, old) -> {
            change.put("from", old);
            change.put("to", job.getStatus());
        }));

        assertThat(startStatus, is(JobStatus.PENDING));
        assertThat(change.get("from"), is(nullValue()));
        assertThat(change.get("to"), is(nullValue()));

        this.queue.start(uuid);
        assertThat(change.get("from"), is(JobStatus.PENDING));
        assertThat(change.get("to"), is(JobStatus.RUNNING));

        this.queue.done(uuid);
        assertThat(change.get("from"), is(JobStatus.RUNNING));
        assertThat(change.get("to"), is(JobStatus.DONE));
    }
}
