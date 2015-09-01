package org.codingmatters.poomjobs.zookeeper.test.utils.embedded;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZKUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 25/08/15.
 */
public class ZookeeperEmbeddedEnsembleTest {

    static private final Logger log = LoggerFactory.getLogger(ZookeeperEmbeddedEnsembleTest.class);
    private ZookeeperEmbeddedEnsemble ensemble;


    @Before
    public void setUp() throws Exception {
        this.ensemble = new ZookeeperEmbeddedEnsemble(3);
        this.ensemble.startEnsemble();
        this.ensemble.waitEnsembleStartup();

    }

    @After
    public void tearDown() throws Exception {
        this.ensemble.stopEnsemble();
        this.ensemble.cleanEnsemble();
    }

    @Test
    public void testStartStopEnsemble() throws Exception {
        ZooKeeper zookeeper = new ZooKeeper(ensemble.getUrl(0), 3000, event -> {});
        zookeeper.create("/" + UUID.randomUUID().toString(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        zookeeper.close();
    }

    @Ignore
    @Test
    public void testStartEnsembleStopOneServer() throws Exception {
        ZooKeeper zookeeper0 = ZKUtils.connectedClient(this.ensemble.getUrl(0));
        String path = "/" + UUID.randomUUID().toString();
        zookeeper0.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        log.info("created node");

        ensemble.stopServer(1);

        Stat stat = zookeeper0.exists(path, false);
        assertThat(stat, is(notNullValue()));
        log.info("stated from 0: {}", stat);

        zookeeper0.create(path + "/yop", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        log.info("created child");

        stat = zookeeper0.exists(path + "/yop", false);
        assertThat(stat, is(notNullValue()));
        log.info("stated from 0: {}", stat);

        log.info("connecting to server 2 : {}", this.ensemble.getUrl(2));
        ZooKeeper zookeeper2 = ZKUtils.connectedClient(this.ensemble.getUrl(2));
        stat = zookeeper2.exists(path, false);
        assertThat(stat, is(notNullValue()));
        log.info("stated from 2 : {}", stat);

        zookeeper0.close();
        zookeeper2.close();
    }
}