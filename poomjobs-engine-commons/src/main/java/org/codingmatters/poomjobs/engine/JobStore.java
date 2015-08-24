package org.codingmatters.poomjobs.engine;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
import org.codingmatters.poomjobs.engine.exception.StoreException;

/**
 * Created by nel on 19/08/15.
 */
public interface JobStore {

    void store(Job job) throws StoreException;
    Job get(Job job) throws StoreException;
    JobList list(ListQuery query) throws StoreException;
    Job pendingJob(String jobSpec) throws StoreException;

    void clean() throws StoreException;

    void start();
    void stop();
}
