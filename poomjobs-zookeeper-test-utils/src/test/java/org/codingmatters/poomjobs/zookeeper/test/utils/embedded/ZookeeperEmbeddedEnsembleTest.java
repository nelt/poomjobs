package org.codingmatters.poomjobs.zookeeper.test.utils.embedded;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.codingmatters.poomjobs.test.utils.CloseableResources;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZooKlient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.codingmatters.poomjobs.zookeeper.test.utils.ZooKlient.zoo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 25/08/15.
 */
public class ZookeeperEmbeddedEnsembleTest {

    static private final Logger log = LoggerFactory.getLogger(ZookeeperEmbeddedEnsembleTest.class);

    @Rule
    public CloseableResources resources = new CloseableResources();

    private ZookeeperEmbeddedEnsemble ensemble;


    @Before
    public void setUp() throws Exception {
        System.setProperty("zookeeper.jmx.log4j.disable", "true");

        this.ensemble = new ZookeeperEmbeddedEnsemble(3);
        for(int i = 0 ; i < 3 ; i++) {
            log.debug("server {} : {} - {}", i, this.ensemble.getSpec(i), this.ensemble.getUrl(i));
        }
        this.ensemble.startEnsemble();
        this.ensemble.waitEnsembleStartup();

    }

    @After
    public void tearDown() throws Exception {
        log.debug("tearing down ensemble...");
        this.ensemble.stopEnsemble();
        this.ensemble.cleanEnsemble();
    }

    @Test
    public void testStartStopEnsemble() throws Exception {
        ZooKlient klient = this.resources.add(zoo(ensemble.getUrl(0)).klient()).waitConnected();
        klient.operate(keeper -> keeper.create("/" + UUID.randomUUID().toString(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT));
    }

    @Test
    public void testStartEnsembleStopOneServer() throws Exception {
        Stat stat = null;

        ZooKlient klient0 = this.resources.add(zoo(this.ensemble.getUrl(0)).klient()).waitConnected();

        String path = "/" + UUID.randomUUID().toString();
        klient0.operate(keeper -> keeper.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT));
        log.info("created node");

        ensemble.stopServer(2);

        stat = klient0.operate(keeper -> keeper.exists(path, false));
        assertThat(stat, is(notNullValue()));
        log.info("stated from 0: {}", stat);

        klient0.operate(keeper -> keeper.create(path + "/yop", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT));
        log.info("created child");

        stat = klient0.operate(keeper -> keeper.exists(path + "/yop", false));
        assertThat(stat, is(notNullValue()));
        log.info("stated from 0: {}", stat);


        log.info("connecting to server 1 : {}", this.ensemble.getUrl(1));
        ZooKlient klient2 = this.resources.add(zoo(this.ensemble.getUrl(1)).klient()).waitConnected();

        stat = klient2.operate(keeper -> keeper.exists(path, false));
        assertThat(stat, is(notNullValue()));
        log.info("stated from 1 : {}", stat);
    }
}