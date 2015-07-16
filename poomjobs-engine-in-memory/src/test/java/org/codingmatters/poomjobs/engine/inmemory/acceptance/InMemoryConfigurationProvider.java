package org.codingmatters.poomjobs.engine.inmemory.acceptance;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;

import java.util.UUID;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryConfigurationProvider {
    private String name;

    public InMemoryConfigurationProvider() {
        this.initialize();
    }

    public void initialize() {
        this.name = UUID.randomUUID().toString();
    }

    public Configuration getListConfig() throws ServiceFactoryException {
        return defaults(this.name).config();
    }

    public Configuration getQueueConfig() throws ServiceFactoryException {
        return defaults(this.name).config();
    }

    public Configuration getMonitorConfig() throws ServiceFactoryException {
        return defaults(this.name).config();
    }

    public Configuration getDispatcherConfig() throws ServiceFactoryException {
        return defaults(this.name).config();
    }
}
