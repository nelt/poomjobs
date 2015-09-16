package org.codingmatters.poomjobs.zookeeper.algo;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.codingmatters.poomjobs.test.utils.ValueChangeSemaphore;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZooKlient;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZookeeperSingleServerTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 15/09/15.
 */
public class WaiterQueueTest {

    static private final Logger log = LoggerFactory.getLogger(WaiterQueueTest.class);

    @Rule
    public ZookeeperSingleServerTestSupport zooSupport = new ZookeeperSingleServerTestSupport();

    private String queuePath;
    private ZooKlient zooKlient;
    private WaiterQueue waiterQueue;

    @Before
    public void setUp() throws Exception {
        String root = "/" + UUID.randomUUID().toString();
        this.queuePath = root + "/waiterQueue";
        this.zooSupport.createPath(root).createPath(this.queuePath);

        this.zooKlient = ZooKlient.zoo(this.zooSupport.getUrl()).klient();
        this.waiterQueue = new WaiterQueue(this.zooKlient, this.queuePath, ZooDefs.Ids.OPEN_ACL_UNSAFE, Executors.newCachedThreadPool());
    }

    @Test
    public void testWaitingEmptyQueueIsImmediate() throws Exception {
        ValueChangeSemaphore<Boolean> fired = new ValueChangeSemaphore<>(false);

        this.waiterQueue.waitMyTurn(() -> fired.set(true));
        assertThat(fired.waitForValue(true, 1000L), is(true));
    }

    @Test
    public void testTwoWaiters() throws Exception {
        ValueChangeSemaphore<Boolean> fired1 = new ValueChangeSemaphore<>(false);
        ValueChangeSemaphore<Boolean> fired2 = new ValueChangeSemaphore<>(false);

        String firstInLine = this.zooKlient.operate(keeper -> keeper.create(this.queuePath + "/waiting-", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));

        this.waiterQueue.waitMyTurn(() -> fired1.set(true));
        this.waiterQueue.waitMyTurn(() -> fired2.set(true));

        this.zooKlient.operate(keeper -> {
            keeper.delete(firstInLine, -1);
            return null;
        });

        assertThat(fired1.waitForValue(true, 1000L), is(true));
        assertThat(fired2.waitForValue(true, 1000L), is(true));
    }

    @Test
    public void testOneWaiterBefore() throws Exception {
        ValueChangeSemaphore<Boolean> fired = new ValueChangeSemaphore<>(false);

        String firstInLine = this.zooKlient.operate(keeper -> keeper.create(this.queuePath + "/waiting-", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));

        this.waiterQueue.waitMyTurn(() -> fired.set(true));
        assertThat(fired.get(), is(false));

        this.zooKlient.operate(keeper -> {
            keeper.delete(firstInLine, -1);
            return null;
        });

        assertThat(fired.waitForValue(true, 1000L), is(true));
    }

    @Test
    public void testManyWaitersBefore() throws Exception {
        ValueChangeSemaphore<Boolean> fired = new ValueChangeSemaphore<>(false);

        LinkedList<String> beforeInLine = new LinkedList<>();
        for(int i = 0 ; i < 30 ; i++) {
            beforeInLine.add(this.zooKlient.operate(keeper -> keeper.create(this.queuePath + "/waiting-", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL)));
        }

        this.waiterQueue.waitMyTurn(() -> fired.set(true));

        while(! beforeInLine.isEmpty()) {
            assertThat(fired.get(), is(false));
            this.zooKlient.operate(keeper -> {
                keeper.delete(beforeInLine.removeFirst(), -1);
                return null;
            });
        }

        assertThat(fired.waitForValue(true, 1000L), is(true));
    }
}