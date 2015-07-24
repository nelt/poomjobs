package org.codingmatters.poomjobs.zookeeper.test.utils.explore;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZookeeperTestSupport;
import org.junit.*;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nel on 22/07/15.
 */
@Ignore
public class ExploreTest {

    public static final String PATH = "/tests";
    @ClassRule
    static public ZookeeperTestSupport zookeeperTestSupport = new ZookeeperTestSupport();
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

            System.out.println("seen : " + watchedEvent);
        });

        zookeeperTestSupport.createPath(PATH, ZooDefs.Ids.OPEN_ACL_UNSAFE);
    }

    @After
    public void tearDown() throws Exception {
        zookeeperTestSupport.recursiveDelete(PATH);
        this.zookeeper.close();
    }


    @Test
    public void testHello() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for(int i = 0 ; i < 10 ; i++) {
            executor.execute(() -> {
                for (int j = 0; j < 1000; j++) {
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
            System.out.println(PATH + " has " + stat.getNumChildren() + " children");
            Thread.sleep(1000);
        }

    }

}
