package org.codingmatters.poomjobs.engine.inmemory;

import org.codingmatters.poomjobs.apis.jobs.*;
import org.codingmatters.poomjobs.apis.jobs.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.apis.services.queue.NoSuchJobException;
import org.codingmatters.poomjobs.engine.EngineConfiguration;
import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.inmemory.impl.monitor.StatusMonitorGroup;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.codingmatters.poomjobs.apis.jobs.JobBuilders.from;
import static org.codingmatters.poomjobs.apis.jobs.JobStatus.PENDING;

/**
 * Created by nel on 21/08/15.
 */
public class AbstractJobQueueService implements JobQueueService {

    private final JobStore store;
    private final EngineConfiguration engineConfiguration;
    private final StatusMonitorGroup statusMonitorGroup;

    public AbstractJobQueueService(JobStore store, EngineConfiguration engineConfiguration, StatusMonitorGroup statusMonitorGroup) {
        this.store = store;
        this.engineConfiguration = engineConfiguration;
        this.statusMonitorGroup = statusMonitorGroup;
    }

    @Override
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


    @Override
    public Job get(UUID uuid) throws NoSuchJobException {
        Job result = this.store.get(JobBuilders.uuid(uuid));
        if(result == null) {
            throw new NoSuchJobException("no such job with uuid=" + uuid.toString());
        }
        return result;
    }

    @Override
    public void start(UUID uuid) throws NoSuchJobException, InconsistentJobStatusException {
        this.mutateJob(uuid, JobOperation.START);
    }

    @Override
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
}
