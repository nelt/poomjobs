package org.codingmatters.poomjobs.zookeeper.test.utils;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.codingmatters.poomjobs.zookeeper.test.utils.embedded.ZookeeperEmbeddedServer;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by nel on 22/07/15.
 */
public class ZookeeperTestSupport extends ExternalResource {

    private File serverDir;
    private ZooKeeperServer zookeeperServer;
    private int zkPort;

//    private ZookeeperEmbeddedServer embeddedServer;

    private ZooKeeper client;


    @Override
    protected void before() throws Throwable {
        int tickTime = 2000;
        int numConnections = 5000;
        String dataDirectory = System.getProperty("java.io.tmpdir");

        do {
            this.serverDir = this.getRandomTestDir(dataDirectory);
        } while(this.serverDir.exists());

        this.zookeeperServer = new ZooKeeperServer(serverDir, serverDir, tickTime);
        ServerCnxnFactory standaloneServerFactory = ServerCnxnFactory.createFactory(0, numConnections);
        this.zkPort = standaloneServerFactory.getLocalPort();

        standaloneServerFactory.startup(zookeeperServer);
        System.out.printf("zookeeperServer started");

//        this.embeddedServer
        this.client = this.createClient(3000, event -> {});
    }

    private File getRandomTestDir(String dataDirectory) {
        return new File(dataDirectory, "test-zookeeper-" + UUID.randomUUID()).getAbsoluteFile();
    }

    @Override
    protected void after() {
        this.zookeeperServer.shutdown();
        while(this.zookeeperServer.isRunning()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("zookeeper server stopped");

        this.delete(this.serverDir);
    }

    public String getUrl() {
        return "127.0.0.1:" + this.zkPort;
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


    private void delete(File file) {
        if(file.isDirectory()) {
            for (File child : file.listFiles()) {
                this.delete(child);
            }

        }
        file.delete();
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
