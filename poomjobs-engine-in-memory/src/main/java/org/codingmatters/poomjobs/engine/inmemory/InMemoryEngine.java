package org.codingmatters.poomjobs.engine.inmemory;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.services.dispatch.JobDispatcherService;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.engine.EngineConfiguration;
import org.codingmatters.poomjobs.engine.JobDispatcher;
import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.inmemory.impl.dispatch.InMemoryDispatcher;
import org.codingmatters.poomjobs.engine.inmemory.impl.monitor.InMemoryStatusMonitorer;
import org.codingmatters.poomjobs.engine.inmemory.impl.monitor.StatusMonitorer;
import org.codingmatters.poomjobs.engine.inmemory.impl.store.InMemoryJobStore;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

import static org.codingmatters.poomjobs.apis.jobs.JobBuilders.from;

/**
 * Created by nel on 07/07/15.
 */
public class InMemoryEngine implements Closeable {

    static private final HashMap<String, InMemoryEngine> engines = new HashMap<>();
    private final Configuration config;
    private final EngineConfiguration engineConfiguration;
    private final JobStore store;
    private final StatusMonitorer statusMonitorer = new InMemoryStatusMonitorer();
    private final JobDispatcher dispatcher;

    private final JobQueueService queueService;
    private final JobListService listService;
    private final JobMonitoringService monitoringService;
    private final JobDispatcherService dispatcherService;

    public InMemoryEngine(Configuration config) {
        this.config = config;
        if(config.hasOption(Options.ENGINE_CONFIGURATION)) {
            this.engineConfiguration = (EngineConfiguration) config.getOption(Options.ENGINE_CONFIGURATION);
        } else {
            this.engineConfiguration = EngineConfiguration.defaults().config();
        }

        this.store = new InMemoryJobStore();

        this.queueService = new AbstractJobQueueService(this.store, this.engineConfiguration, this.statusMonitorer);
        this.listService = new AbstractJobListService(this.store);
        this.monitoringService = new AbstractJobMonitoringService(this.store, this.statusMonitorer);

        this.dispatcher = new InMemoryDispatcher(this.store, this.queueService);
        this.dispatcherService = new AbstractJobDispatcherService(this.dispatcher);

        this.store.start();
        this.dispatcher.start();
    }

    static public InMemoryEngine getEngine(Configuration config) {
        synchronized (engines) {
            String name = (String) config.getOption(InMemoryServiceFactory.NAME_OPTION);
            if (!engines.containsKey(name)) {
                engines.put(name, new InMemoryEngine(config));
            }
            return engines.get(name);
        }
    }

    static public void removeEngine(Configuration config) {
        synchronized (engines) {
            String name = (String) config.getOption(InMemoryServiceFactory.NAME_OPTION);
            if (engines.containsKey(name)) {
                engines.remove(name);
            }
        }
    }

    public JobQueueService getJobQueueService() {
        return this.queueService;
    }
    public JobListService getJobListService() {
        return this.listService;
    }
    public JobMonitoringService getJobMonitoringService() {
        return this.monitoringService;
    }
    public JobDispatcherService getJobDispatcherService() {
        return this.dispatcherService;
    }

    @Override
    public void close() throws IOException {
        this.store.stop();
        this.dispatcher.stop();
    }

    public interface Options {
        String ENGINE_CONFIGURATION = "enngine.configuration";
    }
}
