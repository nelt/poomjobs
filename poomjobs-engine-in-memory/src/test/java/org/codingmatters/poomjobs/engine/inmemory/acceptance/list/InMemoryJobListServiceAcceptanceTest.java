package org.codingmatters.poomjobs.engine.inmemory.acceptance.list;

import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.list.JobListServiceAcceptanceTest;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;

/**
 * Created by nel on 06/07/15.
 */
public class InMemoryJobListServiceAcceptanceTest extends JobListServiceAcceptanceTest {

    private final InMemoryConfigurationProvider configurationProvider = new InMemoryConfigurationProvider();

    @Override
    protected TestConfigurationProvider getConfigurationProvider() {
        return this.configurationProvider;
    }

}