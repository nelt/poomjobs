package org.codingmatters.poomjobs.http.undertow;


import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.util.Headers;
import org.codingmatters.poomjobs.http.RestException;
import org.codingmatters.poomjobs.http.RestResource;
import org.codingmatters.poomjobs.http.RestService;
import org.codingmatters.poomjobs.http.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by nel on 02/11/15.
 */
public class RestServiceHandler implements HttpHandler {

    static private final Logger log = LoggerFactory.getLogger(RestServiceHandler.class);

    private final RestResource resource;

    public RestServiceHandler(RestResource resource) {
        this.resource = resource;
    }

    static public HttpHandler from(RestService serviceDescriptor) {
        PathTemplateHandler result = new PathTemplateHandler(RestServiceHandler::resourceNotFound);

        serviceDescriptor.forEachResource((path, resource) ->
                result.add(path, new RestServiceHandler(resource))
        );

        return result;
    }

    static private void resourceNotFound(HttpServerExchange exchange) {
        log.error("requested resource does not exist : {}", exchange.getRequestPath());
        exchange.setStatusCode(RestStatus.RESOURCE_NOT_FOUND.getHttpStatus());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(RestStatus.RESOURCE_NOT_FOUND.getMessage(), Charset.forName("UTF-8"));
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        this.handleRestRequest(exchange);
    }

    protected void handleRestRequest(HttpServerExchange exchange) throws IOException {
        exchange.startBlocking();

        UndertowRestIO io = buildRestIO(exchange);
        try {
            RestResource.Method method = this.method(exchange);
            this.resource
                    .handler(method)
                    .handle(io);
            log.info("handled rest request {} for {}", exchange.getRequestMethod(), exchange.getRequestPath());
        } catch (RestException e) {
            log.error("error handling rest request " + exchange.getRequestMethod() + " for " + exchange.getRequestPath(), e);
            io.status(e.getStatus()).content(e.getContent());
        }
        io.send(exchange);
    }

    static private UndertowRestIO buildRestIO(HttpServerExchange exchange) throws IOException {
        UndertowRestIO io = null;
        try {
            io = new UndertowRestIO(exchange);
        } catch (IOException e) {
            log.error("error creating RestIO from exchange", e);
            throw e;
        }
        return io;
    }

    protected RestResource.Method method(HttpServerExchange exchange) throws RestException {
        String asString = exchange.getRequestMethod().toString().toUpperCase();
        try {
            return RestResource.Method.valueOf(asString);
        } catch(IllegalArgumentException e) {
            throw new RestException(RestStatus.METHOD_NOT_ALLOWED, e);
        }
    }

}
