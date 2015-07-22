package org.codingmatters.poomjobs.apis;

import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;

/**
 * Created by nel on 18/07/15.
 */
public interface TestConfigurationProvider {
    Configuration getListConfig() throws ServiceFactoryException;

    Configuration getQueueConfig() throws ServiceFactoryException;

    Configuration getMonitorConfig() throws ServiceFactoryException;

    Configuration getDispatcherConfig() throws ServiceFactoryException;
}
