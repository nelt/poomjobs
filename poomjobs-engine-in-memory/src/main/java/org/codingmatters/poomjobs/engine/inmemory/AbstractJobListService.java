package org.codingmatters.poomjobs.engine.inmemory;

import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
import org.codingmatters.poomjobs.engine.JobStore;

/**
 * Created by nel on 21/08/15.
 */
public class AbstractJobListService implements JobListService {
    private final JobStore store;

    public AbstractJobListService(JobStore store) {
        this.store = store;
    }

    @Override
    public JobList list(ListQuery query) {
        return this.store.list(query);
    }

}
