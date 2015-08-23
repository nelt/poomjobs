package org.codingmatters.poomjobs.engine.inmemory;

import org.codingmatters.poomjobs.apis.jobs.JobBuilders;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;
import org.codingmatters.poomjobs.apis.services.queue.NoSuchJobException;
import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.inmemory.impl.monitor.StatusMonitorer;

import java.util.UUID;

/**
 * Created by nel on 21/08/15.
 */
public class AbstractJobMonitoringService implements JobMonitoringService {

    private final JobStore store;
    private final StatusMonitorer statusMonitorer;

    public AbstractJobMonitoringService(JobStore store, StatusMonitorer statusMonitorer) {
        this.store = store;
        this.statusMonitorer = statusMonitorer;
    }

    @Override
    public JobStatus monitorStatus(UUID uuid, StatusChangedMonitor monitor) throws NoSuchJobException {
        JobStatus result = this.store.get(JobBuilders.uuid(uuid)).getStatus();
        this.statusMonitorer.monitor(uuid, monitor);

        return result;
    }
}
