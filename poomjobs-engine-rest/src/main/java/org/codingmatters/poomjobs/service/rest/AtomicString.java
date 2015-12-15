package org.codingmatters.poomjobs.service.rest;

/**
 * Created by nel on 14/12/15.
 */
public class AtomicString {
    private String str;

    public AtomicString(String str) {
        this.str = str;
    }

    public synchronized String get() {
        return this.str;
    }

    public synchronized void set(String str) {
        this.str = str;
    }

    public synchronized String getAndSet(String str) {
        String result = this.str;
        this.str = str;
        return result;
    }
}
