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
}
