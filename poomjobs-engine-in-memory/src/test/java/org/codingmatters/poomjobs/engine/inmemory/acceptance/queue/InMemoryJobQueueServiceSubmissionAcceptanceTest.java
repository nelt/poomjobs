package org.codingmatters.poomjobs.engine.inmemory.acceptance.queue;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.queue.JobQueueServiceSubmissionAcceptanceTest;
import org.codingmatters.poomjobs.engine.EngineConfiguration;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;

/**
 * Created by nel on 09/07/15.
 */
public class InMemoryJobQueueServiceSubmissionAcceptanceTest extends JobQueueServiceSubmissionAcceptanceTest {


    @Override
    protected Configuration getQueueServiceConfig() throws ServiceFactoryException {
        return defaults("test").config();
    }

    @Override
    protected Long getExpectedDefaultRetentionDelay() {
        return EngineConfiguration.defaults().config().getDefaultRetentionDelay();
    }


}
