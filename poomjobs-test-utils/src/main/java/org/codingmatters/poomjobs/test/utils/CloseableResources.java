package org.codingmatters.poomjobs.test.utils;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * Created by nel on 11/09/15.
 */
public class CloseableResources extends ExternalResource {

    static private final Logger log = LoggerFactory.getLogger(CloseableResources.class);

    private final LinkedList<AutoCloseable> closeables = new LinkedList<>();

    @Override
    protected void before() throws Throwable {
    }

    @Override
    protected void after() {
        for (AutoCloseable closeable : this.closeables) {
            if(closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    log.error("error while closing resource", e);
                }
            }
        }
        this.closeables.clear();
    }

    public <T extends AutoCloseable> T add(T closeable) {
        this.closeables.add(closeable);
        return closeable;
    }
}
