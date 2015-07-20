package org.codingmatters.poomjobs.test.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nel on 08/07/15.
 */
public class Helpers {
    static public <T> T[] array(T ... elements) {
        return elements;
    }

    static public <T> List<T> list(T ... elements) {
        if(elements == null) return new ArrayList<>();
        return Arrays.asList(elements);
    }

    static public Thread.State namedThreadState(String name) {
        ThreadInfo[] tis = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        for (ThreadInfo ti : tis) {
            if(name.equals(ti.getThreadName())) {
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

    public interface Condition {
        boolean is();
    }
}
