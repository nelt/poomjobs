package org.codingmatters.poomjobs.engine.rest;

import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;
import org.codingmatters.poomjobs.service.rest.AtomicString;
import org.codingmatters.poomjobs.service.rest.api.JsonJobCodec;
import org.codingmatters.poomjobs.service.rest.api.RestJobStatusChange;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static javax.ws.rs.client.Entity.entity;

/**
 * Created by nel on 18/11/15.
 */
public class JerseyRestEngine implements JobMonitoringService {

    static private final Logger log = LoggerFactory.getLogger(JerseyRestEngine.class);

    private final Client httpClient;
    private final String baseUrl;

    private final Map<UUID, StatusChangedMonitor> monitors = new HashMap<>();

    private final EventSource eventSource;
    private final AtomicString clientUuid = new AtomicString(null);
    private final JsonJobCodec codec = new JsonJobCodec();

    private final long registrationTimeout = 10000;

    public JerseyRestEngine(Client httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;

        this.eventSource = EventSource.target(this.httpClient.target(this.baseUrl).path("/jobs/monitoring")).build();
        this.eventSource.register(this::registered, "uuid");
        this.eventSource.register(this::statusChanged, "job-status-changed");
    }

    @Override
    public JobStatus monitorStatus(UUID uuid, StatusChangedMonitor monitor) throws ServiceException {
        this.manageEventSource();
        Response response = this.httpClient.target(this.baseUrl).path(String.format("/jobs/%s/monitor/status", uuid.toString()))
                .request()
                .post(entity("{\"clientUuid\": \"" + this.clientUuid.get() + "\"}", "application/json"));
        if(response.getStatusInfo().equals(Response.Status.NOT_FOUND)) {
            throw new NoSuchJobException(response.readEntity(String.class));
        }
        if(! response.getStatusInfo().equals(Response.Status.OK)) {
            log.error("while registering monitor for job {}, response status was {}", uuid, response.getStatusInfo());
            throw new ServiceException("error registering change monitor for job " + uuid + "( clientUuid=" + this.clientUuid.get() + ")");
        }
        this.monitors.put(uuid, monitor);
        String json = response.readEntity(String.class);
        try {
            return this.codec.readJobStatus(json);
        } catch (IOException e) {
            throw new ServiceException("error reading status from response : " + json, e);
        }
    }

    private synchronized void manageEventSource() throws ServiceException {
        if(! this.eventSource.isOpen()) {
            this.eventSource.open();
        }
        try {
            this.clientUuid.waitUntilNot(null, this.registrationTimeout);
        } catch (InterruptedException | TimeoutException e) {
            throw new ServiceException("failed registering to event source", e);
        }
    }



    private void registered(InboundEvent event) {
        this.clientUuid.set(event.readData());
    }

    private void statusChanged(InboundEvent event) {
        RestJobStatusChange change;
        try {
            change = this.codec.readStatusChange(event.readData());
        } catch (IOException e) {
            log.error("error reading status change from event: " + event, e);
            return;
        }
        log.debug("job change : " + change);
        StatusChangedMonitor monitor = this.monitors.get(change.getJob().getUuid());
        if(monitor != null) {
            monitor.statusChanged(change.getJob(), change.getOldStatus());
        } else {
            log.error("no monitor found for {}, registered monitors for {}", change, this.monitors.values());
        }
    }

}
