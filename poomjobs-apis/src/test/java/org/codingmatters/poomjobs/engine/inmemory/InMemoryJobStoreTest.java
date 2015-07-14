package org.codingmatters.poomjobs.engine.inmemory;

import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Created by nel on 12/07/15.
 */
public class InMemoryJobStoreTest {

    @Test
    public void testRunningThreads() throws Exception {
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

    private void printThreadNames() {
        ThreadInfo[] tis = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        for (ThreadInfo ti : tis) {
            System.out.println(ti.getThreadName());
        }
    }
}