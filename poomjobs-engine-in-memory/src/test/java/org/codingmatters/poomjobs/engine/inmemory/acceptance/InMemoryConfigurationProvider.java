package org.codingmatters.poomjobs.engine.inmemory.acceptance;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.engine.inmemory.impl.InMemoryEngine;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.UUID;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryConfigurationProvider implements TestConfigurationProvider, TestRule {
    private String name;

    public InMemoryConfigurationProvider() {
        this.setUp();
    }

    private void setUp() {
        this.name = UUID.randomUUID().toString();
        InMemoryEngine.getEngine(defaults(this.name).config());
    }

    private void tearDown() {
        InMemoryEngine.removeEngine(defaults(this.name).config());
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

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setUp();
                try {
                    base.evaluate();
                } finally {
                    tearDown();
                }
            }
        };
    }
}
