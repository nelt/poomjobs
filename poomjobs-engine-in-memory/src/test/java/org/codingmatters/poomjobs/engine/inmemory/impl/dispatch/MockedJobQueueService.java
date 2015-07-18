package org.codingmatters.poomjobs.engine.inmemory.impl.dispatch;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.apis.services.queue.NoSuchJobException;

import java.util.UUID;

/**
 * Created by nel on 18/07/15.
 */
public class MockedJobQueueService implements JobQueueService {
    @Override
    public Job submit(JobSubmission jobSubmission) {
        return null;
    }

    @Override
    public Job get(UUID uuid) throws NoSuchJobException {
        return null;
    }

    @Override
    public void start(UUID uuid) throws NoSuchJobException, InconsistentJobStatusException {

    }

    @Override
    public void done(UUID uuid, String... results) throws NoSuchJobException, InconsistentJobStatusException {

    }

    @Override
    public void cancel(UUID uuid) throws NoSuchJobException, InconsistentJobStatusException {

    }

    @Override
    public void fail(UUID uuid, String... errors) throws NoSuchJobException, InconsistentJobStatusException {

    }
}
