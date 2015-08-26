package org.codingmatters.poomjobs.zookeeper.test.utils.embedded;

import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerMain;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZKUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static org.codingmatters.poomjobs.zookeeper.test.utils.embedded.ServerConfigBuilder.freePorts;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 25/08/15.
 */
public class ZookeeperEmbeddedEnsemble {

    static private final Logger log = LoggerFactory.getLogger(ZookeeperEmbeddedEnsemble.class);

    private final QuorumPeerConfig [] serverConfigs;
    private final Thread [] serverThreads;
    private final EmbeddedQuorumPeerMain [] peerMains;

    public ZookeeperEmbeddedEnsemble(int serverCount) throws Exception {
        this.serverConfigs = new QuorumPeerConfig[serverCount];
        this.serverThreads = new Thread[serverCount];
        this.peerMains = new EmbeddedQuorumPeerMain[serverCount];

        String[] serversSpec = new String[serverCount];
        int [] ports = freePorts(serverCount * 3);

        for(int i = 0 ; i < serverCount ; i++) {
            serversSpec[i] = "localhost:" + ports[i * 3] + ":" + ports[i * 3 + 1];
        }

        for(int i = 0 ; i < serverCount ; i++) {
            File dataDir = ServerConfigBuilder.getFreeDataDirectory();
            dataDir.mkdirs();
            File myIdFile = new File(dataDir, "myid");
            myIdFile.createNewFile();
            try(Writer writer = new FileWriter(myIdFile)) {
                writer.write("" + (i));
                writer.flush();
            }

            ServerConfigBuilder configBuilder = new ServerConfigBuilder()
                    .withProperty("tickTime", "2000")
                    .withProperty("dataDir", dataDir.getAbsolutePath())
                    .withProperty("clientPort", "" + ports[i * 3 + 2])
                    .withProperty("initLimit", "10")
                    .withProperty("syncLimit", "5")
                    ;
            for(int x = 0 ; x < serverCount ; x++) {
                configBuilder.withProperty("server." + x, serversSpec[x]);
            }
            log.info("server {} config : {}", i, configBuilder.dump());
            this.serverConfigs[i] = configBuilder.ensemble();
        }
    }

    public void startEnsemble() throws Exception {
        for(int i = 0 ; i < this.serverConfigs.length ; i++) {
            this.startServer(i);    
        }
    }

    public void waitEnsembleStartup() throws IOException, InterruptedException {
        for(int i = 0 ; i < this.serverConfigs.length ; i++) {
            this.waitServerStartup(i);
        }
    }

    public void startServer(int i) throws Exception {
        assertThat("server " + i + " is stopped (main object)", this.peerMains[i], is(nullValue()));
        assertThat("server " + i + " is stopped (thread object)", this.serverThreads[i], is(nullValue()));

        long start = System.currentTimeMillis();

        this.peerMains[i] = new EmbeddedQuorumPeerMain();
        this.serverThreads[i] = new Thread(() -> {
            try {
                log.info("running peer main");
                this.peerMains[i].runFromConfig(this.serverConfigs[i]);
            } catch (IOException e) {
                log.error("error running embedded quorum peer", e);
            }
        });
        this.serverThreads[i].start();

        log.debug("embedded zookeeper server launched [url={} ; data-dir={}]",
                this.getUrl(i),
                this.serverConfigs[i].getDataDir());
    }

    protected void waitServerStartup(int i) throws IOException, InterruptedException {
        log.debug("waiting server {} startup...", i);
        ZKUtils.waitServerStartup(getUrl(i));
        log.debug("server {} started.", i);
    }

    public String getUrl(int i) {
        return "localhost:" + this.serverConfigs[i].getClientPortAddress().getPort();
    }

    public void stopEnsemble() throws Exception {
        for(int i = 0 ; i < this.serverConfigs.length ; i++) {
            this.stopServer(i);
        }
    }

    public void stopServer(int i) throws Exception {
        if(this.peerMains[i] == null && this.serverThreads[i] == null) {
            log.info("server {} already stopped");
            return;
        }

        log.debug("stopping server {}...", i);
        this.peerMains[i].stop();
        this.serverThreads[i].join();
        log.debug("server {} stopped.", i);

        this.peerMains[i] = null;
        this.serverThreads[i] = null;
    }

    public void cleanEnsemble() {
        for (int i = 0; i < this.serverConfigs.length; i++) {
            this.cleanServer(i);
        }
    }

    public void cleanServer(int i) {
        log.debug("cleaning server {} data dir {}", i , this.serverConfigs[i].getDataDir());
        ServerConfigBuilder.delete(new File(this.serverConfigs[i].getDataDir()));
    }

    class EmbeddedQuorumPeerMain extends QuorumPeerMain {
        public void stop() throws InterruptedException {
            this.quorumPeer.shutdown();
            this.quorumPeer.join();
        }
    }
}
