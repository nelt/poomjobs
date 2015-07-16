package org.codingmatters.poomjobs.engine.inmemory.acceptance.monitoring;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.monitoring.JobMonitoringServiceAcceptanceTest;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;
import org.junit.Before;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryJobMonitoringServiceAcceptanceTest extends JobMonitoringServiceAcceptanceTest {

    private InMemoryConfigurationProvider configurationProvider = new InMemoryConfigurationProvider();

    @Override
    @Before
    public void setUp() throws Exception {
        this.configurationProvider.initialize();
        super.setUp();
    }

    @Override
    protected Configuration getMonitoringServiceConfig() throws ServiceFactoryException {
        return this.configurationProvider.getMonitorConfig();
    }

    @Override
    protected Configuration getQueueServiceConfig() throws ServiceFactoryException {
        return this.configurationProvider.getQueueConfig();
    }

}
