package org.codingmatters.poomjobs.service.rest;

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

    public synchronized void set(T str) {
        this.holded = str;
    }

    public synchronized T getAndSet(T holded) {
        T result = this.holded;
        this.holded = holded;
        return result;
    }
}
