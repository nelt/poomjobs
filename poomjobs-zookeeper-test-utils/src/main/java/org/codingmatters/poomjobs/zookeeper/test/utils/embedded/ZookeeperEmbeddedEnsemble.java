package org.codingmatters.poomjobs.zookeeper.test.utils.embedded;

import org.codingmatters.poomjobs.zookeeper.test.utils.JMXBug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.codingmatters.poomjobs.zookeeper.test.utils.embedded.ServerConfigBuilder.freePorts;

/**
 * Created by nel on 25/08/15.
 */
public class ZookeeperEmbeddedEnsemble {

    static {
        JMXBug.fix();
    }

    static private final Logger log = LoggerFactory.getLogger(ZookeeperEmbeddedEnsemble.class);

    private final ZookeeperEmbeddedPeerServer [] servers;

    public ZookeeperEmbeddedEnsemble(int serverCount) throws Exception {
        this.servers = new ZookeeperEmbeddedPeerServer[serverCount];

        String[] serversSpec = new String[serverCount];
        int [] ports = freePorts(serverCount * 3);

        for(int i = 0 ; i < serverCount ; i++) {
            serversSpec[i] = "localhost:" + ports[i * 3] + ":" + ports[i * 3 + 1];
        }

        for(int i = 0 ; i < serverCount ; i++) {
            File dataDir = ServerConfigBuilder.getFreeDataDirectory();
            int clientPort = ports[i * 3 + 2];

            this.servers[i] = new ZookeeperEmbeddedPeerServer(dataDir, clientPort, i, serversSpec);
        }
    }

    public void startEnsemble() throws Exception {
        for(int i = 0 ; i < this.servers.length ; i++) {
            this.startServer(i);    
        }
    }

    public void waitEnsembleStartup() throws Exception {
        for(int i = 0 ; i < this.servers.length ; i++) {
            this.waitServerStartup(i);
        }
    }

    public void startServer(int i) throws Exception {
        this.servers[i].start();
    }

    protected void waitServerStartup(int i) throws Exception {
        this.servers[i].waitStartup();
    }

    public String getClientUrl(int i) {
        return servers[i].getUrl();
    }

    public void stopEnsemble() throws Exception {
        for(int i = 0 ; i < this.servers.length ; i++) {
            this.stopServer(i);
        }
    }

    public void stopServer(int i) throws Exception {
        this.servers[i].stop();
    }

    public void cleanEnsemble() throws Exception {
        for (int i = 0; i < this.servers.length; i++) {
            this.cleanServer(i);
        }
    }

    public void cleanServer(int i) throws Exception {
        this.servers[i].clean();
    }

    public String getSpec(int i) {
        return this.servers[i].getSpec();
    }
}
