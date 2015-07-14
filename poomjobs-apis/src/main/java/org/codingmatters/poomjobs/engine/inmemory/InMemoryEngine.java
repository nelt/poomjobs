package org.codingmatters.poomjobs.engine.inmemory;

import org.codingmatters.poomjobs.apis.jobs.*;
import org.codingmatters.poomjobs.apis.list.*;
import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.engine.EngineConfiguration;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.codingmatters.poomjobs.apis.jobs.JobBuilders.from;
import static org.codingmatters.poomjobs.apis.jobs.JobStatus.*;

/**
 * Created by nel on 07/07/15.
 */
public class InMemoryEngine implements JobListService {

    public interface Options {
        String ENGINE_CONFIGURATION = "enngine.configuration";
    }

    private final Configuration config;
    private final EngineConfiguration engineConfiguration;

    private final InMemoryJobStore store = new InMemoryJobStore();

    public InMemoryEngine(Configuration config) {
        this.config = config;
        if(config.hasOption(Options.ENGINE_CONFIGURATION)) {
            this.engineConfiguration = (EngineConfiguration) config.getOption(Options.ENGINE_CONFIGURATION);
        } else {
            this.engineConfiguration = EngineConfiguration.defaults().config();
        }
        this.store.startCleanerThread();
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
        job = mutator.mutate(job);
        job = operation.operate(job);
        this.store.store(job);
    }

    private interface Mutator {
        Job mutate(Job job);
    }
}
