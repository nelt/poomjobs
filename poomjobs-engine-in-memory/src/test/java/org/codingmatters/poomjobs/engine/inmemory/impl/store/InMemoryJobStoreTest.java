package org.codingmatters.poomjobs.engine.inmemory.impl.store;

import org.codingmatters.poomjobs.engine.JobStore;
import org.junit.Test;

import java.lang.ref.WeakReference;

import static java.lang.Thread.State.RUNNABLE;
import static java.lang.Thread.State.TERMINATED;
import static org.codingmatters.poomjobs.test.utils.TestHelpers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 12/07/15.
 */
public class InMemoryJobStoreTest {

    @Test
    public void testExplicitStop() throws Exception {
        JobStore store = new InMemoryJobStore();
        String threadName = "in-memory-job-store-cleaner@" + store.hashCode();
        store.start();

        assertBefore(() -> namedThreadState(threadName), is(not(TERMINATED)), 10 * 1000L);

        store.stop();
        assertThat("cleaner thread not stopped", namedThreadState(threadName), is(TERMINATED));
    }

    @Test
    public void testInMemoryJobStoreCleanerRemovedOnGC() throws Exception {
        JobStore store = new InMemoryJobStore();
        WeakReference<JobStore> storeRef = new WeakReference<JobStore>(store);

        String threadName = "in-memory-job-store-cleaner@" + store.hashCode();
        store.start();

        assertBefore(() -> namedThreadState(threadName), is(not(TERMINATED)), 10 * 1000L);

        store = null;

        waitUntil(() -> {
            System.gc();
            System.runFinalization();
            return storeRef.get() == null;
        }, 10 * 1000L);

        assertThat("store not garbage collected", storeRef.get(), is(nullValue()));

        assertBefore("cleaner thread not stopped",
                () -> namedThreadState(threadName), isOneOf(TERMINATED, RUNNABLE), 10 * 1000L);
    }

    @Test
    public void testGCTenTimes() throws Exception {
        for(int i = 0 ; i < 10 ; i++) {
            this.testInMemoryJobStoreCleanerRemovedOnGC();
        }
    }

}