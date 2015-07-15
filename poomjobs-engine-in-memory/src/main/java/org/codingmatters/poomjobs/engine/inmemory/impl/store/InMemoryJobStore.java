package org.codingmatters.poomjobs.engine.inmemory.impl.store;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobList;
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

    private AtomicBoolean run = new AtomicBoolean(false);

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

    public synchronized JobList currentList() {
        return new InMemoryJobList(this.jobs);
    }

    public synchronized void clean() {
        LinkedList<Job> expired = new LinkedList<>();
        for (Job finishedJob : this.finishedJobs) {
            if(finishedJob.getEndTime().plus(finishedJob.getRetentionDelay(), ChronoUnit.MILLIS).isAfter(LocalDateTime.now())) {
                expired.add(finishedJob);
            }
        }
        if(! expired.isEmpty()) {
            this.jobs.removeAll(expired);
            this.finishedJobs.removeAll(finishedJobs);
        }
    }

    public void startCleanerThread() {
        synchronized (this.run) {
            if(! this.run.get()) {
                this.run.set(true);
            }
        }
        if(! this.cleanerThread.isAlive()) {
            this.cleanerThread.start();
        }
    }

    public void stopCleanerThread() {
        this.run.set(false);
    }


    public Job get(Job job) {
        int index = this.jobs.indexOf(job);
        return index != -1 ? this.jobs.get(index) : null;
    }

    @Override
    protected void finalize() throws Throwable {
        this.stopCleanerThread();
        super.finalize();
    }

    public boolean isRunning() {
        return this.run.get();
    }
}
