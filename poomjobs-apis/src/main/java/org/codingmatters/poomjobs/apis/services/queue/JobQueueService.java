package org.codingmatters.poomjobs.apis.services.queue;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.apis.exception.ServiceException;

import java.util.UUID;

/**
 * Created by nel on 05/07/15.
 */
public interface JobQueueService {
    Job submit(JobSubmission jobSubmission) throws ServiceException;

    Job get(UUID uuid) throws ServiceException;

    void start(UUID uuid) throws ServiceException;

    void done(UUID uuid, String ... results) throws ServiceException;

    void cancel(UUID uuid) throws ServiceException;

    void fail(UUID uuid, String ... errors) throws ServiceException;
}
