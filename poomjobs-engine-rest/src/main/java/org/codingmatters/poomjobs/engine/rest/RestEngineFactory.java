package org.codingmatters.poomjobs.engine.rest;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactory;
import org.codingmatters.poomjobs.apis.services.dispatch.JobDispatcherService;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.eclipse.jetty.client.HttpClient;

/**
 * Created by nel on 05/11/15.
 */
public class RestEngineFactory implements ServiceFactory {

    private static final String URL = "rest.url";
    private static final String HTTP_CLIENT = "rest.http.client";

    static private RestEngineFactory instance = new RestEngineFactory();

    static public Configuration.Builder forURL(String url, HttpClient httpClient) {
        return Configuration.defaults(instance).withOption(URL, url).withOption(HTTP_CLIENT, httpClient);
    }

    @Override
    public JobQueueService queueService(Configuration config) {
        return this.createRestEngine(config);
    }

    protected RestEngine createRestEngine(Configuration config) {
        HttpClient httpClient = (HttpClient) config.getOption(HTTP_CLIENT);
        String url = (String) config.getOption(URL);
        return new RestEngine(httpClient, url);
    }

    @Override
    public JobListService listService(Configuration config) {
        return this.createRestEngine(config);
    }

    @Override
    public JobMonitoringService monitoringService(Configuration config) {
        return null;
    }

    @Override
    public JobDispatcherService dispatcherService(Configuration config) {
        return null;
    }
}
