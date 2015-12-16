package org.codingmatters.poomjobs.engine.rest;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.http.TestUndertowServer;
import org.eclipse.jetty.client.HttpClient;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.rules.ExternalResource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.UUID;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;
import static org.codingmatters.poomjobs.http.undertow.RestServiceBundle.services;
import static org.codingmatters.poomjobs.http.undertow.RestServiceHandler.from;
import static org.codingmatters.poomjobs.service.rest.PoomjobRestServices.*;

/**
 * Created by nel on 09/11/15.
 */
public class RestEngineTestConfigurationProvider extends ExternalResource implements TestConfigurationProvider {

    private final TestUndertowServer server;

    private HttpClient jettyClient;
    private Configuration configuration;
    private JobQueueService queueDeleguate;
    private JobListService listDeleguate;
    private Client jerseyClient;
    private JobMonitoringService monitoringDeleguate;

    public RestEngineTestConfigurationProvider(TestUndertowServer server) {
        this.server = server;
    }

    @Override
    protected void before() throws Throwable {
        this.jettyClient = new HttpClient();
        this.jettyClient.start();

        this.jerseyClient = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build();

        Configuration config = defaults(UUID.randomUUID().toString()).config();
        this.queueDeleguate = PoorMansJob.queue(config);
        this.listDeleguate = PoorMansJob.list(config);
        this.monitoringDeleguate = PoorMansJob.monitor(config);

        this.server.setHandler(services().service("/queue",
                from(
                        queue(this.queueDeleguate),
                        list(this.listDeleguate),
                        monitoring(this.monitoringDeleguate)
                )
        ));


        this.configuration = RestEngineFactory.forURL(this.server.url("/queue"))
                .withOption(RestEngineFactory.JETTY_CLIENT, this.jettyClient)
                .withOption(RestEngineFactory.JERSEY_CLIENT, this.jerseyClient)
                .config();
    }

    @Override
    protected void after() {
        try {
            this.jettyClient.stop();
        } catch (Exception e) {

        }
    }

    @Override
    public Configuration getListConfig() throws ServiceFactoryException {
        return this.configuration;
    }

    @Override
    public Configuration getQueueConfig() throws ServiceFactoryException {
        return this.configuration;
    }

    @Override
    public Configuration getMonitorConfig() throws ServiceFactoryException {
        return this.configuration;
    }

    @Override
    public Configuration getDispatcherConfig() throws ServiceFactoryException {
        return this.configuration;
    }
}
