package org.codingmatters.poomjobs.apis.list;

import org.codingmatters.poomjobs.apis.jobs.Job;

import java.util.UUID;

/**
 * Created by nel on 05/07/15.
 */
public interface JobListService {
    Job submit(JobSubmission jobSubmission);

    JobList list();

    Job get(UUID uuid) throws NoSuchJobException;

    void start(UUID uuid) throws NoSuchJobException, InconsistentJobStatusException;

    void done(UUID uuid, String ... results) throws NoSuchJobException, InconsistentJobStatusException;

    void cancel(UUID uuid) throws NoSuchJobException, InconsistentJobStatusException;

    void fail(UUID uuid, String ... errors) throws NoSuchJobException, InconsistentJobStatusException;
}
