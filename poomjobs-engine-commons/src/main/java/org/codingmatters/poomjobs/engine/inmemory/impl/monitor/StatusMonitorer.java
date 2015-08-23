package org.codingmatters.poomjobs.engine.inmemory.impl.monitor;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;

import java.util.UUID;

/**
 * Created by nel on 23/08/15.
 */
public interface StatusMonitorer {
    void monitor(UUID uuid, StatusChangedMonitor monitor);

    void changed(Job job, JobStatus old);

    int monitorCount();
}
