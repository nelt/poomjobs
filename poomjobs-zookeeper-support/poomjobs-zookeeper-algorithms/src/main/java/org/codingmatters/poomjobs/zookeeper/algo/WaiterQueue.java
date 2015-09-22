package org.codingmatters.poomjobs.zookeeper.algo;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.codingmatters.poomjobs.zookeeper.algo.exception.WaiterQueueException;
import org.codingmatters.poomjobs.zookeeper.client.ZooKlient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    public WaiterQueue(ZooKlient klient, String root, List<ACL> acl) {
        this.klient = klient;
        this.root = root;
        this.acl = acl;
        this.service = Executors.newSingleThreadExecutor(r -> new Thread(r, "waiter-queue-executor-" + this.root));
    }

    public void waitMyTurn(Waiter waiter) throws WaiterQueueException {
        this.initialize();
        try {
            String waiterPath = this.klient.operate(keeper -> keeper.create(this.root + "/waiting-", new byte[0], this.acl, CreateMode.EPHEMERAL_SEQUENTIAL));
            synchronized (this.waiting) {
                waiterPath = waiterPath.substring(this.root.length() + 1);
                this.waiting.put(waiterPath, waiter);
                log.trace("waiter created for path {}", waiterPath);
            }
        } catch (KeeperException | InterruptedException e) {
            throw new WaiterQueueException("failed registering waiter", e);
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
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void queueChanged(WatchedEvent event) {
        if(event.getType().equals(Watcher.Event.EventType.NodeChildrenChanged)) {
            this.registerQueueWatcher();

            log.trace("node children changed for {}", event.getPath());
            List<String> children;
            try {
                children = this.klient.operate(keeper -> keeper.getChildren(event.getPath(), false));
            } catch (KeeperException | InterruptedException e) {
                log.error("error retrieving waiter queue content, cannot operate, as skipping an event, queue may be blocked", e);
                return;
            }
            Collections.sort(children);

            synchronized (this.waiting) {
                String nextInLine = children.get(0);
                if(this.waiting.containsKey(nextInLine)) {
                    Waiter waiter = this.waiting.remove(nextInLine);
                    log.debug("executing waiter for {}", nextInLine);
                    this.service.execute(() -> this.execute(waiter, this.root + "/" + nextInLine));
                } else {
                    log.trace("no waiters registered here for {}", nextInLine);
                }
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
        } catch (KeeperException | InterruptedException e) {
            log.error("error while deleting waiter node, queue state may be inconsistent", e);
        }
    }

    interface Waiter {
        void nextInLine();
    }
}
