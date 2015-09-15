package org.codingmatters.poomjobs.zookeeper.test.utils.embedded;

import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerMain;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZKUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 01/09/15.
 */
public class ZookeeperEmbeddedPeerServer {

    static private final Logger log = LoggerFactory.getLogger(ZookeeperEmbeddedPeerServer.class);

    private final File dataDir;
    private final int clientPort;
    private final String[] serversSpec;
    private final int serverId;

    private Thread serverThread;
    private EmbeddedQuorumPeerMain peerMain;


    public ZookeeperEmbeddedPeerServer(File dataDir, int clientPort, int serverId, String[] serversSpec) {
        this.dataDir = dataDir;
        this.clientPort = clientPort;
        this.serversSpec = serversSpec;
        this.serverId = serverId;
    }

    public void start() throws Exception {
        assertThat("server " + this.serverId + " is stopped (main object)", this.peerMain, is(nullValue()));
        assertThat("server " + this.serverId + " is stopped (thread object)", this.serverThread, is(nullValue()));

        QuorumPeerConfig serverConfig = this.freshConfig();
        this.peerMain = new EmbeddedQuorumPeerMain();
        this.serverThread = new Thread(() -> {
            try {
                log.info("running peer main");
                this.peerMain.runFromConfig(serverConfig);
            } catch (IOException e) {
                log.error("error running embedded quorum peer", e);
            }
        });
        this.serverThread.setName("embedded-server-" + this.serverId + "-monitor");
        this.serverThread.start();

        log.debug("embedded zookeeper server launched [url={} ; data-dir={}]",
                this.getUrl(),
                serverConfig.getDataDir());
    }

    private QuorumPeerConfig freshConfig() throws Exception {
        this.dataDir.mkdirs();
        File myIdFile = new File(dataDir, "myid");
        myIdFile.createNewFile();
        try(Writer writer = new FileWriter(myIdFile)) {
            writer.write("" + this.serverId);
            writer.flush();
        }

        ServerConfigBuilder configBuilder = new ServerConfigBuilder()
                .withProperty("dataDir", this.dataDir.getAbsolutePath())
                .withProperty("clientPort", "" + this.clientPort)
                .withProperty("initLimit", "10")
                .withProperty("syncLimit", "5")
//                .withProperty("tickTime", "-1")
                ;
        for(int x = 0 ; x < this.serversSpec.length ; x++) {
            configBuilder.withProperty("server." + x, this.serversSpec[x]);
        }

        return configBuilder.ensemble();
    }

    public void waitStartup() throws Exception {
        log.debug("waiting server {} startup...", this.serverId);
        ZKUtils.waitServerStartup(this.getUrl());
        log.debug("server {} started.", this.serverId);
    }

    public void stop() throws Exception {
        if(this.peerMain == null && this.serverThread == null) {
            log.info("server {} already stopped", this.serverId);
            return;
        }

        log.debug("stopping server {}...", this.serverId);
        this.peerMain.stop();
        this.serverThread.join();
        log.debug("server {} stopped.", this.serverId);

        this.peerMain = null;
        this.serverThread = null;
    }

    public void clean() throws Exception {
        log.debug("cleaning server {} data dir {}", this.serverId , this.dataDir.getAbsolutePath());
        ServerConfigBuilder.delete(this.dataDir);
    }


    public String getUrl() {
        return "localhost:" + this.clientPort;
    }

    public String getSpec() {
        return this.serversSpec[this.serverId];
    }


    class EmbeddedQuorumPeerMain extends QuorumPeerMain {
        public void stop() throws InterruptedException {
            this.quorumPeer.shutdown();
            this.quorumPeer.join();
        }
    }
}
