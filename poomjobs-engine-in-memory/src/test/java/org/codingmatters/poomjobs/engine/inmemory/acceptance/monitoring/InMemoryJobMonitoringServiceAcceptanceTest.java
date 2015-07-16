package org.codingmatters.poomjobs.engine.inmemory.acceptance.monitoring;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.monitoring.JobMonitoringServiceAcceptanceTest;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryJobMonitoringServiceAcceptanceTest extends JobMonitoringServiceAcceptanceTest {

    @Override
    protected Configuration getMonitoringServiceConfig() throws ServiceFactoryException {
        return defaults("test").config();
    }

    @Override
    protected Configuration getQueueServiceConfig() throws ServiceFactoryException {
        return defaults("test").config();
    }

}
