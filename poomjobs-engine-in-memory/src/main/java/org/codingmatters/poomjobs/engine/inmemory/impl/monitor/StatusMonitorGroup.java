package org.codingmatters.poomjobs.engine.inmemory.impl.monitor;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by nel on 16/07/15.
 */
public class StatusMonitorGroup {

    private final HashMap<UUID, Set<StatusChangedMonitor>> statusMonitors = new HashMap<>();

    public synchronized void monitor(UUID uuid, StatusChangedMonitor monitor) {
        if(! this.statusMonitors.containsKey(uuid)) {
            this.statusMonitors.put(uuid, new HashSet<>());
        }
        this.statusMonitors.get(uuid).add(monitor);
    }

    public synchronized void changed(Job job, JobStatus old) {
        if(this.statusMonitors.containsKey(job.getUuid())) {
            for (StatusChangedMonitor statusChangedMonitor : this.statusMonitors.get(job.getUuid())) {
                statusChangedMonitor.statusChanged(job, old);
            }
        }
    }
}
