package org.codingmatters.poomjobs.service.rest;

import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.http.RestService;
import org.codingmatters.poomjobs.http.sse.ServerSentEventChannel;

import static org.codingmatters.poomjobs.http.RestService.resource;
import static org.codingmatters.poomjobs.http.RestService.sseChannel;

/**
 * Created by nel on 05/11/15.
 */
public class PoomjobRestServices {
    static public RestService queueService(JobQueueService jobQueueService, JobListService jobListService) {
        JobQueueRestService queueRestService = new JobQueueRestService(jobQueueService);
        JobListRestService listRestService = new JobListRestService(jobListService);

        return RestService.service()
                .resource("/jobs/{uuid}/start", resource()
                        .POST(io -> queueRestService.start(io))
                )
                .resource("/jobs/{uuid}/done", resource()
                        .POST(io -> queueRestService.done(io))
                )
                .resource("/jobs/{uuid}/cancel", resource()
                        .POST(io -> queueRestService.cancel(io))
                )
                .resource("/jobs/{uuid}/fail", resource()
                        .POST(io -> queueRestService.fail(io))
                )
                .resource("/jobs/{uuid}", resource()
                        .GET(io -> queueRestService.get(io))
                )
                .resource("/jobs", resource()
                        .POST(io -> queueRestService.submit(io))
                )
                .resource("/jobs/list", resource()
                        .POST(io -> listRestService.list(io))
                );
    }

    static public RestService monitoringService(JobMonitoringService jobMonitoringService) {
        JobMonitoringRestService monitoringService = new JobMonitoringRestService(jobMonitoringService);

        ServerSentEventChannel sseChannel = sseChannel()
                .onRegister((client, channel) -> monitoringService.newClient(client, channel))
                .onUnregister((client, channel) -> monitoringService.clientGone(client, channel))
                .channel();

        monitoringService.setChannel(sseChannel);

        return RestService.service()
                .resource("/jobs/{uuid}/monitor/status", resource()
                        .POST(io -> monitoringService.monitoringRequest(io))
                )
                .serverSentEventChannel("/jobs/monitoring", sseChannel)
                ;
    }
}
