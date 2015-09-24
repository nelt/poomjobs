package org.codingmatters.poomjobs.test.utils;

/**
 * Created by nel on 23/09/15.
 */
public interface Runner {

    class Run {
        static public void inThread(Runner r) {
            new Thread(() -> {
                try {
                    r.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    void run() throws Exception;
}
