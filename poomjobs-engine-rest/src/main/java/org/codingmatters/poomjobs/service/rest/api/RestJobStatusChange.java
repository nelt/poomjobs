package org.codingmatters.poomjobs.service.rest.api;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobBuilders;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;

/**
 * Created by nel on 14/12/15.
 */
public class RestJobStatusChange {

    static public class Builder {
        private JobStatus oldStatus;
        private JobBuilders.Builder job;

        public Builder withOldStatus(JobStatus oldStatus) {
            this.oldStatus = oldStatus;
            return this;
        }

        public Builder withJob(JobBuilders.Builder job) {
            this.job = job;
            return this;
        }

        public RestJobStatusChange build() {
            return new RestJobStatusChange(this.oldStatus, this.job.job());
        }
    }

    private final JobStatus oldStatus;
    private final Job job;

    public RestJobStatusChange(JobStatus oldStatus, Job job) {
        this.oldStatus = oldStatus;
        this.job = job;
    }

    public JobStatus getOldStatus() {
        return oldStatus;
    }

    public Job getJob() {
        return job;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RestJobStatusChange that = (RestJobStatusChange) o;

        if (oldStatus != that.oldStatus) return false;
        return !(job != null ? !job.equals(that.job) : that.job != null);

    }

    @Override
    public int hashCode() {
        int result = oldStatus != null ? oldStatus.hashCode() : 0;
        result = 31 * result + (job != null ? job.hashCode() : 0);
        return result;
    }
}
