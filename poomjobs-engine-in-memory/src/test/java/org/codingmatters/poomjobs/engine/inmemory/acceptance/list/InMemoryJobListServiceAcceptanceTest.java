package org.codingmatters.poomjobs.engine.inmemory.acceptance.list;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.list.JobListServiceAcceptanceTest;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;

/**
 * Created by nel on 06/07/15.
 */
public class InMemoryJobListServiceAcceptanceTest extends JobListServiceAcceptanceTest {
    @Override
    protected Configuration getListServiceConfig() throws ServiceFactoryException {
        return defaults("test").config();
    }

    @Override
    protected Configuration getQueueServiceConfig() throws ServiceFactoryException {
        return defaults("test").config();
    }

}