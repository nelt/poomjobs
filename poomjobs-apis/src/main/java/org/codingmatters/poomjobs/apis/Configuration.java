package org.codingmatters.poomjobs.apis;


import org.codingmatters.poomjobs.apis.factory.ServiceFactory;

import java.util.HashMap;

/**
 * Created by nel on 07/07/15.
 */
public class Configuration {

    static public Builder defaults(ServiceFactory serviceFactory) {
        return new Builder(serviceFactory);
    }

    static public class Builder {
        private final ServiceFactory serviceFactory;

        private final HashMap<String, Object> options = new HashMap<String, Object>();

        public Builder(ServiceFactory serviceFactory) {
            this.serviceFactory = serviceFactory;
        }

        public Builder withOption(String name, Object option) {
            this.options.put(name, option);
            return this;
        }
        public Configuration config() {
            return new Configuration(
                    this.options,
                    this.serviceFactory);
        }
    }

    private final HashMap<String, Object> options;
    private final ServiceFactory serviceFactory;

    private Configuration(HashMap<String, Object> options, ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
        this.options = new HashMap<>(options);
    }

    public ServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    public boolean hasOption(String name) {
        return this.options.containsKey(name);
    }

    public Object getOption(String name) {
        return this.options.get(name);
    }

}
