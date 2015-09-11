package org.codingmatters.poomjobs.zookeeper.test.utils;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.codingmatters.poomjobs.zookeeper.test.utils.embedded.ZookeeperEmbeddedServer;
import org.junit.rules.ExternalResource;

import java.util.ArrayList;

/**
 * Created by nel on 22/07/15.
 */
public class ZookeeperTestSupport extends ExternalResource {


    private ZookeeperEmbeddedServer embeddedServer;
    private ZooKeeper client;

    @Override
    protected void before() throws Throwable {
        System.setProperty("zookeeper.jmx.log4j.disable", "true");

        this.embeddedServer = new ZookeeperEmbeddedServer();
        this.embeddedServer.start();

        this.client = this.createClient(3000, event -> {});
    }

    @Override
    protected void after() {
        this.embeddedServer.stop();
        this.embeddedServer.cleanUp();
    }

    public String getUrl() {
        return this.embeddedServer.getUrl();
    }


    public void recursiveDelete(String path) throws Exception {
        Stat stat = this.client.exists(path, false);
        if(stat != null) {
            System.out.println("removing children");
            if(stat.getNumChildren() != 0) {
                for (String child : this.client.getChildren(path, false)) {
                    this.client.delete(path + "/" + child, -1);
                }

            }
            System.out.println("removing test root");
            this.client.delete(path, -1);
        }
    }


    public ZooKeeper createClient(int sessionTimeout, Watcher watcher) throws Exception {
        return new ZooKeeper(this.getUrl(), sessionTimeout, watcher);
    }

    public void createPath(String path, ArrayList<ACL> openAclUnsafe) throws Exception {
        if(this.client.exists(path, false) == null) {
            this.client.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }
}
