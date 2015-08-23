package org.codingmatters.poomjobs.engine.inmemory.impl.monitor;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryStatusMonitorer implements StatusMonitorer {

    private final HashMap<UUID, Set<StatusChangedMonitor>> statusMonitors = new HashMap<>();

    @Override
    public synchronized void monitor(UUID uuid, StatusChangedMonitor monitor) {
        if(! this.statusMonitors.containsKey(uuid)) {
            this.statusMonitors.put(uuid, new HashSet<>());
        }
        this.statusMonitors.get(uuid).add(monitor);
    }

    @Override
    public synchronized void changed(Job job, JobStatus old) {
        if(this.statusMonitors.containsKey(job.getUuid())) {
            LinkedList<StatusChangedMonitor> invalidatedMonitors = new LinkedList<>();
            for (StatusChangedMonitor monitor : this.statusMonitors.get(job.getUuid())) {
                if(
                        monitor instanceof StatusChangedMonitor.Weak &&
                                ! ((StatusChangedMonitor.Weak) monitor).isValid()) {
                    invalidatedMonitors.add(monitor);
                } else {
                    monitor.statusChanged(job, old);
                }
            }
            if(! invalidatedMonitors.isEmpty()) {
                this.statusMonitors.get(job.getUuid()).removeAll(invalidatedMonitors);
            }
        }
    }

    @Override
    public synchronized int monitorCount() {
        AtomicInteger result = new AtomicInteger(0);
        this.statusMonitors.forEach((uuid, statusChangedMonitors) -> {
            result.addAndGet(statusChangedMonitors.size());
        });
        return result.get();
    }
}
