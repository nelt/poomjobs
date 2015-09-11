package org.codingmatters.poomjobs.zookeeper.test.utils;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected;

/**
 * Created by nel on 26/08/15.
 */
public class ZKUtils {
    static private final Logger log = LoggerFactory.getLogger(ZKUtils.class);

    static public void waitServerStartup(String url) throws IOException, InterruptedException {
        AtomicBoolean connected = new AtomicBoolean(false);
        ZooKeeper client = new ZooKeeper(url, 3000, event -> {
            if(SyncConnected.equals(event.getState())) {
                connected.set(true);
                synchronized (connected) {
                    connected.notifyAll();
                }
            }
        });
        try {
            synchronized (connected) {
                connected.wait();
            }
        } finally {
            client.close();
        }
    }
}
