package org.codingmatters.poomjobs.service.rest;

import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
import org.codingmatters.poomjobs.http.RestException;
import org.codingmatters.poomjobs.http.RestIO;
import org.codingmatters.poomjobs.http.RestStatus;
import org.codingmatters.poomjobs.service.rest.api.JsonCodecException;
import org.codingmatters.poomjobs.service.rest.api.JsonJobCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by nel on 10/11/15.
 */
public class JobListRestService {
    static private final Logger log = LoggerFactory.getLogger(JobListRestService.class);

    private final JsonJobCodec codec = new JsonJobCodec();
    private final JobListService deleguate;

    public JobListRestService(JobListService deleguate) {
        this.deleguate = deleguate;
    }

    public void list(RestIO io) throws RestException {
        ListQuery query = null;
        try {
            query = this.codec.readListQuery(new String(io.requestContent(), Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(query == null) {
            query = ListQuery.query();
        }

        JobList result = null;
        try {
            result = this.deleguate.list(query);
            log.debug("retrieved list: {}", result);
            io.status(RestStatus.OK)
                    .contentType("application/json")
                    .encoding("UTF-8")
                    .content(this.codec.write(result));
        } catch (ServiceException e) {
            throw new RestException(RestStatus.RESOURCE_NOT_FOUND, e.getMessage(), e);
        } catch (JsonCodecException e) {
            log.error("failed writing job list as JSON : " + result, e);
            throw new RestException(RestStatus.INTERNAL_ERROR, e.getMessage(), e);
        }
    }
}
