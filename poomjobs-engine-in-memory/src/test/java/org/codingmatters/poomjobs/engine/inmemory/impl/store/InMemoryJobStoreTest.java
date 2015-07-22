package org.codingmatters.poomjobs.engine.inmemory.impl.store;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.lang.ref.WeakReference;

import static java.lang.Thread.State.RUNNABLE;
import static java.lang.Thread.State.TERMINATED;
import static org.codingmatters.poomjobs.test.utils.Helpers.namedThreadState;
import static org.codingmatters.poomjobs.test.utils.Helpers.waitUntil;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 12/07/15.
 */
public class InMemoryJobStoreTest {

    @Test
    public void testExplicitStop() throws Exception {
        InMemoryJobStore store = new InMemoryJobStore();
        String threadName = "in-memory-job-store-cleaner@" + store.hashCode();
        store.start();

        waitUntil(() -> namedThreadState(threadName) != TERMINATED, 10 * 1000L);
        assertThat(namedThreadState(threadName), is(not(TERMINATED)));

        store.stop();
        assertThat("cleaner thread not stopped", namedThreadState(threadName), is(TERMINATED));
    }

    @Test
    public void testInMemoryJobStoreCleanerRemovedOnGC() throws Exception {
        InMemoryJobStore store = new InMemoryJobStore();
        WeakReference<InMemoryJobStore> storeRef = new WeakReference<InMemoryJobStore>(store);

        String threadName = "in-memory-job-store-cleaner@" + store.hashCode();
        store.start();

        waitUntil(() -> namedThreadState(threadName) != TERMINATED, 10 * 1000L);
        assertThat(namedThreadState(threadName), is(not(TERMINATED)));

        store = null;

        waitUntil(() -> {
            System.gc();
            System.runFinalization();
            return storeRef.get() == null;
        }, 10 * 1000L);

        assertThat("store not garbage collected", storeRef.get(), is(nullValue()));

        waitUntil(() -> namedThreadState(toString()).equals(TERMINATED), 10 * 1000L);
        assertThat("cleaner thread not stopped", namedThreadState(threadName), isOneOf(TERMINATED, RUNNABLE));
    }

    @Test
    public void testGCTenTimes() throws Exception {
        for(int i = 0 ; i < 10 ; i++) {
            this.testInMemoryJobStoreCleanerRemovedOnGC();
        }
    }

}