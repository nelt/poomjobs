package org.codingmatters.poomjobs.test.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 08/07/15.
 */
public class TestHelpers {
    static public <T> T[] array(T ... elements) {
        return elements;
    }

    static public <T> List<T> list(T ... elements) {
        if(elements == null) return new ArrayList<>();
        return Arrays.asList(elements);
    }

    static public Thread.State namedThreadState(String name) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for (long tid : threadMXBean.getAllThreadIds()) {
            ThreadInfo ti = threadMXBean.getThreadInfo(tid);
            if(ti != null && name.equals(ti.getThreadName())) {
                return ti.getThreadState();
            }
        }
        return Thread.State.TERMINATED;
    }

    static public void printThreads() {
        ThreadInfo[] tis = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        System.out.println(("threads :"));
        for (ThreadInfo ti : tis) {
            System.out.println("\t" + ti.getThreadName());
        }
    }

    public static void waitUntil(Condition condition, long timeout) throws Exception {
        long delay = 10L;
        long waited = 0L;
        while(waited < timeout && ! condition.is()) {
            Thread.sleep(delay);
            waited += delay;
        }
    }

    public static void assertOccuresBefore(Condition condition, long timeout) throws Exception {
        waitUntil(condition, timeout);
        assertThat(condition.is(), is(true));
    }



    public static <T> T[] range(T[] all, int from, int to) {
        return Arrays.copyOfRange(all, from, to);
    }

    public interface Condition {
        boolean is() throws Exception;
    }
}
