package org.codingmatters.poomjobs.apis.services.monitoring;

import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.queue.NoSuchJobException;

import java.util.UUID;

/**
 * Created by nel on 16/07/15.
 */
public interface JobMonitoringService {
    JobStatus monitorStatus(UUID uuid, StatusChangedMonitor monitor) throws NoSuchJobException;
}
