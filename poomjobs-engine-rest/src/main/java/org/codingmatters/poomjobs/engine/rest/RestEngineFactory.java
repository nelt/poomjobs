package org.codingmatters.poomjobs.engine.rest;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactory;
import org.codingmatters.poomjobs.apis.services.dispatch.JobDispatcherService;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.eclipse.jetty.client.HttpClient;

import javax.ws.rs.client.Client;

/**
 * Created by nel on 05/11/15.
 */
public class RestEngineFactory implements ServiceFactory {

    public static final String URL = "rest.url";
    public static final String JETTY_CLIENT = "jetty.http.client";
    public static final String JERSEY_CLIENT = "jersey.http.client";

    static private RestEngineFactory instance = new RestEngineFactory();

    static public Configuration.Builder forURL(String url) {
        return Configuration.defaults(instance).withOption(URL, url);
    }

    @Override
    public JobQueueService queueService(Configuration config) {
        return this.createJettyEngine(config);
    }

    protected JettyRestEngine createJettyEngine(Configuration config) {
        HttpClient httpClient = (HttpClient) config.getOption(JETTY_CLIENT);
        String url = (String) config.getOption(URL);
        return new JettyRestEngine(httpClient, url);
    }

    protected JerseyRestEngine createJerseyEngine(Configuration config) {
        Client httpClient = (Client) config.getOption(JERSEY_CLIENT);
        String url = (String) config.getOption(URL);
        return new JerseyRestEngine(httpClient, url);
    }



    @Override
    public JobListService listService(Configuration config) {
        return this.createJettyEngine(config);
    }

    @Override
    public JobMonitoringService monitoringService(Configuration config) {
        return this.createJerseyEngine(config);
    }

    @Override
    public JobDispatcherService dispatcherService(Configuration config) {
        return null;
    }
}
