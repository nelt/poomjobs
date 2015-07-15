package org.codingmatters.poomjobs.apis;

import org.codingmatters.poomjobs.apis.list.JobListService;
import org.codingmatters.poomjobs.apis.queue.JobQueueService;
import org.codingmatters.poomjobs.engine.inmemory.InMemoryEngine;

/**
 * Created by nel on 05/07/15.
 */
public class PoorMansJob {

    static public JobQueueService queue(Configuration config) {
        return InMemoryEngine.getEngine(config);
    }

    static public JobListService list(Configuration config) {
        return InMemoryEngine.getEngine(config);
    }

}
