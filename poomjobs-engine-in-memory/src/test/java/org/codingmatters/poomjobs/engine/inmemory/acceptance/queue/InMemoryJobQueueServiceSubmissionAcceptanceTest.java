package org.codingmatters.poomjobs.engine.inmemory.acceptance.queue;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.queue.JobQueueServiceSubmissionAcceptanceTest;
import org.codingmatters.poomjobs.engine.EngineConfiguration;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;
import org.junit.Before;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;

/**
 * Created by nel on 09/07/15.
 */
public class InMemoryJobQueueServiceSubmissionAcceptanceTest extends JobQueueServiceSubmissionAcceptanceTest {

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
    protected Long getExpectedDefaultRetentionDelay() {
        return EngineConfiguration.defaults().config().getDefaultRetentionDelay();
    }


}
