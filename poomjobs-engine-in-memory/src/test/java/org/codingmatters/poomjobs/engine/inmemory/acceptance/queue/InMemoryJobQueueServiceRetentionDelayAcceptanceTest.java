package org.codingmatters.poomjobs.engine.inmemory.acceptance.queue;

import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.queue.JobQueueServiceRetentionDelayAcceptanceTest;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;
import org.junit.Rule;

/**
 * Created by nel on 09/11/15.
 */
public class InMemoryJobQueueServiceRetentionDelayAcceptanceTest extends JobQueueServiceRetentionDelayAcceptanceTest {

    @Rule
    public InMemoryConfigurationProvider configurationProvider = new InMemoryConfigurationProvider();

    @Override
    protected TestConfigurationProvider getConfigurationProvider() {
        return this.configurationProvider;
    }
}
