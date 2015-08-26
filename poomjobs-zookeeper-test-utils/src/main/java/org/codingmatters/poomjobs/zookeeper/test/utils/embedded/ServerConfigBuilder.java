package org.codingmatters.poomjobs.zookeeper.test.utils.embedded;

import com.sun.corba.se.spi.activation.Server;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by nel on 26/08/15.
 */
public class ServerConfigBuilder {

    static private final Logger log = LoggerFactory.getLogger(ServerConfigBuilder.class);

    private final Properties properties = new Properties();

    static public int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    static public int[] freePorts(int count) throws IOException {
        int [] result = new int[count];
        ArrayList<ServerSocket> sockets = new ArrayList<>(count);
        try {
            for (int i = 0; i < count; i++) {
                ServerSocket socket = new ServerSocket(0);
                sockets.add(socket);
                result[i] = socket.getLocalPort();
            }
            return result;
        } finally {
            for (ServerSocket socket : sockets) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static public File getFreeDataDirectory() {
        String dataDirectory = System.getProperty("java.io.tmpdir");
        File result;
        do {
            result = new File(dataDirectory, "test-zookeeper-" + UUID.randomUUID()).getAbsoluteFile();
        } while(result.exists());
        return result;
    }

    static public void delete(File file) {
        if(file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }

    public ServerConfigBuilder withProperty(String key, String value) {
        this.properties.setProperty(key, value);
        return this;
    }

    public QuorumPeerConfig ensemble() throws Exception {
        QuorumPeerConfig result = new QuorumPeerConfig();
        result.parseProperties(this.properties);
        return result;
    }

    public ServerConfig standalone() throws Exception {
        ServerConfig result = new ServerConfig();
        result.readFrom(ensemble());
        return result;
    }

    public String dump() {
        StringWriter writer = new StringWriter();
        this.properties.list(new PrintWriter(writer));
        writer.flush();
        return writer.toString();
    }
}
