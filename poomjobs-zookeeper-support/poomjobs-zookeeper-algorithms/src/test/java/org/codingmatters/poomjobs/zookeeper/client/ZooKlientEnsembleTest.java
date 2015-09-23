package org.codingmatters.poomjobs.zookeeper.client;

import org.codingmatters.poomjobs.test.utils.CloseableResources;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZookeeperEnsembleTestSupport;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.codingmatters.poomjobs.zookeeper.client.ZooKlient.zoo;
import static org.codingmatters.poomjobs.zookeeper.test.utils.ZookeeperEnsembleTestSupport.ensemble;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 22/09/15.
 */
public class ZooKlientEnsembleTest {
    static private final Logger log = LoggerFactory.getLogger(ZooKlientEnsembleTest.class);

    @Rule
    public ZookeeperEnsembleTestSupport ensemble = ensemble().withServerCount(3).build();

    @Rule
    public CloseableResources resources = new CloseableResources();


    @Test
    public void testConnectedUrl() throws Exception {
        ZooKlient klient = this.resources.add(zoo(this.ensemble.getClientFullUrl()).klient());
        // force connection
        klient.operate(keeper -> keeper.getChildren("/", false));

        String[] urls = this.ensemble.getClientFullUrl().split(",");

        assertThat(klient.getConnectedUrl(), startsWith("localhost:"));
        assertThat(klient.getConnectedUrl(), isOneOf(urls));
    }

    @Test
    public void testSeamlessReconnection() throws Exception {
        ZooKlient klient = this.resources.add(zoo(this.ensemble.getClientFullUrl()).klient());

        klient.operate(keeper -> keeper.getChildren("/", false));

        log.info("klient url : {}", klient.getConnectedUrl());
        int server = this.ensemble.getServerByConnectionUrl(klient.getConnectedUrl());
        log.info("connected to server {} with url {}", server, klient.getConnectedUrl());

        this.ensemble.stopServer(server);
        log.info("stopped server {}", server);

        klient.operate(keeper -> keeper.getChildren("/", false));

        server = this.ensemble.getServerByConnectionUrl(klient.getConnectedUrl());
        log.info("reconnected to server {} with url {}", server, klient.getConnectedUrl());
    }
}
