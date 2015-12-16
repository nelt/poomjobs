package org.codingmatters.poomjobs.service.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;
import org.codingmatters.poomjobs.http.RestException;
import org.codingmatters.poomjobs.http.RestIO;
import org.codingmatters.poomjobs.http.RestStatus;
import org.codingmatters.poomjobs.http.sse.ServerSentEventChannel;
import org.codingmatters.poomjobs.http.sse.ServerSentEventClient;
import org.codingmatters.poomjobs.service.rest.api.JsonCodecException;
import org.codingmatters.poomjobs.service.rest.api.JsonJobCodec;
import org.codingmatters.poomjobs.service.rest.api.RestJobStatusChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.codingmatters.poomjobs.http.sse.ServerSentEvent.data;

/**
 * Created by nel on 14/12/15.
 */
public class JobMonitoringRestService {

    static private Logger log = LoggerFactory.getLogger(JobMonitoringRestService.class);

    private final JobMonitoringService jobMonitoringService;
    private final HashMap<String, ServerSentEventClient> clientByUuid = new HashMap<>();
    private final JsonJobCodec codec = new JsonJobCodec();

    private ServerSentEventChannel channel;
    private ObjectMapper mapper = new ObjectMapper();

    public JobMonitoringRestService(JobMonitoringService jobMonitoringService) {
        this.jobMonitoringService = jobMonitoringService;
    }

    public void newClient(ServerSentEventClient client, ServerSentEventChannel channel) {
        this.clientByUuid.put(client.uuid(), client);
        channel.send(data(client.uuid()).withEvent("uuid").event(), client);
    }

    public void clientGone(ServerSentEventClient client, ServerSentEventChannel channel) {
        this.clientByUuid.remove(client.uuid());
    }

    public void monitoringRequest(RestIO io) throws RestException {
        UUID jobUuid = this.getJobUuid(io);
        ServerSentEventClient client = this.getClient(io);
        try {
            JobStatus currentStatus = this.jobMonitoringService.monitorStatus(jobUuid, this.createStatusChangedMonitor(client));
            String json = this.mapper.writeValueAsString(currentStatus);
            log.debug("response body: {}", json);
            io.status(RestStatus.OK)
                    .contentType("application/json")
                    .content(json);
        } catch (NoSuchJobException e) {
            throw new RestException(RestStatus.RESOURCE_NOT_FOUND, e.getMessage(), e);
        } catch (ServiceException | JsonProcessingException e) {
            throw new RestException(RestStatus.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    protected ServerSentEventClient getClient(RestIO io) throws RestException {
        try {
            Map params = this.mapper.readValue(new String(io.requestContent(), "UTF-8"), Map.class);
            if(params.containsKey("clientUuid")) {
                String clientUuid = (String) params.get("clientUuid");
                log.debug("looking up client with uuid: {}", clientUuid);
                if(this.clientByUuid.containsKey(clientUuid)) {
                    return this.clientByUuid.get(clientUuid);
                } else {
                    log.error("no client registered with uuid {}", clientUuid);
                    throw new RestException(RestStatus.BAD_REQUEST, "client uuid not registered");
                }
            } else {
                throw new RestException(RestStatus.BAD_REQUEST, "must provide a client uuid");
            }
        } catch (IOException e) {
            throw new RestException(RestStatus.BAD_REQUEST, "error reading parameters", e);
        }
    }

    protected UUID getJobUuid(RestIO io) throws RestException {
        UUID jobUuid;
        if(io.parameters().get("uuid") == null || io.parameters().get("uuid").isEmpty()) {
            log.debug("path params: {}", io.parameters());
            throw new RestException(RestStatus.BAD_REQUEST, "must provide a job uuid");
        }
        try {
            jobUuid = UUID.fromString(io.parameters().get("uuid").get(0));
        } catch(IllegalArgumentException e) {
            throw new RestException(RestStatus.BAD_REQUEST, "must provide a valid job uuid", e);
        }
        return jobUuid;
    }

    protected StatusChangedMonitor createStatusChangedMonitor(ServerSentEventClient client) {
        return (job, old) -> {
            try {
                this.channel.send(
                        data(this.codec.write(new RestJobStatusChange(old, job)))
                                .withEvent("job-status-changed")
                                .event(),
                        client);
            } catch (JsonCodecException e) {
                log.error("error formatting status change event from : %s ; %s", job, old);
            }
        };
    }

    public void setChannel(ServerSentEventChannel channel) {
        this.channel = channel;
    }
}
