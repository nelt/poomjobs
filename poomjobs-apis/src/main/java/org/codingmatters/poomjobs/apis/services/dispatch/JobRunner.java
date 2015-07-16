package org.codingmatters.poomjobs.apis.services.dispatch;

import org.codingmatters.poomjobs.apis.jobs.Job;

/**
 * Created by nel on 16/07/15.
 */
public interface JobRunner {
    void run(Job job);
}
