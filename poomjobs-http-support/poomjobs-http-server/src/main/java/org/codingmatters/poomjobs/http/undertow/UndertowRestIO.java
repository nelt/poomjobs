package org.codingmatters.poomjobs.http.undertow;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.codingmatters.poomjobs.http.RestIO;
import org.codingmatters.poomjobs.http.RestStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nel on 12/11/15.
 */
public class UndertowRestIO implements RestIO {
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
        try (InputStream in = exchange.getInputStream()) {
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

        if (this.status.getMessage() != null && this.content == null) {
            this.contentType("text/plain").content(this.status.getMessage());
        }

        String contentTypeLine = String.format("%s; %s", this.contentType, this.encoding);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentTypeLine);
        if (content != null) {
            exchange.getResponseSender().send(content, Charset.forName(this.encoding));
        }
    }
}
