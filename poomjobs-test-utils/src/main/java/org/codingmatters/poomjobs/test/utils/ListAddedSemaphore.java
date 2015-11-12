package org.codingmatters.poomjobs.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by nel on 12/11/15.
 */
public class ListAddedSemaphore<T> {
    static private final Logger log = LoggerFactory.getLogger(ListAddedSemaphore.class);

    private final List<T> list = new LinkedList<T>();
    private int originalSize = 0;

    public synchronized void mark() {
        this.originalSize = this.list.size();
    }

    public synchronized void add(T element) {
        this.list.add(element);
        log.debug("added {}", element);
    }

    public synchronized List<T> elements() {
        return new LinkedList<>(this.list);
    }

    public synchronized List<T> waitAdded(long timeout) throws InterruptedException {
        return this.waitAdded(1, timeout);
    }

    public synchronized List<T> waitAdded(int increment, long timeout) throws InterruptedException {
        long start = System.currentTimeMillis();

        if(this.notIncremented(increment)) {
            do {
                this.wait(100);
            } while (this.notIncremented(increment) && System.currentTimeMillis() - start < timeout);
        }

        if(this.notIncremented(increment)) {
            throw new AssertionError("collection size was not incremented. " +
                    "Expected " + (this.originalSize + increment) + " but was " + this.list.size());
        } else {
            return this.elements();
        }
    }

    protected boolean notIncremented(int increment) {
        return this.list.size() < this.originalSize + increment;
    }

}
