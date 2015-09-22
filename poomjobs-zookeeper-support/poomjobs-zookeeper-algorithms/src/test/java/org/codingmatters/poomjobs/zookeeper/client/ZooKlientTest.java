package org.codingmatters.poomjobs.zookeeper.client;

import org.codingmatters.poomjobs.test.utils.CloseableResources;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZookeeperEnsembleTestSupport;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.codingmatters.poomjobs.zookeeper.test.utils.ZookeeperEnsembleTestSupport.ensemble;

/**
 * Created by nel on 22/09/15.
 */
public class ZooKlientTest {
    static private final Logger log = LoggerFactory.getLogger(ZooKlientTest.class);

    @Rule
    public ZookeeperEnsembleTestSupport ensemble = ensemble().withServerCount(3).build();

    @Rule
    public CloseableResources resources = new CloseableResources();


    @Test
    public void testConnectedUrl() throws Exception {
        ZooKlient klient = this.resources.add(ZooKlient.zoo(this.ensemble.getClientFullUrl()).klient());
        // force connection
        klient.operate(keeper -> keeper.getChildren("/", false));

        Assert.assertThat(klient.getConnectedUrl(), Matchers.startsWith("127.0.0.1:"));
    }

    @Test
    public void testSeamlessReconnection() throws Exception {
        ZooKlient klient = this.resources.add(ZooKlient.zoo(this.ensemble.getClientFullUrl()).klient());

        klient.operate(keeper -> keeper.getChildren("/", false));

        int server = this.ensemble.getServerByConnectionUrl(klient.getConnectedUrl());
        log.info("connected to server {} with url {}", server, klient.getConnectedUrl());

        this.ensemble.stopServer(server);
        log.info("stopped server {}", server);

        klient.operate(keeper -> keeper.getChildren("/", false));

        server = this.ensemble.getServerByConnectionUrl(klient.getConnectedUrl());
        log.info("reconnected to server {} with url {}", server, klient.getConnectedUrl());
    }
}
