package org.codingmatters.poomjobs.engine.inmemory.impl.store;

import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.exception.StoreException;
import org.codingmatters.poomjobs.engine.inmemory.impl.utils.StoppableRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

/**
 * Created by nel on 12/07/15.
 */
public class CleanerRunnable extends StoppableRunnable {

    static private Logger log = LoggerFactory.getLogger(CleanerRunnable.class);

    private final WeakReference<JobStore> storeWeakReference;

    public CleanerRunnable(JobStore store) {
        this.storeWeakReference = new WeakReference<>(store);
    }

    @Override
    public void step() {
        JobStore store = this.storeWeakReference.get();
        if(store != null) {
            try {
                store.clean();
            } catch (StoreException e) {
                log.error("error cleaning job queue", e);
            }
        } else {
            this.requestStop();
        }
    }
}
