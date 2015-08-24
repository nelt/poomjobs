package org.codingmatters.poomjobs.engine.services;

import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.*;
import org.codingmatters.poomjobs.apis.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.engine.EngineConfiguration;
import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.StatusMonitorer;
import org.codingmatters.poomjobs.engine.exception.StoreException;
import org.codingmatters.poomjobs.engine.logs.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.codingmatters.poomjobs.apis.jobs.JobBuilders.from;
import static org.codingmatters.poomjobs.apis.jobs.JobStatus.PENDING;

/**
 * Created by nel on 21/08/15.
 */
public class BaseJobQueueService implements JobQueueService {

    static private final Logger log = LoggerFactory.getLogger(BaseJobQueueService.class);

    private final JobStore store;
    private final EngineConfiguration engineConfiguration;
    private final StatusMonitorer statusMonitorer;

    public BaseJobQueueService(JobStore store, EngineConfiguration engineConfiguration, StatusMonitorer statusMonitorer) {
        this.store = store;
        this.engineConfiguration = engineConfiguration;
        this.statusMonitorer = statusMonitorer;
    }

    @Override
    public Job submit(JobSubmission jobSubmission) {
        log.debug("job submitted {}", jobSubmission.getJob());
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
        try {
            this.store.store(job);
        } catch (StoreException e) {

        }

        Audit.log("job submitted {}", job);
        return job;
    }


    @Override
    public Job get(UUID uuid) throws ServiceException {
        Job result = null;
        try {
            result = this.store.get(JobBuilders.uuid(uuid));
        } catch (StoreException e) {
            log.error("error getting job from store " + uuid, e);
            throw new ServiceException(e);
        }
        if(result == null) {
            throw new NoSuchJobException("no such job with uuid=" + uuid.toString());
        }
        Audit.log("get job {}", uuid);
        return result;
    }

    @Override
    public void start(UUID uuid) throws ServiceException {
        try {
            this.mutateJob(uuid, JobOperation.START);
            Audit.log("started job {}", uuid);
        } catch (StoreException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void done(UUID uuid, String ... results) throws ServiceException {
        try {
            this.mutateJob(uuid, JobOperation.STOP, job -> from(job).withResults(results).job());
            Audit.log("stopped job {}", uuid);
        } catch (StoreException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void cancel(UUID uuid) throws ServiceException {
        try {
            this.mutateJob(uuid, JobOperation.CANCEL);
            Audit.log("canceled job {}", uuid);
        } catch (StoreException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void fail(UUID uuid, String... errors) throws ServiceException {
        try {
            this.mutateJob(uuid, JobOperation.FAIL, j -> from(j).withErrors(errors).job());
            Audit.log("failed job {}", uuid);
        } catch (StoreException e) {
            throw new ServiceException(e);
        }
    }

    private void mutateJob(UUID uuid, JobOperation operation) throws StoreException, ServiceException {
        this.mutateJob(uuid, operation, job -> job);
    }
    private void mutateJob(UUID uuid, JobOperation operation, Mutator mutator) throws StoreException, ServiceException {
        Job job = this.get(uuid);
        JobStatus old = job.getStatus();
        job = mutator.mutate(job);
        job = operation.operate(job);

        this.store.store(job);

        if(! old.equals(job.getStatus())) {
            this.statusMonitorer.changed(job, old);
        }
    }


    private interface Mutator {
        Job mutate(Job job);
    }
}
