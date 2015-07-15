package org.codingmatters.poomjobs.apis;

import org.codingmatters.poomjobs.apis.list.JobListService;
import org.codingmatters.poomjobs.apis.queue.JobQueueService;

/**
 * Created by nel on 05/07/15.
 */
public class PoorMansJob {

    static public JobQueueService queue(Configuration config) {
        return config.getServiceFactory().queueServiceueueService(config);
    }

    static public JobListService list(Configuration config) {
        return config.getServiceFactory().listService(config);
    }

}
