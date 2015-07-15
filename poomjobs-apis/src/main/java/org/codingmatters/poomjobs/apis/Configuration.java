package org.codingmatters.poomjobs.apis;


import java.util.HashMap;

/**
 * Created by nel on 07/07/15.
 */
public class Configuration {

    static public Builder defaults(String url) {
        return new Builder(url);
    }

    static public class Builder {
        private final String url;
        private final HashMap<String, Object> options = new HashMap<String, Object>();

        public Builder(String url) {
            this.url = url;
        }

        public Builder withOption(String name, Object option) {
            this.options.put(name, option);
            return this;
        }
        public Configuration config() {
            return new Configuration(this.url, this.options);
        }
    }

    private final String url;
    private final HashMap<String, Object> options;

    private Configuration(String url, HashMap<String, Object> options) {
        this.url = url;
        this.options = new HashMap<String, Object>(options);
    }

    public String getUrl() {
        return url;
    }

    public boolean hasOption(String name) {
        return this.options.containsKey(name);
    }

    public Object getOption(String name) {
        return this.options.get(name);
    }

}
