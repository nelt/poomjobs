package org.codingmatters.poomjobs.apis.jobs;

import org.codingmatters.poomjobs.apis.exception.InconsistentJobStatusException;

import java.util.Arrays;
import java.util.HashSet;

import static java.time.LocalDateTime.now;
import static org.codingmatters.poomjobs.apis.jobs.JobBuilders.from;
import static org.codingmatters.poomjobs.apis.jobs.JobStatus.*;

/**
 * Created by nel on 07/07/15.
 */
public enum JobOperation {

    START(
            (job) -> from(job)
                    .withStatus(RUNNING)
                    .withStartTime(now())
                    .job(),
            PENDING
    ),
    STOP(
            (job) -> from(job)
                    .withStatus(DONE)
                    .withEndTime(now())
                    .job(),
            RUNNING
    ),
    FAIL(
            (job) -> from(job)
                    .withStatus(FAILED)
                    .withEndTime(now())
                    .job(),
            RUNNING
    ),
    CANCEL(
            (job) -> from(job)
                    .withStatus(CANCELED)
                    .withEndTime(now())
                    .job(),
            PENDING, RUNNING
    )
    ;

    private JobMutation jobMutation;
    private HashSet<JobStatus> consistentStatuses;

    JobOperation(JobMutation jobMutation, JobStatus ... consistentStatuses) {
        this.jobMutation = jobMutation;
        this.consistentStatuses = new HashSet<>(Arrays.asList(consistentStatuses));
    }

    public Job operate(Job job) throws InconsistentJobStatusException {
        return this.operate(job, j -> j);
    }

    public Job operate(Job job, JobMutation additionalMutation) throws InconsistentJobStatusException {
        if(! this.consistentStatuses.contains(job.getStatus())) {
            throw new InconsistentJobStatusException(String.format(
                    "cannot %s job %s with status %s (should be one of %s)",
                    this.name().toLowerCase(), job.getUuid(), job.getStatus(), this.consistentStatuses
            ));
        } else {
            return additionalMutation.mutate(this.jobMutation.mutate(job));
        }
    }

    /**
     * Created by nel on 07/07/15.
     */
    public interface JobMutation {
        Job mutate(Job job);
    }
}
