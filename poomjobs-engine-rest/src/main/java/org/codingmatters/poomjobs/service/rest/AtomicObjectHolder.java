package org.codingmatters.poomjobs.service.rest;

import java.util.concurrent.TimeoutException;

/**
 * Created by nel on 15/12/15.
 */
public class AtomicObjectHolder<T> {
    private T holded;

    public AtomicObjectHolder(T holded) {
        this.holded = holded;
    }

    public synchronized T get() {
        return this.holded;
    }

    public synchronized void set(T holded) {
        this.holded = holded;
        this.notifyAll();
    }

    public synchronized T getAndSet(T holded) {
        T result = this.holded;
        this.set(holded);
        return result;
    }

    public synchronized T setIfNull(T holded) {
        if(this.holded == null) {
            this.set(holded);
        }
        return this.holded;
    }

    private synchronized T waitWhile(Cond condition, long timeout) throws InterruptedException, TimeoutException {
        long start = System.currentTimeMillis();
        while(condition.is() && System.currentTimeMillis() - start < timeout) {
            this.wait(timeout - (System.currentTimeMillis() - start));
        }
        if(condition.is()) {
            throw new TimeoutException();
        }
        return this.holded;
    }
    public synchronized T waitUntilNot(T value, long timeout) throws InterruptedException, TimeoutException {
        return this.waitWhile(() -> this.same(value), timeout);
    }

    public synchronized T waitWhile(T value, long timeout) throws InterruptedException, TimeoutException {
        return this.waitWhile(() -> ! this.same(value), timeout);
    }

    public interface Cond {
        boolean is();
    }

    private boolean same(T value) {
        if(this.holded == null) {
            return value == null;
        } else {
            return this.holded.equals(value);
        }
    }
}
