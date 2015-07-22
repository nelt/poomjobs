package org.codingmatters.poomjobs.engine.inmemory.impl.store;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
import org.codingmatters.poomjobs.engine.inmemory.impl.jobs.InMemoryJobList;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nel on 09/07/15.
 */
public class InMemoryJobStore {

    private final LinkedList<Job> jobs = new LinkedList<>();
    private final HashSet<Job> finishedJobs = new HashSet<>();

    private final Thread cleanerThread;
    private final CleanerRunnable cleaner;

    public InMemoryJobStore() {
        this.cleaner = new CleanerRunnable(this);
        this.cleanerThread = new Thread(this.cleaner);
        this.cleanerThread.setName("in-memory-job-store-cleaner@" + this.hashCode());
    }

    public synchronized void store(Job job) {
        int index = this.jobs.indexOf(job);
        if(index != -1) {
            this.jobs.set(index, job);
        } else {
            this.jobs.add(job);
        }
        if(job.getEndTime() != null) {
            this.finishedJobs.add(job);
        }
    }

    public synchronized JobList list(ListQuery query) {
        JobList results = new InMemoryJobList();
        for(
                long i = query.getOffset() ;
                results.size() < query.getLimit() && i < this.jobs.size();
                i++) {

            Job job = this.jobs.get((int) i);
            if(this.matches(job, query)) {
                results.add(job);
            }
        }
        return results;
    }

    private boolean matches(Job job, ListQuery query) {
        if(! query.getStatuses().contains(job.getStatus())) {
            return false;
        }
        return true;
    }

    public synchronized JobList pendingJobs() {
        JobList result = new InMemoryJobList();
        for (Job job : this.jobs) {
            if(job.getStatus() == JobStatus.PENDING) {
                result.add(job);
            }
        }
        return result;
    }

    public synchronized void clean() {
        LinkedList<Job> expired = new LinkedList<>();
        for (Job finishedJob : this.finishedJobs) {
            if(this.isRetentionDelayExpired(finishedJob)) {
                expired.add(finishedJob);
            }
        }
        if(! expired.isEmpty()) {
            this.jobs.removeAll(expired);
            this.finishedJobs.removeAll(expired);
        }
    }

    protected boolean isRetentionDelayExpired(Job finishedJob) {
        return finishedJob.getEndTime().plus(finishedJob.getRetentionDelay(), ChronoUnit.MILLIS)
                .isBefore(LocalDateTime.now());
    }

    public void start() {
        this.cleaner.requestStart();
        if(! this.cleanerThread.isAlive()) {
            this.cleanerThread.start();
        }
    }

    public void stop() {
        this.cleaner.requestStop();
        try {
            this.cleanerThread.join(10 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public Job get(Job job) {
        int index = this.jobs.indexOf(job);
        return index != -1 ? this.jobs.get(index) : null;
    }

    @Override
    protected void finalize() throws Throwable {
        this.stop();
        super.finalize();
    }
}
