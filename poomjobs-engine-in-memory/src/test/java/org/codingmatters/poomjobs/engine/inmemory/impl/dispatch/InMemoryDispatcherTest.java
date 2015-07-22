package org.codingmatters.poomjobs.engine.inmemory.impl.dispatch;

import org.codingmatters.poomjobs.engine.inmemory.impl.store.InMemoryJobStore;
import org.junit.Test;

import java.lang.ref.WeakReference;

import static java.lang.Thread.State.TERMINATED;
import static org.codingmatters.poomjobs.test.utils.Helpers.namedThreadState;
import static org.codingmatters.poomjobs.test.utils.Helpers.waitUntil;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 17/07/15.
 */
public class InMemoryDispatcherTest {

    @Test
    public void testExplicitStop() throws Exception {
        InMemoryDispatcher dispatcher = new InMemoryDispatcher(new InMemoryJobStore(), new MockedJobQueueService());
        String threadName = "in-memory-dispatcher@" + dispatcher.hashCode();
        dispatcher.start();

        assertThat(namedThreadState(threadName), is(not(TERMINATED)));

        dispatcher.stop();

        assertThat("dispatcher thread not stopped", namedThreadState(threadName), is(TERMINATED));
    }

    @Test
    public void testDispatcherThreadStoppedWhenDispatcherGarbageCollected() throws Exception {
        InMemoryDispatcher dispatcher = new InMemoryDispatcher(new InMemoryJobStore(), new MockedJobQueueService());
        WeakReference<InMemoryDispatcher> dispatcherRef = new WeakReference<>(dispatcher);

        String threadName = "in-memory-dispatcher@" + dispatcher.hashCode();
        dispatcher.start();

        waitUntil(() -> namedThreadState(threadName) != TERMINATED, 10 * 1000L);
        assertThat(namedThreadState(threadName), is(not(TERMINATED)));

        dispatcher = null;

        waitUntil(() -> {
            System.gc();
            System.runFinalization();
            return dispatcherRef.get() == null;
        }, 10 * 1000L);

        assertThat("dispatcher not garbage collected", dispatcherRef.get(), is(nullValue()));

        waitUntil(() -> namedThreadState(threadName).equals(TERMINATED), 10 * 1000L);
        assertThat("dispatcher thread not stopped", namedThreadState(threadName), is(TERMINATED));
    }

    @Test
    public void testGCTenTimes() throws Exception {
        for(int i = 0 ; i < 10 ; i++) {
            this.testDispatcherThreadStoppedWhenDispatcherGarbageCollected();
        }
    }
}