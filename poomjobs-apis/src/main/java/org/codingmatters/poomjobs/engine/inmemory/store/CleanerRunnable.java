package org.codingmatters.poomjobs.engine.inmemory.store;

import java.lang.ref.WeakReference;

/**
 * Created by nel on 12/07/15.
 */
public class CleanerRunnable implements Runnable {

    private final WeakReference<InMemoryJobStore> storeWeakReference;

    public CleanerRunnable(InMemoryJobStore store) {
        this.storeWeakReference = new WeakReference<>(store);
    }

    @Override
    public void run() {
        while(true) {
            InMemoryJobStore store = this.storeWeakReference.get();
            if(store == null) {
                return ;
            }

            if(! store.isRunning()) {
                return;
            }

            store.clean();
            store = null;

            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
