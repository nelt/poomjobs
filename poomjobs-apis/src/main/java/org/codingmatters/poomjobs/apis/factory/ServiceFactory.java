package org.codingmatters.poomjobs.apis.factory;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.list.JobListService;
import org.codingmatters.poomjobs.apis.queue.JobQueueService;

/**
 * Created by nel on 15/07/15.
 */
public interface ServiceFactory {
    JobQueueService queueServiceueueService(Configuration config);
    JobListService listService(Configuration config);
}
