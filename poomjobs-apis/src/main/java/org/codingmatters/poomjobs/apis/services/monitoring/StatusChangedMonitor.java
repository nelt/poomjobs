package org.codingmatters.poomjobs.apis.services.monitoring;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;

/**
 * Created by nel on 16/07/15.
 */
public interface StatusChangedMonitor {
    void statusChanged(Job job, JobStatus old);
}
