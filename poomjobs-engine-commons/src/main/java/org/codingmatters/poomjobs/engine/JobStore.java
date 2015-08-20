package org.codingmatters.poomjobs.engine;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;

/**
 * Created by nel on 19/08/15.
 */
public interface JobStore {
    void store(Job job);

    JobList list(ListQuery query);

    Job pendingJob(String jobSpec);

    void clean();

    void start();

    void stop();

    Job get(Job job);

    void register(JobRunner runner, String forJob);
}
