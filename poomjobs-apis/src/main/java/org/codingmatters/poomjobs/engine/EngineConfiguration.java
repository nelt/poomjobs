package org.codingmatters.poomjobs.engine;


import java.time.Duration;

/**
 * Created by nel on 07/07/15.
 */
public class EngineConfiguration {
    static public final Long DEFAULT_DEFAULT_RETENTION_DELAY = Duration.ofHours(2).toMinutes();


    static public Builder defaults() {
        return new Builder();
    }

    static public class Builder {
        private Long defaultRetentionDelay = DEFAULT_DEFAULT_RETENTION_DELAY;

        public Builder withDefaultRetentionDelay(Long defaultRetentionDelay) {
            this.defaultRetentionDelay = defaultRetentionDelay;
            return this;
        }

        public EngineConfiguration config() {
            return new EngineConfiguration(this.defaultRetentionDelay);
        }
    }

    private final Long defaultRetentionDelay;

    public EngineConfiguration(Long defaultRetentionDelay) {
        this.defaultRetentionDelay = defaultRetentionDelay;
    }

    public Long getDefaultRetentionDelay() {
        return defaultRetentionDelay;
    }

}
