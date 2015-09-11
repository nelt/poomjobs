package org.codingmatters.poomjobs.zookeeper.test.utils;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by nel on 11/09/15.
 */
public class ZooKlient implements AutoCloseable {
    static private Logger log = LoggerFactory.getLogger(ZooKlient.class);

    static public ZooKlientBuilder zoo(String connectString) {
        return new ZooKlientBuilder(connectString);
    }

    static public class ZooKlientBuilder {
        private final String connectString;
        private int sessionTimeout = 3000;
        private boolean canBeReadOnly = false;
        private Watcher watcher;

        private ZooKlientBuilder(String connectString) {
            this.connectString = connectString;
        }

        public ZooKlientBuilder withSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
            return this;
        }

        public ZooKlientBuilder withCanBeReadOnly(boolean canBeReadOnly) {
            this.canBeReadOnly = canBeReadOnly;
            return this;
        }

        public ZooKlientBuilder withWatcher(Watcher watcher) {
            this.watcher = watcher;
            return this;
        }

        public ZooKlient klient() throws Exception {
            return new ZooKlient(() -> new ZooKeeper(
                    this.connectString,
                    this.sessionTimeout,
                    null,
                    this.canBeReadOnly), this.watcher);
        }
    }

    private final ZooKeeperCreator keeperCreator;
    private final Watcher internalWatcher = this::process;

    private ZooKeeper keeper;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private ZooKlient(ZooKeeperCreator keeperCreator, Watcher watcher) throws Exception {
        this.keeperCreator = keeperCreator;
        this.keeper = this.keeperCreator.create();
        this.keeper.register(new WatcherChain().watches(this.internalWatcher, watcher));
    }

    public <T> T operate(SyncOperation<T> operation) throws KeeperException, InterruptedException {
        while(true) {
            try {
                return operation.operate(this.keeper);
            } catch (KeeperException e) {
                if(e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                    log.debug("recoverable keeper exception, will retry when connection recovered", e);
                    this.waitConnected();
                    log.debug("connection recovered");
                } else {
                    throw e;
                }
            }
        }
    }

    public ZooKlient waitConnected() throws InterruptedException {
        synchronized (this.connected) {
            if(! this.connected.get()) {
                this.connected.wait();
            }
        }
        return this;
    }

    private void process(WatchedEvent event) {
        switch (event.getState()) {
            case Disconnected:
                this.processDisconnected(event);
                break;
            case SyncConnected:
                this.processSyncConnected(event);
                break;
            case AuthFailed:
                break;
            case ConnectedReadOnly:
                break;
            case SaslAuthenticated:
                break;
            case Expired:
                break;
        }
    }

    private void processSyncConnected(WatchedEvent event) {
        log.debug("session {} connected", this.keeper.getSessionId());
        this.connected.set(true);
        synchronized (this.connected) {
            this.connected.notifyAll();
        }
    }

    private void processDisconnected(WatchedEvent event) {
        log.debug("session {} disconnected", this.keeper.getSessionId());
        this.connected.set(false);
    }

    @Override
    public void close() throws Exception {
        this.keeper.close();
    }

    private interface ZooKeeperCreator {
        ZooKeeper create() throws Exception;
    }

    public interface SyncOperation<T> {
        T operate(ZooKeeper keeper) throws KeeperException, InterruptedException;
    }
}
