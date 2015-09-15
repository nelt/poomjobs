package org.codingmatters.poomjobs.zookeeper.test.utils.explore;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZookeeperSingleServerTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 22/07/15.
 */
public class ExploreTest {

    public static final String PATH = "/tests";
    static private final Logger log = LoggerFactory.getLogger(ExploreTest.class);
    @Rule
    public ZookeeperSingleServerTestSupport zookeeperTestSupport = new ZookeeperSingleServerTestSupport();

    private ZooKeeper zookeeper;

    @Before
    public void setUp() throws Exception {
        this.zookeeper = zookeeperTestSupport.createClient(3000, watchedEvent -> {
            System.out.println(watchedEvent);
            switch (watchedEvent.getState()) {
                case Unknown:
                    break;
                case Disconnected:
                    break;
                case NoSyncConnected:
                    break;
                case SyncConnected:
                    break;
                case AuthFailed:
                    break;
                case ConnectedReadOnly:
                    break;
                case SaslAuthenticated:
                    break;
                case Expired:
                    break;
            }

            log.debug("seen : {}", watchedEvent);
        });

        zookeeperTestSupport.createPath(PATH, ZooDefs.Ids.OPEN_ACL_UNSAFE);
    }

    @After
    public void tearDown() throws Exception {
        this.zookeeper.close();
    }

    @Test
    public void testHello() throws Exception {
        int producerCount = 10;
        int nodesPerProducer = 10;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for(int i = 0 ; i < producerCount ; i++) {
            executor.execute(() -> {
                for (int j = 0; j < nodesPerProducer; j++) {
                    try {
                        String n = this.zookeeper.create(PATH + "/" + UUID.randomUUID().toString(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                        Thread.sleep(10);
                    } catch (KeeperException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        executor.shutdown();
        while (! executor.isTerminated()) {
            Stat stat = this.zookeeper.exists(PATH, false);
            log.debug("{} has {} children", PATH, stat.getNumChildren());
            Thread.sleep(1000);
        }

        Stat stat = this.zookeeper.exists(PATH, false);
        log.debug("{} has {} children", PATH, stat.getNumChildren());

        assertThat(stat.getNumChildren(), is(producerCount * nodesPerProducer));
    }

}
