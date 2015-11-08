package org.codingmatters.poomjobs.service.rest;

import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.http.RestService;

import static org.codingmatters.poomjobs.http.RestService.resource;

/**
 * Created by nel on 05/11/15.
 */
public class PoomjobRestServices {
    static public RestService queueService(String forPath, JobQueueService jobQueueService) {
        JobQueueRestService service = new JobQueueRestService(jobQueueService);
        return RestService.root(forPath)
                .resource("/jobs/{uuid}/start", resource()
                        .POST(io -> service.start(io))
                ).resource("/jobs/{uuid}", resource()
                        .GET(io -> service.get(io))
                )
                .resource("/jobs", resource()
                        .POST(io -> service.submit(io))
                );
    }
}
