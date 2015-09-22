package org.codingmatters.poomjobs.zookeeper.client;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.LinkedList;

/**
 * Created by nel on 11/09/15.
 */
public class WatcherChain implements Watcher {
    private final LinkedList<Watcher> watchers = new LinkedList<>();

    public WatcherChain watches(Watcher... watchers) {
        if (watchers != null) {
            for (Watcher watcher : watchers) {
                if (watcher != null) {
                    this.watchers.add(watcher);
                }
            }
        }
        return this;
    }

    @Override
    public void process(WatchedEvent event) {
        for (Watcher watcher : this.watchers) {
            watcher.process(event);
        }
    }
}
