package org.codingmatters.poomjobs.engine.rest;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.services.list.JobListService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.http.RestServiceHandler;
import org.codingmatters.poomjobs.http.TestUndertowServer;
import org.codingmatters.poomjobs.service.rest.PoomjobRestServices;
import org.eclipse.jetty.client.HttpClient;
import org.junit.rules.ExternalResource;

import java.util.UUID;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;

/**
 * Created by nel on 09/11/15.
 */
public class RestEngineTestConfigurationProvider extends ExternalResource implements TestConfigurationProvider {

    private final TestUndertowServer server;

    private HttpClient httpClient;
    private Configuration configuration;
    private JobQueueService queueDeleguate;
    private JobListService listDeleguate;

    public RestEngineTestConfigurationProvider(TestUndertowServer server) {
        this.server = server;
    }

    @Override
    protected void before() throws Throwable {
        this.httpClient = new HttpClient();
        this.httpClient.start();

        Configuration config = defaults(UUID.randomUUID().toString()).config();
        this.queueDeleguate = PoorMansJob.queue(config);
        this.listDeleguate = PoorMansJob.list(config);
        this.server.setHandler(RestServiceHandler.from(
                PoomjobRestServices.queueService("/queue", this.queueDeleguate, this.listDeleguate))
        );

        this.configuration = RestEngineFactory.forURL(this.server.url("/queue"), this.httpClient).config();
    }

    @Override
    protected void after() {
        try {
            this.httpClient.stop();
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
