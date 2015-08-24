package org.codingmatters.poomjobs.engine.services;

import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.exception.StoreException;
import org.codingmatters.poomjobs.engine.logs.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nel on 21/08/15.
 */
public class BaseJobListService implements JobListService {

    static private final Logger log = LoggerFactory.getLogger(BaseJobListService.class);

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
            String errorReference = Audit.logError("error querying job list {}", query);
            log.error(errorReference + "error querying job list " + query, e);
            throw new ServiceException(e);
        }
        Audit.log("job list queried {}", query);
        return result;
    }

}
