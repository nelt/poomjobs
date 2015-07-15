package org.codingmatters.poomjobs.apis;


import org.codingmatters.poomjobs.apis.factory.ServiceFactory;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;

import java.util.HashMap;

/**
 * Created by nel on 07/07/15.
 */
public class Configuration {
    private static final String IN_MEMORY_FACTORY = "org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory";

    static public Builder defaults(String url) {
        return new Builder(url);
    }

    static public class Builder {
        private final String url;
        private String serviceFactoryClass = IN_MEMORY_FACTORY;

        private final HashMap<String, Object> options = new HashMap<String, Object>();

        public Builder(String url) {
            this.url = url;
        }

        public Builder withServiceFactoryClass(String serviceFactoryClass) {
            this.serviceFactoryClass = serviceFactoryClass;
            return this;
        }

        public Builder withOption(String name, Object option) {
            this.options.put(name, option);
            return this;
        }
        public Configuration config() throws ServiceFactoryException {
            try {
                return new Configuration(
                        this.url,
                        this.options,
                        (ServiceFactory) Class.forName(this.serviceFactoryClass).newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new ServiceFactoryException(this.serviceFactoryClass, e);
            }
        }
    }

    private final String url;
    private final HashMap<String, Object> options;
    private final ServiceFactory serviceFactory;

    private Configuration(String url, HashMap<String, Object> options, ServiceFactory serviceFactory) {
        this.url = url;
        this.serviceFactory = serviceFactory;
        this.options = new HashMap<>(options);
    }

    public String getUrl() {
        return url;
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
