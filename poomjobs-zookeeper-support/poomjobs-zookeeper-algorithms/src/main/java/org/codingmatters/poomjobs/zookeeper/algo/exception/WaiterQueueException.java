package org.codingmatters.poomjobs.zookeeper.algo.exception;

/**
 * Created by nel on 18/09/15.
 */
public class WaiterQueueException extends Exception {
    public WaiterQueueException(String msg, Exception e) {
        super(msg, e);
    }
}
