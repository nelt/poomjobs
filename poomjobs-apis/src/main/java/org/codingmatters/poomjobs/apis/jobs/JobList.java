package org.codingmatters.poomjobs.apis.jobs;

import org.codingmatters.poomjobs.apis.jobs.exception.JobListException;

import java.util.List;
import java.util.UUID;

/**
 * Created by nel on 07/07/15.
 */
public interface JobList extends List<Job> {
    boolean contains(UUID uuid) throws JobListException;
    Job get(UUID uuid) throws JobListException;
}
