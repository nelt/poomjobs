package org.codingmatters.poomjobs.engine.rest;

import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.list.JobListServiceAcceptanceTest;
import org.codingmatters.poomjobs.http.TestUndertowServer;
import org.junit.Rule;

/**
 * Created by nel on 10/11/15.
 */
public class JettyRestEngineJobListServiceAcceptanceTest extends JobListServiceAcceptanceTest {
    @Rule
    public TestUndertowServer server = new TestUndertowServer();

    @Rule
    public RestEngineTestConfigurationProvider configurationProvider = new RestEngineTestConfigurationProvider(this.server);

    @Override
    protected TestConfigurationProvider getConfigurationProvider() {
        return this.configurationProvider;
    }
}
