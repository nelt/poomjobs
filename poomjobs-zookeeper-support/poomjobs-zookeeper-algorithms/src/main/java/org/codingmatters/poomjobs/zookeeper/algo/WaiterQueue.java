package org.codingmatters.poomjobs.zookeeper.algo;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZooKlient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nel on 15/09/15.
 */
public class WaiterQueue {

    static private final Logger log = LoggerFactory.getLogger(WaiterQueue.class);

    private final ZooKlient klient;
    private final String root;
    private final List<ACL> acl;
    private final ExecutorService service;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final HashMap<String, Waiter> waiting = new HashMap<>();

    public WaiterQueue(ZooKlient klient, String root, List<ACL> acl, ExecutorService service) {
        this.klient = klient;
        this.root = root;
        this.acl = acl;
        this.service = service;
    }

    public void waitMyTurn(Waiter waiter) {
        this.initialize();
        try {
            String waiterPath = this.klient.operate(keeper -> keeper.create(this.root + "/waiting-", new byte[0], this.acl, CreateMode.EPHEMERAL_SEQUENTIAL));
            synchronized (this.waiting) {
                waiterPath = waiterPath.substring(this.root.length() + 1);
                this.waiting.put(waiterPath, waiter);
                log.trace("waiter created for path {}", waiterPath);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    protected void initialize() {
        synchronized (this.initialized) {
            if(! this.initialized.get()) {
                this.registerQueueWatcher();
                this.initialized.set(false);
                log.trace("queue initialized");
            }
        }
    }

    private void registerQueueWatcher() {
        try {
            this.klient.operate(keeper -> keeper.getChildren(this.root, this::queueChanged));
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void queueChanged(WatchedEvent event) {
        if(event.getType().equals(Watcher.Event.EventType.NodeChildrenChanged)) {
            this.registerQueueWatcher();

            log.trace("node children changed for {}", event.getPath());
            try {
                List<String> children = this.klient.operate(keeper -> keeper.getChildren(event.getPath(), false));
                Collections.sort(children);

                log.trace("sorted children : {}", children);
                synchronized (this.waiting) {
                    String nextInLine = children.get(0);
                    if(this.waiting.containsKey(nextInLine)) {
                        Waiter waiter = this.waiting.remove(nextInLine);
                        log.trace("waiting waiter for {}", nextInLine);
                        this.service.execute(() -> this.execute(waiter, this.root + "/" + nextInLine));
                    } else {
                        log.trace("no waiters registered here for {}", nextInLine);
                    }
                }

            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void execute(Waiter waiter, String waiterNode) {
        waiter.nextInLine();
        try {
            this.klient.operate(keeper -> {
                keeper.delete(waiterNode, -1);
                return null;
            });
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    interface Waiter {
        void nextInLine();
    }
}
