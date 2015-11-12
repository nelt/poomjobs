package org.codingmatters.poomjobs.http.undertow;


import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.codingmatters.poomjobs.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by nel on 02/11/15.
 */
public class RestServiceHandler implements HttpHandler {

    static private final Logger log = LoggerFactory.getLogger(RestServiceHandler.class);

    private final RestService descriptor;

    public RestServiceHandler(RestService serviceDescriptor) {
        descriptor = serviceDescriptor;
    }

    static public HttpHandler from(RestService serviceDescriptor) {
        return new RestServiceHandler(serviceDescriptor);
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
        log.info("request path :  {}", exchange.getRequestPath());
        log.info("relative path : {}", exchange.getRelativePath());

        exchange.startBlocking();

        String path = exchange.getRelativePath();

        UndertowRestIO io = null;
        try {
            io = new UndertowRestIO(exchange);
        } catch (IOException e) {
            log.error("error creating RestIO from exchange", e);
            throw e;
        }

        if(! path.startsWith(this.descriptor.getRootPath())) {
            log.error("requested service does not exist : {}", exchange.getRequestPath());
            io.status(RestStatus.SERVICE_NOT_FOUND);
        } else {
            RestResourceInvocation resourceInvocation = this.descriptor.getMatchingResource(path.substring(this.descriptor.getRootPath().length()));
            try {
                RestResource.Method method = this.method(exchange);
                resourceInvocation.getResource()
                        .handler(method)
                        .handle(io.withPathParameters(resourceInvocation.getPathParameters()));
                log.info("handled rest request {} for {}", exchange.getRequestMethod(), exchange.getRequestPath());
            } catch (RestException e) {
                log.error("error handling rest request " + exchange.getRequestMethod() + " for " + exchange.getRequestPath(), e);
                io.status(e.getStatus()).content(e.getContent());
            }
        }
        io.send(exchange);
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
