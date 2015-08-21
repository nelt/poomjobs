package org.codingmatters.poomjobs.engine.inmemory;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.jobs.*;
import org.codingmatters.poomjobs.apis.jobs.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.services.dispatch.JobDispatcherService;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.apis.services.queue.NoSuchJobException;
import org.codingmatters.poomjobs.engine.EngineConfiguration;
import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.inmemory.impl.dispatch.InMemoryDispatcher;
import org.codingmatters.poomjobs.engine.inmemory.impl.monitor.StatusMonitorGroup;
import org.codingmatters.poomjobs.engine.inmemory.impl.store.InMemoryJobStore;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.codingmatters.poomjobs.apis.jobs.JobBuilders.from;
import static org.codingmatters.poomjobs.apis.jobs.JobStatus.PENDING;

/**
 * Created by nel on 07/07/15.
 */
public class InMemoryEngine implements JobDispatcherService, Closeable {

    static private final HashMap<String, InMemoryEngine> engines = new HashMap<>();
    private final Configuration config;
    private final EngineConfiguration engineConfiguration;
    private final JobStore store;
    private final StatusMonitorGroup statusMonitorGroup = new StatusMonitorGroup();
    private final InMemoryDispatcher dispatcher;
    private JobQueueService queueService;
    private JobListService listService;
    private JobMonitoringService monitoringService;

    public InMemoryEngine(Configuration config) {
        this.config = config;
        if(config.hasOption(Options.ENGINE_CONFIGURATION)) {
            this.engineConfiguration = (EngineConfiguration) config.getOption(Options.ENGINE_CONFIGURATION);
        } else {
            this.engineConfiguration = EngineConfiguration.defaults().config();
        }

        this.store = new InMemoryJobStore();

        this.queueService = new AbstractJobQueueService(this.store, this.engineConfiguration, this.statusMonitorGroup);
        this.listService = new AbstractJobListService(this.store);
        this.monitoringService = new AbstractJobMonitoringService(this.store, this.statusMonitorGroup);

        this.dispatcher = new InMemoryDispatcher(this.store, this.queueService);
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
    public JobDispatcherService getJobDispatcherService() {return this;}



    @Override
    public void register(JobRunner runner, String jobSpec) {
        this.dispatcher.register(runner, jobSpec);
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
