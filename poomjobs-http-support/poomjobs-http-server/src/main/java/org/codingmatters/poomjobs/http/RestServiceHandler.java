package org.codingmatters.poomjobs.http;


import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        private final HashMap<String, String> headers = new HashMap<>();


        private Map<String, List<String>> parameters;
        private Map<String, List<String>> pathParameters = new HashMap<>();
        private byte[] requestBytes;

        public UndertowRestIO(HttpServerExchange exchange) throws IOException {
            this.parameters = new HashMap<>();
            exchange.getQueryParameters().forEach((key, values) -> {
                this.parameters.put(key, new ArrayList<>(values));
            });
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try(InputStream in = exchange.getInputStream()) {
                byte[] buffer = new byte[1024];
                for (int read = in.read(buffer); read != -1; read = in.read(buffer)) {
                    out.write(buffer, 0, read);
                }
            }
            this.requestBytes = out.toByteArray();
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

        public UndertowRestIO withPathParameters(Map<String, List<String>> pathParameters) {
            this.pathParameters.putAll(pathParameters);
            return this;
        }

        @Override
        public Map<String, List<String>> parameters() {
            return this.parameters;
        }

        @Override
        public Map<String, List<String>> pathParameters() {
            return this.pathParameters;
        }

        @Override
        public byte[] requestContent() {
            return this.requestBytes;
        }

        @Override
        public void header(String name, String value) {
            this.headers.put(name, value);
        }

        public void send(HttpServerExchange exchange) {
            exchange.setStatusCode(this.status.getHttpStatus());

            this.headers.forEach((name, value) -> exchange.getResponseHeaders().add(new HttpString(name), value));

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
