package org.codingmatters.poomjobs.apis.jobs;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Created by nel on 03/07/15.
 */
public class Job {

    private final UUID uuid;
    private final String job;
    private final String [] arguments;
    private final Long retentionDelay;
    private final LocalDateTime submissionTime;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final JobStatus status;
    private final String [] results;
    private final String [] errors;

    public Job(UUID uuid, String job, String[] arguments, Long retentionDelay, LocalDateTime submissionTime, LocalDateTime startTime, LocalDateTime endTime, JobStatus status, String[] results, String[] errors) {
        this.uuid = uuid;
        this.job = job;
        this.arguments = arguments;
        this.retentionDelay = retentionDelay;
        this.submissionTime = submissionTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.results = results;
        this.errors = errors;
    }

    public UUID getUuid() {
        return uuid;
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

    public LocalDateTime getSubmissionTime() {
        return submissionTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public JobStatus getStatus() {
        return status;
    }

    public String[] getResults() {
        return results;
    }

    public String[] getErrors() {
        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        return !(uuid != null ? !uuid.equals(job.uuid) : job.uuid != null);

    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Job{" +
                "status=" + status +
                ", job='" + job + '\'' +
                ", uuid=" + uuid +
                '}';
    }
}
