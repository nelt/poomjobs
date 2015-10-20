package org.codingmatters.poomjobs.zookeeper.client.internal;

/**
 * Created by nel on 20/10/15.
 */
public class ConnectionStateMonitor {

    private boolean connected = false;

    public synchronized boolean isConnected() {
        return this.connected;
    }

    public synchronized void waitConnected() throws InterruptedException {
        if(this.connected) return;
        this.wait();
    }

    public synchronized void disconnect() {
        this.connected = false;
        this.notifyAll();
    }

    public synchronized void connect() {
        this.connected = true;
        this.notifyAll();
    }
}
