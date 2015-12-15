package org.codingmatters.poomjobs.http.undertow;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.codingmatters.poomjobs.http.RestIO;
import org.codingmatters.poomjobs.http.RestStatus;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created by nel on 12/11/15.
 */
public class UndertowRestIO extends UndertowRestInput implements RestIO {

    private RestStatus status = RestStatus.OK;
    private String contentType = "text/plain";
    private String encoding = "UTF-8";
    private String content;
    private final HashMap<String, String> headers = new HashMap<>();


    public UndertowRestIO(HttpServerExchange exchange) throws IOException {
        super(exchange.getQueryParameters(), exchange.getPathParameters(), exchange.getInputStream());
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
    @Override
    public RestIO header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public void send(HttpServerExchange exchange) {
        exchange.setStatusCode(this.status.getHttpStatus());

        this.headers.forEach((name, value) -> exchange.getResponseHeaders().add(new HttpString(name), value));

        if (this.status.getMessage() != null && this.content == null) {
            this.contentType("text/plain").content(this.status.getMessage());
        }

        String contentTypeLine = String.format("%s; charset=%s", this.contentType, this.encoding);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentTypeLine);
        if (content != null) {
            exchange.getResponseSender().send(content, Charset.forName(this.encoding));
        }
    }
}
