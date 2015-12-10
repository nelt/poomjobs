package org.codingmatters.poomjobs.http.sse;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Created by nel on 09/12/15.
 */
public class ServerSentEventFutureTest {

    @Test
    public void testNothingToWaitFor() throws Exception {
        assertTrue(new ServerSentEventFuture(0).isDone());
    }

    @Test
    public void testWaitForOneSuccess() throws Exception {
        ServerSentEventFuture future = new ServerSentEventFuture(1);

        assertFalse(future.isDone());

        final ServerSentEventSendingReport[] report = {null};
        Thread t = this.start(() -> report[0] = future.get());

        future.success();
        assertTrue(future.isDone());

        t.join();

        assertThat(report[0], is(new ServerSentEventSendingReport(1L, 0L)));
    }

    @Test
    public void testWaitForOneSuccessAndOneFailure() throws Exception {
        ServerSentEventFuture future = new ServerSentEventFuture(2);

        assertFalse(future.isDone());

        final ServerSentEventSendingReport[] report = {null};
        Thread t = this.start(() -> report[0] = future.get());

        future.success();
        future.failure();

        assertTrue(future.isDone());

        t.join();

        assertThat(report[0], is(new ServerSentEventSendingReport(1L, 1L)));
    }


    @Test
    public void testSuccessfulTimeout() throws Exception {
        ServerSentEventFuture future = new ServerSentEventFuture(1);

        Thread t = this.start(() -> future.get(100, TimeUnit.MILLISECONDS));

        future.success();
        assertTrue(future.isDone());

        t.join();
    }

    @Test(expected = TimeoutException.class)
    public void testFailedTimeout() throws Exception {
        ServerSentEventFuture future = new ServerSentEventFuture(1);
        System.out.println(future.get(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testTimedGet() throws Exception {
        ServerSentEventFuture future = new ServerSentEventFuture(2);

        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch(TimeoutException e) {}
        assertFalse(future.isDone());

        future.success();


        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch(TimeoutException e) {}
        assertFalse(future.isDone());

        future.success();

        future.get();
        assertTrue(future.isDone());
    }

    private Thread start(Async async) {
        Thread result = new Thread(() -> {
            try {
                async.async();
            } catch (Exception e) {
                throw new AssertionError("error executing async code", e);
            }
        });
        result.start();
        return result;
    }

    interface Async {
        void async() throws Exception;
    }
}