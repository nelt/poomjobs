package org.codingmatters.poomjobs.http;

import java.util.AbstractMap;
import java.util.LinkedHashMap;

/**
 * Created by nel on 02/11/15.
 */
public class RestService {

    public static RestService root(String rootPath) {
        return new RestService(rootPath);
    }

    public static RestResource resource() {
        return RestResource.resource();
    }


    private final String rootPath;
    private final LinkedHashMap<String, RestResource> resources = new LinkedHashMap<>();

    private RestService(String rootPath) {
        this.rootPath = rootPath;
    }

    public RestService resource(String name, RestResource resource) {
        this.resources.put(name, resource);
        return this;
    }

    public String getRootPath() {
        return rootPath;
    }

    public RestResource getMatchingResource(String name) {
        return this.resources.entrySet().stream()
                .filter(entry -> name.equals(entry.getKey()))
                .findFirst()
                .orElse(new AbstractMap.SimpleEntry<String, RestResource>(name, RestResource.notFound()))
                .getValue();
    }
}
