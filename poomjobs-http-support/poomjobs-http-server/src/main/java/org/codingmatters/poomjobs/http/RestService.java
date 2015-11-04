package org.codingmatters.poomjobs.http;

import org.codingmatters.poomjobs.http.internal.ParametrizedPathPattern;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nel on 02/11/15.
 */
public class RestService {

    private static final Pattern PARAMETRIZED_PATH_PATTERN = Pattern.compile("\\{([^{]*)\\}");

    public static RestService root(String rootPath) {
        return new RestService(rootPath);
    }

    public static RestResource resource() {
        return RestResource.resource();
    }


    private final String rootPath;

    private final LinkedHashMap<String, RestResource> resources = new LinkedHashMap<>();
    private final LinkedHashMap<ParametrizedPathPattern, RestResource> parametrizedPathResources = new LinkedHashMap<>();

    private RestService(String rootPath) {
        this.rootPath = rootPath;
    }

    public RestService resource(String name, RestResource resource) {
        if(this.isParametrizedPathName(name)) {
            this.parametrizedPathResources.put(this.parametrizedPathPattern(name), resource);
        } else {
            this.resources.put(name, resource);
        }
        return this;
    }

    private ParametrizedPathPattern parametrizedPathPattern(String name) {
        List<String> names =new LinkedList<>();
        Matcher parameters = PARAMETRIZED_PATH_PATTERN.matcher(name);

        while(parameters.find()) {
            names.add(parameters.group(1));
        }

        ParametrizedPathPattern result = new ParametrizedPathPattern(Pattern.compile(name.replaceAll("\\{[^{]*\\}", "(.*)")), names);
        return result;
    }

    private boolean isParametrizedPathName(String name) {
        return PARAMETRIZED_PATH_PATTERN.matcher(name).find();
    }

    public String getRootPath() {
        return rootPath;
    }

    public RestResourceInvocation getMatchingResource(String name) {
        return this.resources.entrySet().stream()
                .filter(entry -> name.equals(entry.getKey()))
                .map(entry -> new RestResourceInvocation(entry.getValue()))
                .findFirst()
                .orElse(this.findParametrizedResource(name)
                        .orElse(new RestResourceInvocation(RestResource.notFound())))
        ;
    }

    private Optional<RestResourceInvocation> findParametrizedResource(String name) {
        return this.parametrizedPathResources.entrySet().stream()
                .filter(entry -> entry.getKey().getPattern().matcher(name).find())
                .map(entry -> {
                    Map<String, List<String>> parameters = new HashMap<>();

                    Matcher values = entry.getKey().getPattern().matcher(name);
                    if (values.matches()) {
                        for (int i = 1; i <= values.groupCount(); i++) {
                            String param = entry.getKey().getNames().get(i - 1);
                            if (!parameters.containsKey(param)) {
                                parameters.put(param, new ArrayList<>());
                            }
                            parameters.get(param).add(values.group(i));
                        }
                    }

                    return new RestResourceInvocation(parameters, entry.getValue());
                })
                .findFirst();
    }
}
