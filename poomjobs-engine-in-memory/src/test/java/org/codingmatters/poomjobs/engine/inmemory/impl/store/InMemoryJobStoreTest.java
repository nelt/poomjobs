package org.codingmatters.poomjobs.engine.inmemory.impl.store;

import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 12/07/15.
 */
public class InMemoryJobStoreTest {

    @Test
    public void testInMemoryJobStoreCleanerRemovedOnGC() throws Exception {
        InMemoryJobStore store = new InMemoryJobStore();
        String cleanerThreadName = "in-memory-job-store-cleaner@" + store.hashCode();
        store.startCleanerThread();

        assertThat(this.namedThreadIsRunning(cleanerThreadName), is(true));
        store = null;
        System.gc();
        Thread.sleep(500L);
        assertThat(this.namedThreadIsRunning(cleanerThreadName), is(false));
    }


    private boolean namedThreadIsRunning(String name) {
        ThreadInfo[] tis = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        for (ThreadInfo ti : tis) {
            if(name.equals(ti.getThreadName())) {
                return true;
            }
        }
        return false;
    }

}