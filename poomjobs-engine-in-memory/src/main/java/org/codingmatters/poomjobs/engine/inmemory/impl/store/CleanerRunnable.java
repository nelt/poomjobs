package org.codingmatters.poomjobs.engine.inmemory.impl.store;

import org.codingmatters.poomjobs.engine.inmemory.impl.utils.StoppableRunnable;

import java.lang.ref.WeakReference;

/**
 * Created by nel on 12/07/15.
 */
public class CleanerRunnable extends StoppableRunnable {

    private final WeakReference<InMemoryJobStore> storeWeakReference;

    public CleanerRunnable(InMemoryJobStore store) {
        this.storeWeakReference = new WeakReference<>(store);
    }

    @Override
    public void step() {
        InMemoryJobStore store = this.storeWeakReference.get();
        if(store != null) {
            store.clean();
        } else {
            this.requestStop();
        }
    }
}
