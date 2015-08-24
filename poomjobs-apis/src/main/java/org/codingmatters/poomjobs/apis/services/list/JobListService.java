package org.codingmatters.poomjobs.apis.services.list;

import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.JobList;

/**
 * Created by nel on 15/07/15.
 */
public interface JobListService {
    JobList list(ListQuery query) throws ServiceException;
}
