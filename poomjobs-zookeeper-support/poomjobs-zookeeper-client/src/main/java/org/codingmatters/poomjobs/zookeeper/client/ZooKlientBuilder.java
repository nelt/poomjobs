package org.codingmatters.poomjobs.zookeeper.client;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by nel on 25/09/15.
 */
public class ZooKlientBuilder {
    private final String connectString;
    private int sessionTimeout = 3000;
    private boolean canBeReadOnly = false;
    private Watcher watcher;

    private Long sessionId = null;
    private byte[] sessionPasswd = null;

    private boolean recoverOnSessionExpiry = false;

    ZooKlientBuilder(String connectString) {
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
        if (this.sessionId == null) {
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
