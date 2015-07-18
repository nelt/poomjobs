package org.codingmatters.poomjobs.engine.inmemory.impl.dispatch;

import org.codingmatters.poomjobs.engine.inmemory.impl.utils.StoppableRunnable;

import java.lang.ref.WeakReference;

/**
 * Created by nel on 18/07/15.
 */
public class DispatcherRunnable extends StoppableRunnable {
    private final WeakReference<InMemoryDispatcher> dispatcherReference;

    public DispatcherRunnable(InMemoryDispatcher dispatcher) {
        this.dispatcherReference = new WeakReference<>(dispatcher);
    }

    @Override
    public void step() {
        InMemoryDispatcher dispatcher = this.dispatcherReference.get();
        if(dispatcher != null) {
            dispatcher.dispatch();
        } else {
            this.requestStop();
        }
    }
}
