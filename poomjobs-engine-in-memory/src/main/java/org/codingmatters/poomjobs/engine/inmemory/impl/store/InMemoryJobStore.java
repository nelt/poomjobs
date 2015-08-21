package org.codingmatters.poomjobs.engine.inmemory.impl.store;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.engine.JobDispatcher;
import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.inmemory.impl.dispatch.InMemoryDispatcher;
import org.codingmatters.poomjobs.engine.inmemory.impl.jobs.InMemoryJobList;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by nel on 09/07/15.
 */
public class InMemoryJobStore implements JobStore {

    private final LinkedList<Job> jobs = new LinkedList<>();
    private final HashSet<Job> finishedJobs = new HashSet<>();
    private final HashMap<String, List<Job>> pendingJobs = new HashMap<>();

    private final Thread cleanerThread;
    private final CleanerRunnable cleaner;

    public InMemoryJobStore() {
        this.cleaner = new CleanerRunnable(this);
        this.cleanerThread = new Thread(this.cleaner);
        this.cleanerThread.setName("in-memory-job-store-cleaner@" + this.hashCode());
    }

    @Override
    public synchronized void store(Job job) {
        int index = this.jobs.indexOf(job);
        if(index != -1) {
            this.jobs.set(index, job);
        } else {
            this.jobs.add(job);
        }

        if(job.getStatus().equals(JobStatus.PENDING)) {
            if(! this.pendingJobs.containsKey(job.getJob())) {
                this.pendingJobs.put(job.getJob(), new LinkedList<>());
            }
            this.pendingJobs.get(job.getJob()).add(job);
        } else if(this.pendingJobs.get(job.getJob()) != null && this.pendingJobs.get(job.getJob()).contains(job)){
            this.pendingJobs.get(job.getJob()).remove(job);
        }

        if(job.getEndTime() != null) {
            this.finishedJobs.add(job);
        }
    }

    @Override
    public synchronized JobList list(ListQuery query) {
        JobList results = new InMemoryJobList();
        for(
                long i = 0, scroll = 0 ;
                results.size() < query.getLimit() && i < this.jobs.size();
                i++) {
            Job job = this.jobs.get((int) i);
            if(this.matches(job, query)) {
                scroll++;
                if(scroll > query.getOffset()) {
                    results.add(job);
                }
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

    @Override
    public synchronized Job pendingJob(String jobSpec) {
        if(this.pendingJobs.containsKey(jobSpec) && ! this.pendingJobs.get(jobSpec).isEmpty()) {
            return this.pendingJobs.get(jobSpec).get(0);
        } else {
            return null;
        }
    }

    @Override
    public synchronized void clean() {
        LinkedList<Job> expired = this.getExpiredJobs();
        if(! expired.isEmpty()) {
            this.remove(expired);
        }
    }

    private LinkedList<Job> getExpiredJobs() {
        LinkedList<Job> expired = new LinkedList<>();
        for (Job finishedJob : this.finishedJobs) {
            if(this.isRetentionDelayExpired(finishedJob)) {
                expired.add(finishedJob);
            }
        }
        return expired;
    }

    private synchronized void remove(LinkedList<Job> jobs) {
        for (Job job : jobs) {
            if(job.getStatus().equals(JobStatus.PENDING)) {
                if(this.pendingJobs.containsKey(job.getJob())) {
                    this.pendingJobs.get(job.getJob()).remove(job);
                }
            }
        }

        this.jobs.removeAll(jobs);
        this.finishedJobs.removeAll(jobs);
    }

    protected boolean isRetentionDelayExpired(Job finishedJob) {
        return finishedJob.getEndTime().plus(finishedJob.getRetentionDelay(), ChronoUnit.MILLIS)
                .isBefore(LocalDateTime.now());
    }

    @Override
    public void start() {
        this.cleaner.requestStart();
        if(! this.cleanerThread.isAlive()) {
            this.cleanerThread.start();
        }
    }

    @Override
    public void stop() {
        this.cleaner.requestStop();
        try {
            this.cleanerThread.join(10 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
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
