package org.codingmatters.poomjobs.engine.services;

import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.exception.StoreException;
import org.codingmatters.poomjobs.engine.logs.Audit;

/**
 * Created by nel on 21/08/15.
 */
public class BaseJobListService implements JobListService {
    private final JobStore store;

    public BaseJobListService(JobStore store) {
        this.store = store;
    }

    @Override
    public JobList list(ListQuery query) throws ServiceException {
        JobList result = null;
        try {
            result = this.store.list(query);
        } catch (StoreException e) {
            throw new ServiceException(e);
        }
        Audit.log("job list queried {}", query);
        return result;
    }

}
