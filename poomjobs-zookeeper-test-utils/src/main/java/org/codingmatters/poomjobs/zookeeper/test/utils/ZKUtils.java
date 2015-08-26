package org.codingmatters.poomjobs.zookeeper.test.utils;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected;

/**
 * Created by nel on 26/08/15.
 */
public class ZKUtils {
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
        synchronized (connected) {
            connected.wait();
        }
        client.close();
    }
}
