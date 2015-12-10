package org.codingmatters.poomjobs.http.sse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by nel on 09/12/15.
 */
public class ServerSentEventFuture implements Future<ServerSentEventSendingReport> {


    private final long clientCount;

    private class State {
        private long success = 0L;
        private long failures = 0L;

        public synchronized void success() {
            this.success++;
            this.notifyAll();
        }
        public synchronized void failure() {
            this.failures++;
            this.notifyAll();
        }

        public synchronized long getSuccess() {
            return this.success;
        }

        public synchronized long getFailures() {
            return this.failures;
        }

        public synchronized long count() {
            return this.success + this.failures;
        }
    }

    private final State state = new State();

    public ServerSentEventFuture(long clientCount) {
        this.clientCount = clientCount;
    }

    public void success() {
        this.state.success();
    }

    public void failure() {
        this.state.failure();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return this.state.count() >= this.clientCount;
    }

    @Override
    public ServerSentEventSendingReport get() throws InterruptedException, ExecutionException {
        synchronized (this.state) {
            while(! this.isDone()) {
                this.state.wait();
            }
        }
        return this.report();
    }

    @Override
    public ServerSentEventSendingReport get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long timeoutAsMillis = unit.toMillis(timeout);
        long start = System.currentTimeMillis();

        synchronized (this.state) {
            long elapsed = System.currentTimeMillis() - start;
            while (!this.isDone() && elapsed < timeoutAsMillis) {
                this.state.wait(timeoutAsMillis - elapsed);
                elapsed = System.currentTimeMillis() - start;
            }
        }

        if(this.isDone()) {
            return this.report();
        } else {
            throw new TimeoutException("timedout waiting send for " + timeoutAsMillis + "ms.");
        }
    }

    protected ServerSentEventSendingReport report() {
        return new ServerSentEventSendingReport(this.state.getSuccess(), this.state.getFailures());
    }
}
