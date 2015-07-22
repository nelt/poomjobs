package org.codingmatters.poomjobs.engine.inmemory.acceptance.list;

import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.list.JoblistServiceQueryAcceptanceTest;
import org.codingmatters.poomjobs.engine.inmemory.acceptance.InMemoryConfigurationProvider;

/**
 * Created by nel on 22/07/15.
 */
public class InMemoryJoblistServiceQueryAcceptanceTest extends JoblistServiceQueryAcceptanceTest {

    private final InMemoryConfigurationProvider configurationProvider = new InMemoryConfigurationProvider();
    
    @Override
    protected TestConfigurationProvider getConfigurationProvider() {
        return this.configurationProvider;
    }
}
