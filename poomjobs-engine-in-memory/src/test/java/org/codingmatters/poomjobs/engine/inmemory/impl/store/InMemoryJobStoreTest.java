package org.codingmatters.poomjobs.engine.inmemory.impl.store;

import org.junit.Test;

import java.lang.ref.WeakReference;

import static java.lang.Thread.State.TERMINATED;
import static org.codingmatters.poomjobs.test.utils.Helpers.namedThreadState;
import static org.codingmatters.poomjobs.test.utils.Helpers.waitUntil;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 12/07/15.
 */
public class InMemoryJobStoreTest {

    @Test
    public void testInMemoryJobStoreCleanerRemovedOnGC() throws Exception {
        InMemoryJobStore store = new InMemoryJobStore();
        WeakReference<InMemoryJobStore> storeRef = new WeakReference<InMemoryJobStore>(store);

        String threadName = "in-memory-job-store-cleaner@" + store.hashCode();
        store.start();

        assertThat(namedThreadState(threadName), is(not(TERMINATED)));
        store = null;
        System.gc();

        waitUntil(() -> storeRef.get() == null, 10 * 1000L);
        assertThat("store not garbage collected", storeRef.get(), is(nullValue()));

        waitUntil(() -> namedThreadState(toString()).equals(TERMINATED), 10 * 1000L);
        assertThat("cleaner thread not stopped", namedThreadState(threadName), is(TERMINATED));
    }

}