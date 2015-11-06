package org.codingmatters.poomjobs.service.rest;

import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.http.RestException;
import org.codingmatters.poomjobs.http.RestIO;
import org.codingmatters.poomjobs.http.RestStatus;
import org.codingmatters.poomjobs.service.rest.api.JsonCodecException;
import org.codingmatters.poomjobs.service.rest.api.JsonJobCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by nel on 06/11/15.
 */
public class JobQueueRestService {
    static private final Logger log = LoggerFactory.getLogger(JobQueueRestService.class);

    private final JobQueueService deleguate;
    private final JsonJobCodec codec = new JsonJobCodec();

    public JobQueueRestService(JobQueueService deleguate) {
        this.deleguate = deleguate;
    }

    public void get(RestIO io) throws RestException {
        UUID uuid = UUID.fromString(io.pathParameters().get("uuid").get(0));
        try {
            Job job = this.deleguate.get(uuid);
            io.status(RestStatus.OK)
                    .contentType("application/json")
                    .encoding("UTF-8")
                    .content(this.codec.write(job));
        } catch (ServiceException e) {
            log.error("job not found : " + uuid.toString(), e);
            throw new RestException(RestStatus.RESOURCE_NOT_FOUND, e);
        } catch (JsonCodecException e) {
            log.error("failed writing job as JSON : " + uuid.toString(), e);
            throw new RestException(RestStatus.INTERNAL_ERROR, e);
        }
    }

    public void submit(RestIO io) throws RestException {
        String json = new String(io.requestContent(), Charset.forName("UTF-8"));
        try {
            JobSubmission submission = this.codec.readJobSubmission(json);
            Job job = this.deleguate.submit(submission);

            io.status(RestStatus.SEE_OTHER).header("Location", "./jobs/" + job.getUuid().toString());
        } catch (JsonCodecException e) {
            log.error("unable to parse JSON : " + json, e);
            throw new RestException(RestStatus.BAD_REQUEST, e);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
}
