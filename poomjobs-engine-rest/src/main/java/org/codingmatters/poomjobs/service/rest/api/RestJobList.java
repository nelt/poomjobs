package org.codingmatters.poomjobs.service.rest.api;

import org.codingmatters.poomjobs.apis.exception.JobListException;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobBuilders;
import org.codingmatters.poomjobs.apis.jobs.JobList;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by nel on 10/11/15.
 */
public class RestJobList extends LinkedList<Job> implements JobList {

    @Override
    public boolean contains(UUID uuid) throws JobListException {
        return this.contains(JobBuilders.uuid(uuid));
    }

    @Override
    public Job get(UUID uuid) throws JobListException {
        int index = this.indexOf(JobBuilders.uuid(uuid));
        if (index == -1) throw new JobListException("no job with uuid " + uuid);
        return this.get(index);
    }
}
