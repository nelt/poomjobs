package org.codingmatters.poomjobs.http;

import java.util.EnumMap;

/**
 * Created by nel on 02/11/15.
 */
public class RestResource {

    public static RestResource resource() {
        return new RestResource();
    }

    public RestResource GET(RestMethodHandler methodHandler) {
        this.handlers.put(Method.GET, methodHandler);
        return this;
    }

    public RestResource POST(RestMethodHandler methodHandler) {
        this.handlers.put(Method.POST, methodHandler);
        return this;
    }

    public RestResource PUT(RestMethodHandler methodHandler) {
        this.handlers.put(Method.PUT, methodHandler);
        return this;
    }

    public RestResource DELETE(RestMethodHandler methodHandler) {
        this.handlers.put(Method.DELETE, methodHandler);
        return this;
    }

    private EnumMap<Method, RestMethodHandler> handlers = new EnumMap<>(Method.class);

    public RestMethodHandler handler(Method method) {
        return this.handlers.getOrDefault(method, RestMethodHandler.METHOD_NOT_ALLOWED);
    }

    public enum Method {
        GET, POST, PUT, DELETE
    }


    static private final RestResource RESOURCE_NOT_FOUND = new RestResource()
            .GET(RestMethodHandler.RESOURCE_NOT_FOUND)
            .POST(RestMethodHandler.RESOURCE_NOT_FOUND)
            .PUT(RestMethodHandler.RESOURCE_NOT_FOUND)
            .DELETE(RestMethodHandler.RESOURCE_NOT_FOUND)
            ;

    public static RestResource notFound() {
        return RESOURCE_NOT_FOUND;
    }
}
