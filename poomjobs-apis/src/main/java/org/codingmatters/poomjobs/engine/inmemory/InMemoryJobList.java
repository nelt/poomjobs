package org.codingmatters.poomjobs.engine.inmemory;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobBuilders;
import org.codingmatters.poomjobs.apis.list.JobList;
import org.codingmatters.poomjobs.apis.list.exception.JobListException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by nel on 07/07/15.
 */
public class InMemoryJobList extends LinkedList<Job> implements JobList {

    public InMemoryJobList(Collection<? extends Job> c) {
        super(c);
    }

    @Override
    public boolean contains(UUID uuid) throws JobListException {
        return this.contains(JobBuilders.uuid(uuid));
    }

    @Override
    public Job get(UUID uuid) throws JobListException {
        int index = this.indexOf(JobBuilders.uuid(uuid));
        if(index == -1) throw new JobListException("no job with uuid " + uuid);
        return this.get(index);
    }
}
