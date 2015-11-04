package org.codingmatters.poomjobs.http.internal;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by nel on 04/11/15.
 */
public class ParametrizedPathPattern {
    private final Pattern pattern;
    private final List<String> names;

    public ParametrizedPathPattern(Pattern pattern, List<String> names) {
        this.pattern = pattern;
        this.names = names;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public List<String> getNames() {
        return names;
    }
}
