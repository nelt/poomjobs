package org.codingmatters.poomjobs.test.utils;

/**
 * Created by nel on 16/09/15.
 */
public class ValueChangeSemaphore<T> {
    private T value;

    public ValueChangeSemaphore(T initialValue) {
        this.value = initialValue;
    }

    public synchronized T set(T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        this.notifyAll();
        return oldValue;
    }

    public synchronized T get() {
        return this.value;
    }

    public synchronized T waitForValue(T awaited, long timeout) throws InterruptedException {
        long start = System.currentTimeMillis();

        if(! this.same(this.value, awaited)) {
            do {
                this.wait(100);
            } while (!this.same(this.value, awaited) && System.currentTimeMillis() - start < timeout);
        }
        return this.value;
    }

    public boolean same(T value1, T value2) {
        if(value1 == null) {
            return value2 == null;
        } else {
            return value1.equals(value2);
        }
    }

}
