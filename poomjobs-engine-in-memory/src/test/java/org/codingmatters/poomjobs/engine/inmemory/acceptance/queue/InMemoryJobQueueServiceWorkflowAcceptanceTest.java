package org.codingmatters.poomjobs.engine.inmemory.acceptance.queue;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.queue.JobQueueServiceWorkflowAcceptanceTest;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;
import org.junit.Before;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;

/**
 * Created by nel on 08/07/15.
 */
public class InMemoryJobQueueServiceWorkflowAcceptanceTest extends JobQueueServiceWorkflowAcceptanceTest {

    private InMemoryConfigurationProvider configurationProvider = new InMemoryConfigurationProvider();

    @Override
    protected TestConfigurationProvider getConfigurationProvider() {
        return this.configurationProvider;
    }

}
