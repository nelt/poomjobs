package org.codingmatters.poomjobs.zookeeper.test.utils;

import org.codingmatters.poomjobs.zookeeper.test.utils.embedded.ZookeeperEmbeddedEnsemble;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nel on 25/08/15.
 */
public class ZookeeperEnsembleTestSupport extends ExternalResource {

    static private final Logger log = LoggerFactory.getLogger(ZookeeperEnsembleTestSupport.class);

    static public Builder ensemble() {
        return new Builder(3);
    }


    static public class Builder {
        private int serverCount;
        private Builder(int serverCount) {
            this.serverCount = serverCount;
        }

        public Builder withServerCount(int serverCount) {
            this.serverCount = serverCount;
            return this;
        }

        public ZookeeperEnsembleTestSupport build() {
            return new ZookeeperEnsembleTestSupport(this.serverCount);
        }
    }

    private final int serverCount;
    private ZookeeperEmbeddedEnsemble ensemble;


    public ZookeeperEnsembleTestSupport(int serverCount) {
        this.serverCount = serverCount;
    }

    @Override
    protected void before() throws Throwable {
        System.setProperty("zookeeper.jmx.log4j.disable", "true");

        this.ensemble = new ZookeeperEmbeddedEnsemble(this.serverCount);
        this.ensemble.startEnsemble();
        this.ensemble.waitEnsembleStartup();
    }

    @Override
    protected void after() {
        log.debug("tearing down ensemble...");
        try {
            log.debug("stopping ensemble...");
            this.ensemble.stopEnsemble();
            log.debug("ensemble stopped.");
        } catch(Exception e) {
            log.error("failed to stop ensemble", e);
        }

        try {
            log.debug("cleaning ensemble...");
            this.ensemble.cleanEnsemble();
            log.debug("ensemble cleaned.");
        } catch(Exception e) {
            log.error("failed to clean ensemble", e);
        }
        log.debug("ensemble torn down.");
    }

    public void startServer(int i) throws Exception {
        this.ensemble.startServer(i);
    }

    public String getClientUrl(int i) {
        return this.ensemble.getClientUrl(i);
    }

    public String getClientFullUrl() {
        String url = "";
        for(int i = 0 ; i < this.getServerCount() ; i++) {
            if(i > 0) {
                url += ",";
            }
            url += this.ensemble.getClientUrl(i);
        }
        return url;
    }

    public void stopServer(int i) throws Exception {
        this.ensemble.stopServer(i);
    }

    public String getServerSpec(int i) {
        return this.ensemble.getSpec(i);
    }

    public int getServerCount() {
        return serverCount;
    }

    public int getServerByConnectionUrl(String connectedUrl) throws Exception {
        return this.ensemble.getServerByConnectionUrl(connectedUrl);
    }
}
