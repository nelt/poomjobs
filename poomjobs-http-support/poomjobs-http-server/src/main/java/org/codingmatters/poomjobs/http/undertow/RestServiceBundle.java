package org.codingmatters.poomjobs.http.undertow;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Headers;
import org.codingmatters.poomjobs.http.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * Created by nel on 13/11/15.
 */
public class RestServiceBundle implements HttpHandler {
    static private final Logger log = LoggerFactory.getLogger(RestServiceBundle.class);

    static public RestServiceBundle services() {
        return new RestServiceBundle();
    }

    private final PathHandler deleguate = Handlers.path(Handlers.path(exchange -> {
        log.error("requested service does not exist : {}", exchange.getRequestPath());
        exchange.setStatusCode(RestStatus.SERVICE_NOT_FOUND.getHttpStatus());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(RestStatus.SERVICE_NOT_FOUND.getMessage(), Charset.forName("UTF-8"));
    }));

    private RestServiceBundle() {}

    public RestServiceBundle service(String path, HttpHandler serviceHandler) {
        this.deleguate.addPrefixPath(path, serviceHandler);
        return this;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.deleguate.handleRequest(exchange);
    }
}
