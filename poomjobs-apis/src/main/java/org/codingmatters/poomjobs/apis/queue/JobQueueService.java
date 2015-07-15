package org.codingmatters.poomjobs.apis.queue;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.jobs.exception.InconsistentJobStatusException;

import java.util.UUID;

/**
 * Created by nel on 05/07/15.
 */
public interface JobQueueService {
    Job submit(JobSubmission jobSubmission);

    Job get(UUID uuid) throws NoSuchJobException;

    void start(UUID uuid) throws NoSuchJobException, InconsistentJobStatusException;

    void done(UUID uuid, String ... results) throws NoSuchJobException, InconsistentJobStatusException;

    void cancel(UUID uuid) throws NoSuchJobException, InconsistentJobStatusException;

    void fail(UUID uuid, String ... errors) throws NoSuchJobException, InconsistentJobStatusException;
}
