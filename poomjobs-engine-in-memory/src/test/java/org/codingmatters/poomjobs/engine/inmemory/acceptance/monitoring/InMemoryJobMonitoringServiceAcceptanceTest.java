package org.codingmatters.poomjobs.engine.inmemory.acceptance.monitoring;

import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.monitoring.JobMonitoringServiceAcceptanceTest;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryJobMonitoringServiceAcceptanceTest extends JobMonitoringServiceAcceptanceTest {

    private InMemoryConfigurationProvider configurationProvider = new InMemoryConfigurationProvider();

    @Override
    protected TestConfigurationProvider getConfigurationProvider() {
        return this.configurationProvider;
    }

}
