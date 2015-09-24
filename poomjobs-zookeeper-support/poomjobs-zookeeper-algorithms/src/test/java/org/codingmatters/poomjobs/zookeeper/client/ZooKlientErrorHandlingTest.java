package org.codingmatters.poomjobs.zookeeper.client;

import org.apache.zookeeper.KeeperException;
import org.codingmatters.poomjobs.test.utils.CloseableResources;
import org.codingmatters.poomjobs.test.utils.ValueChangeSemaphore;
import org.codingmatters.poomjobs.zookeeper.test.utils.ZookeeperEnsembleTestSupport;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.codingmatters.poomjobs.test.utils.Runner.Run.inThread;
import static org.codingmatters.poomjobs.zookeeper.client.ZooKlient.zoo;
import static org.codingmatters.poomjobs.zookeeper.test.utils.ZookeeperEnsembleTestSupport.ensemble;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 25/09/15.
 */
public class ZooKlientErrorHandlingTest {

    static private final Logger log = LoggerFactory.getLogger(ZooKlientErrorHandlingTest.class);

    @Rule
    public ZookeeperEnsembleTestSupport ensemble = ensemble().withServerCount(3).build();

    @Rule
    public CloseableResources resources = new CloseableResources();


    @Test
    public void testReconnection() throws Exception {
        ZooKlient klient = this.resources.add(zoo(ensemble.getClientUrl(1)).klient());
        klient.operate(keeper -> keeper.getChildren("/", false));

        ValueChangeSemaphore<Boolean> done = new ValueChangeSemaphore<>(false);

        ensemble.stopServer(1);
        inThread(() -> {
            klient.operate(keeper -> keeper.getChildren("/", false));
            done.set(true);
        });
        ensemble.startServer(1);

        assertThat(done.waitForValue(true, 3000L), is(true));
    }

    @Test
    public void testSessionExpired() throws Exception {
        ZooKlient klient = resources.add(ZooKlient.zoo(ensemble.getClientUrl(1)).klient());
        klient.operate(keeper -> keeper.getChildren("/", false));

        ensemble.stopServer(1);
        Thread.sleep(klient.getSessionTimeout() * 2);
        ensemble.startServer(1);

        try {
            klient.operate(keeper -> keeper.getChildren("/", false));
            Assert.fail("expected session to be expired");
        } catch (KeeperException.SessionExpiredException se) {}
    }

    @Test
    public void testSessionExpiredRecovery() throws Exception {
        ZooKlient klient = resources.add(ZooKlient.zoo(ensemble.getClientUrl(1)).withSessionExpiryRecovery(true).klient());
        klient.operate(keeper -> keeper.getChildren("/", false));

        ensemble.stopServer(1);
        Thread.sleep(klient.getSessionTimeout() * 2);
        ensemble.startServer(1);

        klient.operate(keeper -> keeper.getChildren("/", false));
    }


}
