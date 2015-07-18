package org.codingmatters.poomjobs.engine.inmemory.acceptance.dispatch;

import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.dispatch.JobDispatcherServiceAcceptanceTest;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryJobDispatcherServiceAcceptanceTest extends JobDispatcherServiceAcceptanceTest {

    private InMemoryConfigurationProvider configurationProvider = new InMemoryConfigurationProvider();

    @Override
    protected TestConfigurationProvider getConfigurationProvider() {
        return this.configurationProvider;
    }

}
