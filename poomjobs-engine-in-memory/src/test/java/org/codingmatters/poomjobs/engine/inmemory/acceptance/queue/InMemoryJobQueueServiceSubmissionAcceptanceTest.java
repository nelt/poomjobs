package org.codingmatters.poomjobs.engine.inmemory.acceptance.queue;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
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
    protected TestConfigurationProvider getConfigurationProvider() {
        return this.configurationProvider;
    }

}
