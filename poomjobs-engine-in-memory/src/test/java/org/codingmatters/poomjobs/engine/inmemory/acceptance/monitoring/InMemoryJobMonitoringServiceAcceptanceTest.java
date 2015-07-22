package org.codingmatters.poomjobs.engine.inmemory.acceptance.monitoring;

import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.monitoring.JobMonitoringServiceAcceptanceTest;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;
import org.junit.Rule;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryJobMonitoringServiceAcceptanceTest extends JobMonitoringServiceAcceptanceTest {

    @Rule
    public InMemoryConfigurationProvider configurationProvider = new InMemoryConfigurationProvider();

    @Override
    protected TestConfigurationProvider getConfigurationProvider() {
        return this.configurationProvider;
    }

}
