package org.codingmatters.poomjobs.engine.inmemory.impl;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.jobs.*;
import org.codingmatters.poomjobs.apis.jobs.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.services.dispatch.JobDispatcherService;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.apis.services.queue.NoSuchJobException;
import org.codingmatters.poomjobs.engine.EngineConfiguration;
import org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory;
import org.codingmatters.poomjobs.engine.inmemory.impl.dispatch.InMemoryDispatcher;
import org.codingmatters.poomjobs.engine.inmemory.impl.monitor.StatusMonitorGroup;
import org.codingmatters.poomjobs.engine.inmemory.impl.store.InMemoryJobStore;

import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.util.*;

import static org.codingmatters.poomjobs.apis.jobs.JobBuilders.from;
import static org.codingmatters.poomjobs.apis.jobs.JobStatus.PENDING;

/**
 * Created by nel on 07/07/15.
 */
public class InMemoryEngine implements JobQueueService, JobListService, JobMonitoringService, JobDispatcherService {

    static final HashMap<String, WeakReference<InMemoryEngine>> engines = new HashMap<>();

    public static InMemoryEngine getEngine(Configuration config) {
        synchronized (engines) {
            String name = (String) config.getOption(InMemoryServiceFactory.NAME_OPTION);
            if (!engines.containsKey(name) || engines.get(name).get() == null) {
                engines.put(name, new WeakReference<>(new InMemoryEngine(config)));
            }
            return engines.get(name).get();
        }
    }



    public interface Options {
        String ENGINE_CONFIGURATION = "enngine.configuration";
    }

    private final Configuration config;
    private final EngineConfiguration engineConfiguration;

    private final InMemoryJobStore store = new InMemoryJobStore();

    private final InMemoryDispatcher dispatcher;

    public InMemoryEngine(Configuration config) {
        this.config = config;
        if(config.hasOption(Options.ENGINE_CONFIGURATION)) {
            this.engineConfiguration = (EngineConfiguration) config.getOption(Options.ENGINE_CONFIGURATION);
        } else {
            this.engineConfiguration = EngineConfiguration.defaults().config();
        }
        this.store.start();

        this.dispatcher = new InMemoryDispatcher(this.store, this);
        this.dispatcher.start();
    }


    public Job submit(JobSubmission jobSubmission) {
        JobBuilders.Builder builder = from(jobSubmission)
                .withUuid(UUID.randomUUID())
                .withSubmissionTime(LocalDateTime.now())
                .withRetentionDelay(
                        jobSubmission.getRetentionDelay() != null ?
                                jobSubmission.getRetentionDelay() : this.engineConfiguration.getDefaultRetentionDelay()
                )
                .withStatus(PENDING)
                ;
        Job job = builder.job();
        this.store.store(job);

        return job;
    }

    public JobList list() {
        return this.store.currentList();
    }

    public Job get(UUID uuid) throws NoSuchJobException {
        Job result = this.store.get(JobBuilders.uuid(uuid));
        if(result == null) {
            throw new NoSuchJobException("no such job with uuid=" + uuid.toString());
        }
        return result;
    }

    public void start(UUID uuid) throws NoSuchJobException, InconsistentJobStatusException {
        this.mutateJob(uuid, JobOperation.START);
    }

    public void done(UUID uuid, String ... results) throws NoSuchJobException, InconsistentJobStatusException {
        this.mutateJob(uuid, JobOperation.STOP, job -> from(job).withResults(results).job());
    }

    @Override
    public void cancel(UUID uuid) throws NoSuchJobException, InconsistentJobStatusException {
        this.mutateJob(uuid, JobOperation.CANCEL);
    }

    @Override
    public void fail(UUID uuid, String... errors) throws NoSuchJobException, InconsistentJobStatusException {
        this.mutateJob(uuid, JobOperation.FAIL, j -> from(j).withErrors(errors).job());
    }

    private void mutateJob(UUID uuid, JobOperation operation) throws NoSuchJobException, InconsistentJobStatusException {
        this.mutateJob(uuid, operation, job -> job);
    }
    private void mutateJob(UUID uuid, JobOperation operation, Mutator mutator) throws NoSuchJobException, InconsistentJobStatusException {
        Job job = this.get(uuid);
        JobStatus old = job.getStatus();
        job = mutator.mutate(job);
        job = operation.operate(job);
        this.store.store(job);

        if(! old.equals(job.getStatus())) {
            this.statusMonitorGroup.changed(job, old);
        }
    }

    private interface Mutator {
        Job mutate(Job job);
    }

    private final StatusMonitorGroup statusMonitorGroup = new StatusMonitorGroup();

    @Override
    public JobStatus monitorStatus(UUID uuid, StatusChangedMonitor monitor) throws NoSuchJobException {
        JobStatus result = this.get(uuid).getStatus();
        this.statusMonitorGroup.monitor(uuid, monitor);
        return result;
    }

    @Override
    public void register(JobRunner runner, String... forJobs) {
        if(forJobs == null) return;
        this.dispatcher.register(runner, forJobs);
    }


}
