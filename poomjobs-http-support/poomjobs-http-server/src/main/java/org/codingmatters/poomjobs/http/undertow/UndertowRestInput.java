package org.codingmatters.poomjobs.http.undertow;

import org.codingmatters.poomjobs.http.RestInput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by nel on 14/12/15.
 */
public class UndertowRestInput implements RestInput {

    private Map<String, List<String>> parameters;
    private Map<String, List<String>> pathParameters = new HashMap<>();
    private byte[] requestBytes;

    public UndertowRestInput(
            Map<String, Deque<String>> queryParameters,
            Map<String, Deque<String>> pathParameters,
            InputStream requestInputStream) throws IOException {
        this.parameters = new HashMap<>();
        queryParameters.forEach((key, values) -> {
            this.parameters.put(key, new ArrayList<>(values));
        });
        pathParameters.forEach((key, values) -> {
            this.pathParameters.put(key, new ArrayList<>(values));
        });
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = requestInputStream) {
            byte[] buffer = new byte[1024];
            for (int read = in.read(buffer); read != -1; read = in.read(buffer)) {
                out.write(buffer, 0, read);
            }
        }
        this.requestBytes = out.toByteArray();
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

}
