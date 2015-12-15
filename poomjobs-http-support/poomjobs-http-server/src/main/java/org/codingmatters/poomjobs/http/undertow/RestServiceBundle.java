package org.codingmatters.poomjobs.http.undertow;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import org.codingmatters.poomjobs.http.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.codingmatters.poomjobs.http.undertow.RestServiceHandler.statusResponse;

/**
 * Created by nel on 13/11/15.
 */
public class RestServiceBundle implements HttpHandler {
    static private final Logger log = LoggerFactory.getLogger(RestServiceBundle.class);

    static public RestServiceBundle services() {
        return new RestServiceBundle();
    }

    private final PathHandler deleguate = Handlers.path(exchange -> statusResponse(RestStatus.SERVICE_NOT_FOUND, exchange));

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
