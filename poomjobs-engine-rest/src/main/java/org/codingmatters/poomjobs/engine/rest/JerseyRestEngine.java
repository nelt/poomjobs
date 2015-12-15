package org.codingmatters.poomjobs.engine.rest;

import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;

import javax.ws.rs.client.Client;
import java.util.UUID;

/**
 * Created by nel on 18/11/15.
 */
public class JerseyRestEngine implements JobMonitoringService {

    private final Client httpClient;
    private final String baseUrl;

    public JerseyRestEngine(Client httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public JobStatus monitorStatus(UUID uuid, StatusChangedMonitor monitor) throws ServiceException {
        return null;
    }
}
