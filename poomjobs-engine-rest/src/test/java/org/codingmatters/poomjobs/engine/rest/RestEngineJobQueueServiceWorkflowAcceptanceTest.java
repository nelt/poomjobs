package org.codingmatters.poomjobs.engine.rest;

import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.queue.JobQueueServiceWorkflowAcceptanceTest;
import org.codingmatters.poomjobs.http.TestUndertowServer;
import org.junit.Rule;

/**
 * Created by nel on 09/11/15.
 */
public class RestEngineJobQueueServiceWorkflowAcceptanceTest extends JobQueueServiceWorkflowAcceptanceTest {
    @Rule
    public TestUndertowServer server = new TestUndertowServer();

    @Rule
    public RestEngineTestConfigurationProvider configurationProvider = new RestEngineTestConfigurationProvider(this.server);

    @Override
    protected TestConfigurationProvider getConfigurationProvider() {
        return this.configurationProvider;
    }
}
