package org.codingmatters.poomjobs.engine.rest;

import org.codingmatters.poomjobs.apis.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.service.rest.api.JsonCodecException;
import org.codingmatters.poomjobs.service.rest.api.JsonJobCodec;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;

import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by nel on 05/11/15.
 */
public class RestEngine implements JobQueueService {

    private final HttpClient httpClient;
    private final String baseUrl;

    private final JsonJobCodec codec = new JsonJobCodec();

    public RestEngine(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public Job submit(JobSubmission jobSubmission) throws ServiceException {
        ContentProvider content = this.prepareContent(jobSubmission);
        ContentResponse response = this.POST(content, "/jobs");
        return this.parseResponseAsJos(response);
    }

    @Override
    public Job get(UUID uuid) throws ServiceException {
        ContentResponse response = this.GET("/jobs/" + uuid.toString());
        if(response.getStatus() == 404) {
            throw new NoSuchJobException("no such job with uuid=" + uuid.toString());
        }
        return this.parseResponseAsJos(response);
    }

    @Override
    public void start(UUID uuid) throws ServiceException {
        this.POST("/jobs/" + uuid.toString() + "/start");
    }

    @Override
    public void cancel(UUID uuid) throws ServiceException {
        ContentResponse response = this.POST("/jobs/" + uuid.toString() + "/cancel");
        if (response.getStatus() == 404) {
            this.throwNoSuchJob(uuid);
        } else if (response.getStatus() == 400) {
            throw new InconsistentJobStatusException("cannot cancel job " + uuid + " with status DONE (should be one of [RUNNING, PENDING])");
        }
    }

    @Override
    public void done(UUID uuid, String... results) throws ServiceException {
        ContentResponse response = this.POST(this.prepareContent(results), "/jobs/" + uuid.toString() + "/done");
        if (response.getStatus() == 404) {
            this.throwNoSuchJob(uuid);
        } else if (response.getStatus() == 400) {
            throw new InconsistentJobStatusException("cannot stop job " + uuid + " with status PENDING (should be one of [RUNNING])");
        }
    }

    protected void throwNoSuchJob(UUID uuid) throws NoSuchJobException {
        throw new NoSuchJobException("no such job with uuid=" + uuid.toString());
    }

    @Override
    public void fail(UUID uuid, String... errors) throws ServiceException {
        ContentResponse response = this.POST(this.prepareContent(errors), "/jobs/" + uuid.toString() + "/fail");
        if (response.getStatus() == 404) {
            this.throwNoSuchJob(uuid);
        } else if (response.getStatus() == 400) {
            throw new InconsistentJobStatusException("cannot fail job " + uuid + " with status PENDING (should be one of [RUNNING])");
        }
    }

    private ContentProvider prepareContent(Object contentObject) throws ServiceException {
        StringContentProvider content;
        try {
            content = new StringContentProvider(
                    "application/json",
                    this.codec.write(contentObject),
                    Charset.forName("UTF-8"));
        } catch (JsonCodecException e) {
            throw new ServiceException("failed marshalling job submission", e);
        }
        return content;
    }


    private String url(String path) {
        return this.baseUrl + path;
    }

    private ContentResponse GET(String path) throws ServiceException {
        try {
            return this.httpClient.GET(this.url(path));
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ServiceException("failed accessing REST service at " + this.url(path), e);
        }
    }

    private ContentResponse POST(String path) throws ServiceException {
        return this.POST(null, path);
    }

    private ContentResponse POST(ContentProvider content, String path) throws ServiceException {
        try {
            Request request = this.httpClient.POST(this.url(path));
            if(content != null) {
                request.content(content);
            }
            return request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ServiceException("failed accessing REST service at " + this.url(path), e);
        }
    }

    protected Job parseResponseAsJos(ContentResponse response) throws ServiceException {
        try {
            return this.codec.readJob(response.getContentAsString());
        } catch (JsonCodecException e) {
            throw new ServiceException("failed unmarshalling response as job : " + response.getContentAsString(), e);
        }
    }
}
