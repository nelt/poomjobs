package org.codingmatters.poomjobs.http;


import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

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

    protected void handleRestRequest(HttpServerExchange exchange) {
        log.info("request path :  {}", exchange.getRequestPath());
        log.info("relative path : {}", exchange.getRelativePath());

        String path = exchange.getRelativePath();
        UndertowRestIO io = new UndertowRestIO();
        if(! path.startsWith(this.descriptor.getRootPath())) {
            log.error("requested service does not exist : {}", exchange.getRequestPath());
            io.status(RestStatus.SERVICE_NOT_FOUND);
        } else {
            RestResource resource = this.descriptor.getMatchingResource(path.substring(this.descriptor.getRootPath().length()));
            try {
                RestResource.Method method = this.method(exchange);
                resource
                        .handler(method)
                        .handle(io);
                log.info("successfully handled rest request {} for {}", exchange.getRequestMethod(), exchange.getRequestPath());
            } catch (RestException e) {
                log.error("error handling rest request " + exchange.getRequestMethod() + " for " + exchange.getRequestPath(), e);
                io.status(e.getStatus());
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

    private class UndertowRestIO implements RestIO {
        private RestStatus status = RestStatus.OK;
        private String contentType = "text/plain";
        private String encoding = "UTF-8";
        private String content;

        public UndertowRestIO() {
        }

        @Override
        public RestIO status(RestStatus status) {
            this.status = status;
            return this;
        }

        @Override
        public RestIO contentType(String type) {
            this.contentType = type;
            return this;
        }

        @Override
        public RestIO encoding(String enc) {
            this.encoding = enc;
            return this;
        }

        @Override
        public RestIO content(String content) {
            this.content = content;
            return this;
        }

        public void send(HttpServerExchange exchange) {
            exchange.setStatusCode(this.status.getHttpStatus());
            if(this.status.getMessage() != null) {
                this.contentType("text/plain").content(this.status.getMessage());
            }

            String contentTypeLine = String.format("%s; %s", this.contentType, this.encoding);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentTypeLine);
            if(content != null) {
                exchange.getResponseSender().send(content, Charset.forName(this.encoding));
            }
        }
    }
}
