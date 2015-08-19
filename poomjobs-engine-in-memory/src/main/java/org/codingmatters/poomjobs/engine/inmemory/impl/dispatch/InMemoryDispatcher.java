package org.codingmatters.poomjobs.engine.inmemory.impl.dispatch;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.NoSuchJobException;
import org.codingmatters.poomjobs.engine.inmemory.impl.store.InMemoryJobStore;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryDispatcher {

    private final WeakReference<InMemoryJobStore> storeReference;
    private final WeakReference<JobQueueService> queueServiceReference;

    private final DispatcherRunnable dispatcherRunnable;
    private final ExecutorService runnerPool;
    private final RunnerStore runnerStore = new RunnerStore();
    private Thread dispatcherThread;

    public InMemoryDispatcher(InMemoryJobStore store, JobQueueService queueService) {
        this.storeReference = new WeakReference<>(store);
        this.queueServiceReference = new WeakReference<>(queueService);

        this.dispatcherRunnable = new DispatcherRunnable(this);
        this.dispatcherThread = new Thread(this.dispatcherRunnable);
        this.dispatcherThread.setName("in-memory-dispatcher@" + this.hashCode());

        this.runnerPool = Executors.newCachedThreadPool(this.createThreadFactory());
    }

    protected ThreadFactory createThreadFactory() {
        return new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread result = new Thread(r);
                result.setName(dispatcherThread.getName() + "-worker-" + this.counter.getAndIncrement());
                return result;
            }
        };
    }

    public void register(JobRunner runner, String jobSpec) {
        this.runnerStore.register(runner, jobSpec);
    }

    private void start(UUID uuid) throws InconsistentJobStatusException, NoSuchJobException {
        JobQueueService queueService = this.queueServiceReference.get();
        if(queueService != null) {
            queueService.start(uuid);
        }
    }


    public void dispatch() {
        this.runnerStore.unlockTerminated();

        for(String jobSpec : this.runnerStore.registeredJobSpecs()) {
            JobRunner runner = this.runnerStore.lock(jobSpec);
            if(runner != null) {
                Job pending = this.getPendingJob(jobSpec);
                if(pending != null) {
                    try {
                        this.startRunnerForJob(runner, pending);
                    } catch (InconsistentJobStatusException | NoSuchJobException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.runnerStore.unlock(runner);
                }
            }
        }
    }

    private void startRunnerForJob(JobRunner runner, Job job) throws InconsistentJobStatusException, NoSuchJobException {
        this.start(job.getUuid());
        Future<?> running = this.runnerPool.submit(new RunnerRunnable(job, runner));
        this.runnerStore.running(running, runner);
    }

    private Job getPendingJob(String jobSpec) {
        InMemoryJobStore store = this.storeReference.get();
        return store != null ? store.pendingJob(jobSpec) : null;
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

    @Override
    protected void finalize() throws Throwable {
        this.stop();
        super.finalize();
    }
}
