package org.codingmatters.poomjobs.http;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.junit.rules.ExternalResource;

import java.net.ServerSocket;

/**
 * Created by nel on 03/11/15.
 */
public class TestUndertowServer extends ExternalResource {

    private Undertow server;
    private int port;
    private HttpHandler handler;

    @Override
    protected void before() throws Throwable {
        ServerSocket sock = new ServerSocket(0);
        this.port = sock.getLocalPort();
        sock.close();

        this.server = Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(exchange -> handler.handleRequest(exchange))
                .build();
        this.server.start();
    }

    @Override
    protected void after() {
        this.server.stop();
    }

    public void setHandler(HttpHandler handler) {
        this.handler = handler;
    }

    public String url() {
        return "http://localhost:" + this.port;
    }
}
