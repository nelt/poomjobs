package org.codingmatters.poomjobs.engine.inmemory.impl.utils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nel on 18/07/15.
 */
public abstract class StoppableRunnable implements Runnable {

    public abstract void step() ;

    private final AtomicBoolean run = new AtomicBoolean(false);

    public void requestStart() {
        this.run.set(true);
    }

    public void requestStop() {
        this.run.set(false);
        synchronized (this) {
            this.notifyAll();
        }
    }

    @Override
    public void run() {
        while(this.run.get()) {

            this.step();

            if (!this.run.get()) return;

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
