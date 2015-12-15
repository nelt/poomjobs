package org.codingmatters.poomjobs.service.rest.api;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;

/**
 * Created by nel on 14/12/15.
 */
public class JsonStatusChange {
    private final JobStatus oldStatus;
    private final Job job;

    public JsonStatusChange(JobStatus oldStatus, Job job) {
        this.oldStatus = oldStatus;
        this.job = job;
    }

    public JobStatus getOldStatus() {
        return oldStatus;
    }

    public Job getJob() {
        return job;
    }
}
