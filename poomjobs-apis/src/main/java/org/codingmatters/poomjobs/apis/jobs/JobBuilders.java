package org.codingmatters.poomjobs.apis.jobs;

import org.codingmatters.poomjobs.apis.queue.JobSubmission;

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
        Builder result = new Builder();

        return result;
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

        public Builder withArguments(String[] arguments) {
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
    }
}
