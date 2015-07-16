package org.codingmatters.poomjobs.engine.inmemory.acceptance.list;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.list.JobListServiceAcceptanceTest;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;
import org.junit.Before;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;

/**
 * Created by nel on 06/07/15.
 */
public class InMemoryJobListServiceAcceptanceTest extends JobListServiceAcceptanceTest {

    private InMemoryConfigurationProvider configurationProvider = new InMemoryConfigurationProvider();

    @Override
    @Before
    public void setUp() throws Exception {
        this.configurationProvider.initialize();
        super.setUp();
    }


    @Override
    protected Configuration getListServiceConfig() throws ServiceFactoryException {
        return this.configurationProvider.getListConfig();
    }

    @Override
    protected Configuration getQueueServiceConfig() throws ServiceFactoryException {
        return this.configurationProvider.getQueueConfig();
    }

}