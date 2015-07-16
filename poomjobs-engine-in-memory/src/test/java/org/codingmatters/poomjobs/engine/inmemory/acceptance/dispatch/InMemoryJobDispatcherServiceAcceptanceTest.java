package org.codingmatters.poomjobs.engine.inmemory.acceptance.dispatch;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.dispatch.JobDispatcherServiceAcceptanceTest;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;
import org.junit.Before;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryJobDispatcherServiceAcceptanceTest extends JobDispatcherServiceAcceptanceTest {

    private InMemoryConfigurationProvider configurationProvider = new InMemoryConfigurationProvider();

    @Override
    @Before
    public void setUp() throws Exception {
        this.configurationProvider.initialize();
        super.setUp();
    }

    @Override
    protected Configuration getQueueServiceConfig() throws ServiceFactoryException {
        return this.configurationProvider.getQueueConfig();
    }

    @Override
    protected Configuration getDispatcherServiceConfig() throws ServiceFactoryException {
        return this.configurationProvider.getDispatcherConfig();
    }

}
