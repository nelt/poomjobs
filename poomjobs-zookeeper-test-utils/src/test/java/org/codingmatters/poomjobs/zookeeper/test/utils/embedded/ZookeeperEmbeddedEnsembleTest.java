package org.codingmatters.poomjobs.zookeeper.test.utils.embedded;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by nel on 25/08/15.
 */
public class ZookeeperEmbeddedEnsembleTest {



    @Test
    public void testStartStopEnsemble() throws Exception {
        ZookeeperEmbeddedEnsemble ensemble = new ZookeeperEmbeddedEnsemble(3);
        ensemble.startEnsemble();

        ensemble.waitEnsembleStartup();

        ZooKeeper zookeeper = new ZooKeeper(ensemble.getUrl(0), 3000, event -> {});
        zookeeper.create("/" + UUID.randomUUID().toString(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        ensemble.stopEnsemble();
        ensemble.cleanEnsemble();
    }

    @Ignore
    @Test
    public void testStartEnsembleStopOneServer() throws Exception {
        ZookeeperEmbeddedEnsemble ensemble = new ZookeeperEmbeddedEnsemble(3);
        ensemble.startEnsemble();

        ensemble.waitEnsembleStartup();

        ensemble.stopServer(1);

        Thread.sleep(3000L);
        ZooKeeper zookeeper = new ZooKeeper(ensemble.getUrl(0), 3000, event -> {});
        zookeeper.create("/" + UUID.randomUUID().toString(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        ensemble.startServer(1);


        ensemble.stopEnsemble();
        ensemble.cleanEnsemble();
    }
}