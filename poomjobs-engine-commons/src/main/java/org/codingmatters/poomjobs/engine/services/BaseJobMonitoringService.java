package org.codingmatters.poomjobs.engine.services;

import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.JobBuilders;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;
import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.StatusMonitorer;
import org.codingmatters.poomjobs.engine.exception.StoreException;
import org.codingmatters.poomjobs.engine.logs.Audit;

import java.util.UUID;

/**
 * Created by nel on 21/08/15.
 */
public class BaseJobMonitoringService implements JobMonitoringService {

    private final JobStore store;
    private final StatusMonitorer statusMonitorer;

    public BaseJobMonitoringService(JobStore store, StatusMonitorer statusMonitorer) {
        this.store = store;
        this.statusMonitorer = statusMonitorer;
    }

    @Override
    public JobStatus monitorStatus(UUID uuid, StatusChangedMonitor monitor) throws ServiceException {
        JobStatus result = null;
        try {
            result = this.store.get(JobBuilders.uuid(uuid)).getStatus();
        } catch (StoreException e) {
            throw new ServiceException(e);
        }
        this.statusMonitorer.monitor(uuid, monitor);

        Audit.log("starting monitoring job {}", uuid);
        return result;
    }
}
