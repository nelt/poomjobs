package org.codingmatters.poomjobs.engine.inmemory.impl.store;

import org.codingmatters.poomjobs.test.utils.Helpers;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

import static java.lang.Thread.State.TERMINATED;
import static org.codingmatters.poomjobs.test.utils.Helpers.namedThreadState;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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

        assertThat(namedThreadState(cleanerThreadName), is(not(TERMINATED)));
        store = null;
        System.gc();
        Thread.sleep(500L);
        assertThat(namedThreadState(cleanerThreadName), is(TERMINATED));
    }

}