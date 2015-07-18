package org.codingmatters.poomjobs.engine.inmemory.acceptance;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;

import java.util.UUID;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryConfigurationProvider implements TestConfigurationProvider {
    private String name;

    public InMemoryConfigurationProvider() {
        this.initialize();
    }

    @Override
    public void initialize() {
        this.name = UUID.randomUUID().toString();
    }

    @Override
    public Configuration getListConfig() throws ServiceFactoryException {
        return defaults(this.name).config();
    }

    @Override
    public Configuration getQueueConfig() throws ServiceFactoryException {
        return defaults(this.name).config();
    }

    @Override
    public Configuration getMonitorConfig() throws ServiceFactoryException {
        return defaults(this.name).config();
    }

    @Override
    public Configuration getDispatcherConfig() throws ServiceFactoryException {
        return defaults(this.name).config();
    }
}
