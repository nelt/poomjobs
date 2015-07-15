package org.codingmatters.poomjobs.engine.inmemory;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactory;
import org.codingmatters.poomjobs.apis.list.JobListService;
import org.codingmatters.poomjobs.apis.queue.JobQueueService;

/**
 * Created by nel on 15/07/15.
 */
public class InMemoryServiceFactory implements ServiceFactory {
    @Override
    public JobQueueService queueServiceueueService(Configuration config) {
        return InMemoryEngine.getEngine(config);
    }

    @Override
    public JobListService listService(Configuration config) {
        return InMemoryEngine.getEngine(config);
    }

}
