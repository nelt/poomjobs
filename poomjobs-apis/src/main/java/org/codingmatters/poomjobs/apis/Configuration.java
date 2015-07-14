package org.codingmatters.poomjobs.apis;


import java.util.HashMap;

/**
 * Created by nel on 07/07/15.
 */
public class Configuration {

    static public Builder defaults() {
        return new Builder();
    }

    static public class Builder {
        private final HashMap<String, Object> options = new HashMap<String, Object>();

        public Builder withOption(String name, Object option) {
            this.options.put(name, option);
            return this;
        }
        public Configuration config() {
            return new Configuration(this.options);
        }
    }

    private final HashMap<String, Object> options;

    private Configuration(HashMap<String, Object> options) {
        this.options = new HashMap<String, Object>(options);
    }

    public boolean hasOption(String name) {
        return this.options.containsKey(name);
    }

    public Object getOption(String name) {
        return this.options.get(name);
    }

}
