package org.codingmatters.poomjobs.engine.inmemory.impl.dispatch;

import java.lang.ref.WeakReference;

/**
 * Created by nel on 18/07/15.
 */
public class DispatcherRunnable implements Runnable {
    private final WeakReference<InMemoryDispatcher> dispatcherReference;

    public DispatcherRunnable(InMemoryDispatcher dispatcher) {
        this.dispatcherReference = new WeakReference<InMemoryDispatcher>(dispatcher);
    }

    @Override
    public void run() {
        while(true) {
            InMemoryDispatcher dispatcher = this.dispatcherReference.get();
            if(dispatcher == null) return;
            if(! dispatcher.isRunning()) return;

            dispatcher.dispatch();
            dispatcher = null;

            try {
                synchronized (this) {
                    this.wait(100L);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
