package org.codingmatters.poomjobs.apis.services.queue;

import java.util.Arrays;

/**
 * Created by nel on 06/07/15.
 */
public class JobSubmission {

    static public Builder job(String job) {
        return new Builder().withJob(job);
    }

    static public class Builder {
        private String job;
        private String [] arguments;
        private Long retentionDelay;

        public Builder withJob(String job) {
            this.job = job;
            return this;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobSubmission that = (JobSubmission) o;

        if (job != null ? !job.equals(that.job) : that.job != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(arguments, that.arguments)) return false;
        return !(retentionDelay != null ? !retentionDelay.equals(that.retentionDelay) : that.retentionDelay != null);

    }

    @Override
    public int hashCode() {
        int result = job != null ? job.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(arguments);
        result = 31 * result + (retentionDelay != null ? retentionDelay.hashCode() : 0);
        return result;
    }
}
