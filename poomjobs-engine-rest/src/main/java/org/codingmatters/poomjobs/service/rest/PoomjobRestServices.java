package org.codingmatters.poomjobs.service.rest;

import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.http.RestService;

import static org.codingmatters.poomjobs.http.RestService.resource;

/**
 * Created by nel on 05/11/15.
 */
public class PoomjobRestServices {
    static public RestService queueService(String forPath, JobQueueService jobQueueService, JobListService jobListService) {
        JobQueueRestService queueRestService = new JobQueueRestService(jobQueueService);
        JobListRestService listRestService = new JobListRestService(jobListService);

        return RestService.root(forPath)
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
}
