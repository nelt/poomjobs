package org.codingmatters.poomjobs.engine.inmemory;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactory;
import org.codingmatters.poomjobs.apis.services.dispatch.JobDispatcherService;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.engine.inmemory.impl.InMemoryEngine;

/**
 * Created by nel on 15/07/15.
 */
public class InMemoryServiceFactory implements ServiceFactory {

    static public final String NAME_OPTION = "name";

    static public Configuration.Builder defaults(String name) {
        return Configuration.defaults(instance).withOption(NAME_OPTION, name);
    }

    static private InMemoryServiceFactory instance = new InMemoryServiceFactory();

    @Override
    public JobQueueService queueService(Configuration config) {
        return InMemoryEngine.getEngine(config);
    }

    @Override
    public JobListService listService(Configuration config) {
        return InMemoryEngine.getEngine(config);
    }

    @Override
    public JobMonitoringService monitoringService(Configuration config) {
        return InMemoryEngine.getEngine(config);
    }

    @Override
    public JobDispatcherService dispatcherService(Configuration config) {
        return InMemoryEngine.getEngine(config);
    }

}
