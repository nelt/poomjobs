package org.codingmatters.poomjobs.apis.factory;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;

/**
 * Created by nel on 15/07/15.
 */
public interface ServiceFactory {
    JobQueueService queueService(Configuration config);
    JobListService listService(Configuration config);
    JobMonitoringService monitoringService(Configuration config);
}
