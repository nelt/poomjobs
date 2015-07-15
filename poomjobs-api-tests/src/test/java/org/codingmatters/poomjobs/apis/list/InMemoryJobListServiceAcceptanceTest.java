package org.codingmatters.poomjobs.apis.list;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;

import static org.codingmatters.poomjobs.apis.Configuration.defaults;

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