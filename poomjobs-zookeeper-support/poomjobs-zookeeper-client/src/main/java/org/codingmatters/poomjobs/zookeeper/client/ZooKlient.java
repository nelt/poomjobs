package org.codingmatters.poomjobs.zookeeper.client;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.zookeeper.ZooKeeper.States.CONNECTED;


/**
 * ZooClient is tested in poomjobs-zookeeper-algorithms as it needs poomjobs-zookeeper-test-utils
 * Created by nel on 11/09/15.
 */
public class ZooKlient implements AutoCloseable {
    static private Logger log = LoggerFactory.getLogger(ZooKlient.class);

    /*
    remoteserver:localhost/127.0.0.1:47912
    remoteserver:localhost/0:0:0:0:0:0:0:1:34620

    do not match : remoteserver:localhost/127.0.0.1:36548 lastZxid:4294967303
     */
    public static final Pattern CONNECTED_URL_PATTERN = Pattern.compile("remoteserver:(\\w+)/[^\\s]+:(\\d+)(\\s+|$)");
    private final boolean recoverOnSessionExpired;

    static public ZooKlientBuilder zoo(String connectString) {
        return new ZooKlientBuilder(connectString);
    }


    static public class ZooKlientBuilder {
        private final String connectString;
        private int sessionTimeout = 3000;
        private boolean canBeReadOnly = false;
        private Watcher watcher;

        private Long sessionId = null;
        private byte[] sessionPasswd = null;

        private boolean recoverOnSessionExpiry = false;

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

        public ZooKlientBuilder withSessionExpiryRecovery(boolean recover) {
            this.recoverOnSessionExpiry = recover;
            return this;
        }

        public ZooKlientBuilder reconnects(long sessionId, byte[] sessionPassword) {
            this.sessionId = sessionId;
            this.sessionPasswd = sessionPassword;
            return this;
        }

        public ZooKlient klient() throws Exception {
            if(this.sessionId == null) {
                return new ZooKlient(() -> new ZooKeeper(
                        this.connectString,
                        this.sessionTimeout,
                        null,
                        this.canBeReadOnly), this.watcher, this.recoverOnSessionExpiry);
            } else {
                return new ZooKlient(() -> new ZooKeeper(
                        this.connectString,
                        this.sessionTimeout,
                        null,
                        this.sessionId,
                        this.sessionPasswd,
                        this.canBeReadOnly), this.watcher, this.recoverOnSessionExpiry);
            }
        }
    }

    private final ZooKeeperCreator keeperCreator;
    private final Watcher internalWatcher = this::process;
    private final Watcher registeredWatcher;


    private ZooKeeper keeper;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private ZooKlient(ZooKeeperCreator keeperCreator, Watcher watcher, boolean recoverOnSessionExpired) throws Exception {
        this.keeperCreator = keeperCreator;
        this.registeredWatcher = watcher;
        this.createKeeper();
        this.recoverOnSessionExpired = recoverOnSessionExpired;
    }

    private void createKeeper() throws Exception {
        this.keeper = this.keeperCreator.create();
        this.keeper.register(new WatcherChain().watches(this.internalWatcher, this.registeredWatcher));
    }

    public <T> T operate(SyncOperation<T> operation) throws KeeperException, InterruptedException {
        while(true) {
            try {
                return operation.operate(this.keeper);
            } catch (KeeperException.ConnectionLossException e) {
                log.debug("recoverable keeper exception, will retry when connection recovered", e);
                this.waitConnected();
                log.debug("connection recovered");
            } catch (KeeperException.SessionExpiredException e) {
                if(this.recoverOnSessionExpired) {
                    log.debug("session expired, trying recovering", e);
                } else {
                    throw e;
                }
            }
        }
    }

    public ZooKlient waitConnected() throws InterruptedException {
        log.debug("waiting connection {}", this.connected.get());
        synchronized (this.connected) {
            if(! this.connected.get()) {
                this.connected.wait();
            }
        }
        log.debug("connected");
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
                this.processSessionExpired();
                break;
        }
    }

    private void processSessionExpired() {
        log.debug("session expired");
        this.connected.set(false);
        if(this.recoverOnSessionExpired) {
            log.debug("policy is to recover on expired session...");
            try {
                this.createKeeper();
                log.debug("recovered from session expiry");
            } catch (Exception e) {
                log.error("error recovering from session expiry", e);
            }
        }
    }

    private void processSyncConnected(WatchedEvent event) {
        log.debug("session {} connected", this.keeper.getSessionId());
        synchronized (this.connected) {
            this.connected.set(true);
            this.connected.notifyAll();
        }
    }

    private void processDisconnected(WatchedEvent event) {
        log.debug("session {} disconnected", this.keeper.getSessionId());
        synchronized (this.connected) {
            this.connected.set(false);
        }
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

    public String getConnectedUrl() {
        if(!CONNECTED.equals(this.keeper.getState())) {
            return null;
        }
        log.debug("keeper string : {}", this.keeper.toString());
        try {
            Matcher matcher = CONNECTED_URL_PATTERN
                    .matcher(this.keeper.toString());
            if(matcher.find()) {
                log.debug("group count: {}, matching: {}, group 1: {}, group 2: {}",
                        matcher.groupCount(),
                        matcher.group(),
                        matcher.group(1),
                        matcher.group(2)
                );
                return matcher.group(1) + ":" + matcher.group(2);
            }
        } catch(IllegalStateException e) {
        }
        log.error("cannot find connected url, ZooKeeper.toString() doesn't correspond to what's awaited : {}", this.keeper.toString());
        return null;
    }

    public int getSessionTimeout() {
        return this.keeper.getSessionTimeout();
    }

    public long getSessionId() {
        return keeper.getSessionId();
    }

    public byte[] getSessionPasswd() {
        return keeper.getSessionPasswd();
    }

    @Override
    public String toString() {
        return "ZooKlient{" +
                "keeper=" + keeper +
                '}';
    }
}
