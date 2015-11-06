package org.codingmatters.poomjobs.apis.jobs;

import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by nel on 03/07/15.
 */
public class JobBuilders {

    static public Job uuid(UUID uuid) {
        return new Job(uuid, null, null, null, null, null, null, null, null, null);
    }

    static public Builder build(String job) {
        return new Builder().withJob(job);
    }

    static public Builder from(JobSubmission jobSubmission) {
        return new Builder()
                .withJob(jobSubmission.getJob())
                .withArguments(jobSubmission.getArguments())
                .withRetentionDelay(jobSubmission.getRetentionDelay())
                ;
    }

    static public Builder from(Job job) {
        return new Builder()
                .withJob(job.getJob())
                .withUuid(job.getUuid())
                .withArguments(job.getArguments())
                .withRetentionDelay(job.getRetentionDelay())

                .withStatus(job.getStatus())

                .withStartTime(job.getSubmissionTime())
                .withStartTime(job.getStartTime())
                .withEndTime(job.getEndTime())

                .withResults(job.getResults())
                .withErrors(job.getErrors())
                ;
    }

    static public class Builder {
        private UUID uuid = UUID.randomUUID();

        private String job;
        private String [] arguments;
        private Long retentionDelay;
        private LocalDateTime submissionTime;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private JobStatus status;
        private String [] results;
        private String [] errors;

        public Job job() {
            return new Job(
                    this.uuid,
                    this.job,
                    this.copy(this.arguments),
                    this.retentionDelay,
                    this.submissionTime,
                    this.startTime,
                    this.endTime,
                    this.status,
                    this.copy(this.results),
                    this.copy(this.errors)
            );
        }

        public Builder withUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

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

        public Builder withSubmissionTime(LocalDateTime submissionTime) {
            this.submissionTime = submissionTime;
            return this;
        }

        public Builder withStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder withEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder withStatus(JobStatus status) {
            this.status = status;
            return this;
        }

        public Builder withResults(String[] results) {
            this.results = results;
            return this;
        }

        public Builder withErrors(String[] errors) {
            this.errors = errors;
            return this;
        }

        private <T> T[] copy(T[] original) {
            if(original == null) return null;
            return Arrays.copyOf(original, original.length);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Builder builder = (Builder) o;

            if (uuid != null ? !uuid.equals(builder.uuid) : builder.uuid != null) return false;
            if (job != null ? !job.equals(builder.job) : builder.job != null) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(arguments, builder.arguments)) return false;
            if (retentionDelay != null ? !retentionDelay.equals(builder.retentionDelay) : builder.retentionDelay != null)
                return false;
            if (submissionTime != null ? !submissionTime.equals(builder.submissionTime) : builder.submissionTime != null)
                return false;
            if (startTime != null ? !startTime.equals(builder.startTime) : builder.startTime != null) return false;
            if (endTime != null ? !endTime.equals(builder.endTime) : builder.endTime != null) return false;
            if (status != builder.status) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(results, builder.results)) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(errors, builder.errors);

        }

        @Override
        public int hashCode() {
            int result = uuid != null ? uuid.hashCode() : 0;
            result = 31 * result + (job != null ? job.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(arguments);
            result = 31 * result + (retentionDelay != null ? retentionDelay.hashCode() : 0);
            result = 31 * result + (submissionTime != null ? submissionTime.hashCode() : 0);
            result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
            result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
            result = 31 * result + (status != null ? status.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(results);
            result = 31 * result + Arrays.hashCode(errors);
            return result;
        }
    }
}
