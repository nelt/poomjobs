package org.codingmatters.poomjobs.zookeeper.test.utils.embedded;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZKUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by nel on 25/08/15.
 */
public class ZookeeperEmbeddedServer {
    static private final Logger log = LoggerFactory.getLogger(ZookeeperEmbeddedServer.class);

    private final File dataDir;
    private final int zkPort;

    private final ServerConfig config;
    private EmbeddedZooKeeperServerMain main;
    private Thread mainThread;

    public ZookeeperEmbeddedServer() throws Exception {
        this.dataDir = ServerConfigBuilder.getFreeDataDirectory();
        this.zkPort = ServerConfigBuilder.freePort();

        this.config = new ServerConfigBuilder()
                .withProperty("tickTime", "2000")
                .withProperty("dataDir", this.dataDir.getAbsolutePath())
                .withProperty("clientPort", "" + this.zkPort)
                .standalone();
    }

    public String getUrl() {
        return "127.0.0.1:" + this.zkPort;
    }


    public void start() throws Exception {
        long start = System.currentTimeMillis();
        this.main = new EmbeddedZooKeeperServerMain();
        this.mainThread = new Thread(() -> {
            try {
                this.main.runFromConfig(this.config);
            } catch (IOException e) {
                log.error("error running embedded zookeeper main", e);
            }
        });
        this.mainThread.start();

        ZKUtils.waitServerStartup(this.getUrl());

        log.debug("embedded zookeeper server started in {} ms. [url={} ; data-dir={}]",
                System.currentTimeMillis() - start,
                this.getUrl(),
                this.dataDir.getAbsolutePath());
    }

    public void stop() {
        this.main.stop();
        try {
            this.mainThread.join();
            log.debug("embedded zookeeper server stopped [url={} ; data-dir={}]",
                    this.getUrl(),
                    this.dataDir.getAbsolutePath());
        } catch (InterruptedException e) {
            log.error("error while stopping embedded zookeeper server stopped [url={} ; data-dir={}]",
                    this.getUrl(),
                    this.dataDir.getAbsolutePath());
        }

    }

    public void cleanUp() {
        ServerConfigBuilder.delete(this.dataDir);
    }

    private class EmbeddedZooKeeperServerMain extends ZooKeeperServerMain {
        public void stop() {
            this.shutdown();
        }
    }
}
