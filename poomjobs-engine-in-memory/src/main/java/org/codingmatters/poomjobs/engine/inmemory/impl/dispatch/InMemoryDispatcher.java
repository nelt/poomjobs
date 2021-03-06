package org.codingmatters.poomjobs.engine.inmemory.impl.dispatch;

import org.codingmatters.poomjobs.apis.exception.ServiceException;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.engine.JobDispatcher;
import org.codingmatters.poomjobs.engine.JobStore;
import org.codingmatters.poomjobs.engine.exception.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryDispatcher implements JobDispatcher {

    static private Logger log = LoggerFactory.getLogger(InMemoryDispatcher.class);

    private final WeakReference<JobStore> storeReference;
    private final WeakReference<JobQueueService> queueServiceReference;

    private final DispatcherRunnable dispatcherRunnable;
    private final ExecutorService runnerPool;
    private final RunnerStore runnerStore = new RunnerStore();
    private Thread dispatcherThread;

    public InMemoryDispatcher(JobStore store, JobQueueService queueService) {
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

    @Override
    public void register(JobRunner runner, String jobSpec) {
        this.runnerStore.register(runner, jobSpec);
    }

    private void start(UUID uuid) throws ServiceException {
        JobQueueService queueService = this.queueServiceReference.get();
        if(queueService != null) {
            queueService.start(uuid);
        }
    }


    @Override
    public void dispatch() {
        this.runnerStore.unlockTerminated();

        for(String jobSpec : this.runnerStore.registeredJobSpecs()) {
            JobRunner runner = this.runnerStore.lock(jobSpec);
            if(runner != null) {
                Job pending = this.getPendingJob(jobSpec);
                if(pending != null) {
                    try {
                        this.startRunnerForJob(runner, pending);
                    } catch (ServiceException e) {
                        log.error("error starting job " + pending.getUuid(), e);
                    }
                } else {
                    this.runnerStore.unlock(runner);
                }
            }
        }
    }

    private void startRunnerForJob(JobRunner runner, Job job) throws ServiceException {
        this.start(job.getUuid());
        Future<?> running = this.runnerPool.submit(new RunnerRunnable(job, runner));
        this.runnerStore.running(running, runner);
    }

    private Job getPendingJob(String jobSpec) {
        JobStore store = this.storeReference.get();
        try {
            return store != null ? store.pendingJob(jobSpec) : null;
        } catch (StoreException e) {
            log.error("error getting penging job", e);
            return null;
        }
    }


    @Override
    public void start() {
        this.dispatcherRunnable.requestStart();
        if(! this.dispatcherThread.isAlive()) {
            this.dispatcherThread.start();
        }
    }

    @Override
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
