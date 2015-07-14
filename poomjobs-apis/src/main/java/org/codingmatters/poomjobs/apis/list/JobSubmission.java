package org.codingmatters.poomjobs.apis.list;

import java.util.UUID;

/**
 * Created by nel on 06/07/15.
 */
public class JobSubmission {

    static public Builder job(String job) {
        return new Builder(job);
    }

    static public class Builder {
        private final String job;
        private String [] arguments;
        private Long retentionDelay;

        private Builder(String job) {
            this.job = job;
        }

        public Builder withArguments(String ... arguments) {
            this.arguments = arguments;
            return this;
        }

        public Builder withRetentionDelay(Long retentionDelay) {
            this.retentionDelay = retentionDelay;
            return this;
        }

        public JobSubmission submission() {
            return new JobSubmission(this.job, this.arguments, this.retentionDelay);
        }
    }

    private final String job;
    private final String [] arguments;
    private final Long retentionDelay;

    private JobSubmission(String job, String[] arguments, Long retentionDelay) {
        this.job = job;
        this.arguments = arguments;
        this.retentionDelay = retentionDelay;
    }

    public String getJob() {
        return job;
    }

    public String[] getArguments() {
        return arguments;
    }

    public Long getRetentionDelay() {
        return retentionDelay;
    }
}
