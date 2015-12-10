package org.codingmatters.poomjobs.http;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;

/**
 * Created by nel on 03/11/15.
 */
public class TestUndertowServer extends ExternalResource {

    static private Logger log = LoggerFactory.getLogger(TestUndertowServer.class);

    private Undertow server;
    private int port;
    private HttpHandler handler;

    @Override
    protected void before() throws Throwable {
        log.debug("starting undertow test server...");

        ServerSocket sock = new ServerSocket(0);
        this.port = sock.getLocalPort();
        sock.close();

        this.server = Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(exchange -> handler.handleRequest(exchange))
                .build();
        this.server.start();

        log.debug("undertow test server started");
    }

    @Override
    protected void after() {
        log.debug("stopping undertow test server...");
        this.server.stop();
        log.debug("undertow test server stopped");
    }

    public void setHandler(HttpHandler handler) {
        this.handler = handler;
    }

    public String url(String path) {
        path = path.startsWith("/") ? path : "/" + path;
        return "http://localhost:" + this.port + path;
    }
}
