package org.codingmatters.poomjobs.engine.inmemory.impl.dispatch;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.jobs.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.NoSuchJobException;
import org.codingmatters.poomjobs.engine.inmemory.impl.jobs.InMemoryJobList;
import org.codingmatters.poomjobs.engine.inmemory.impl.store.InMemoryJobStore;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryDispatcher {

    private final HashMap<String, LinkedList<JobRunner>> runners = new HashMap<>();

    private final WeakReference<InMemoryJobStore> storeReference;
    private final WeakReference<JobQueueService> queueServiceReference;

    private final DispatcherRunnable dispatcherRunnable;
    private Thread dispatcherThread;
    private final HashMap<Future, JobRunner> runningRunners = new HashMap<>();

    private final ExecutorService runnerPool;

    public InMemoryDispatcher(InMemoryJobStore store, JobQueueService queueService) {
        this.storeReference = new WeakReference<>(store);
        this.queueServiceReference = new WeakReference<>(queueService);

        this.dispatcherRunnable = new DispatcherRunnable(this);
        this.dispatcherThread = new Thread(this.dispatcherRunnable);
        this.dispatcherThread.setName("in-memory-dispatcher@" + this.hashCode());

        this.runnerPool = Executors.newCachedThreadPool();
    }


    public void register(JobRunner runner, String... forJobs) {
        synchronized (this.runners) {
            for (String jobName : forJobs) {
                if (!this.runners.containsKey(jobName)) {
                    this.runners.put(jobName, new LinkedList<>());
                }
                this.runners.get(jobName).add(runner);
            }
            this.runners.notifyAll();
        }
    }

    private JobList pendingJobs() {
        InMemoryJobStore store = this.storeReference.get();
        return store != null ? store.pendingJobs() : new InMemoryJobList();
    }

    private void start(UUID uuid) throws InconsistentJobStatusException, NoSuchJobException {
        JobQueueService queueService = this.queueServiceReference.get();
        if(queueService != null) {
            queueService.start(uuid);
        }
    }

    public void start() {
        this.dispatcherRunnable.requestStart();
        if(! this.dispatcherThread.isAlive()) {
            this.dispatcherThread.start();
        }
    }

    public void stop() {
        this.dispatcherRunnable.requestStop();
        try {
            this.dispatcherThread.join(10 * 1000L);
            this.runnerPool.shutdownNow();
            this.runnerPool.awaitTermination(10 * 1000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final HashSet<JobRunner> lockedRunners = new HashSet<>();

    public void dispatch() {
        this.unlockTerminatedRunners();
        this.pendingJobs().forEach(job -> {
            JobRunner runner = this.lockRunnerForJob(job);
            if(runner != null) {
                try {
                    this.startRunnerForJob(runner, job);
                } catch (InconsistentJobStatusException | NoSuchJobException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private synchronized JobRunner lockRunnerForJob(Job job) {
        if (this.runners.containsKey(job.getJob()) && !this.runners.get(job.getJob()).isEmpty()) {
            for (JobRunner runner : this.runners.get(job.getJob())) {
                if(! this.lockedRunners.contains(runner)) {
                    this.lockedRunners.add(runner);
                    this.runners.get(job.getJob()).remove(runner);
                    this.runners.get(job.getJob()).add(runner);
                    return runner;
                }
            }
        }
        return null;
    }

    private synchronized void startRunnerForJob(JobRunner runner, Job job) throws InconsistentJobStatusException, NoSuchJobException {
        this.start(job.getUuid());
        Future<?> running = this.runnerPool.submit(new RunnerRunnable(job, runner));
        this.runningRunners.put(running, runner);
    }

    private synchronized void unlockTerminatedRunners() {
        new HashMap<>(this.runningRunners).forEach((future, runner) -> {
            if(future.isDone()) {
                this.lockedRunners.remove(runner);
                this.runningRunners.remove(future);
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        this.stop();
        super.finalize();
    }
}
