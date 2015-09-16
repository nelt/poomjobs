package org.codingmatters.poomjobs.test.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 16/09/15.
 */
public class ValueChangeSemaphoreTest {

    static private final Logger log = LoggerFactory.getLogger(ValueChangeSemaphoreTest.class);

    @Test
    public void testAlreadySet() throws Exception {
        ValueChangeSemaphore<Boolean> semaphore = new ValueChangeSemaphore<>(true);

        long start = System.currentTimeMillis();
        assertThat(semaphore.waitForValue(true, 1000L), is(true));
        assertThat(System.currentTimeMillis() - start, is(lessThan(100L)));
    }

    @Test
    public void testNominal() throws Exception {
        ValueChangeSemaphore<Boolean> semaphore = new ValueChangeSemaphore<>(false);

        long start = System.currentTimeMillis();
        
        new Thread(this.delayed(500L, () -> semaphore.set(true))).run();
        Boolean actual = semaphore.waitForValue(true, 1000L);

        long elapsed = System.currentTimeMillis() - start;

        assertThat(actual, is(true));
        assertThat(elapsed, is(greaterThan(200L)));
        assertThat(elapsed, is(lessThan(800L)));
    }

    @Test
    public void testTimeout() throws Exception {
        ValueChangeSemaphore<Boolean> semaphore = new ValueChangeSemaphore<>(false);

        long start = System.currentTimeMillis();

        Boolean actual = semaphore.waitForValue(true, 1000L);

        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed, is(greaterThanOrEqualTo(1000L)));
        assertThat(actual, is(false));
    }



    private Runnable delayed(long delay, Runnable runnable) {
        return () -> {
            try {
                log.debug("1 {}", delay);
                Thread.sleep(delay);
                log.debug("2");
                runnable.run();
                log.debug("3");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }
}