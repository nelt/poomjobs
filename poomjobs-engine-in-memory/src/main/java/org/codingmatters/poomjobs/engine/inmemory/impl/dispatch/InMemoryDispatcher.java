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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryDispatcher {

    private final HashMap<String, LinkedList<JobRunner>> runners = new HashMap<>();

    private final WeakReference<InMemoryJobStore> storeReference;
    private final WeakReference<JobQueueService> queueServiceReference;
    private final DispatcherRunnable dispatcherRunnable;

    private Thread dispatcherThread;

    public InMemoryDispatcher(InMemoryJobStore store, JobQueueService queueService) {
        this.storeReference = new WeakReference<>(store);
        this.queueServiceReference = new WeakReference<>(queueService);

        this.dispatcherRunnable = new DispatcherRunnable(this);
        this.dispatcherThread = new Thread(this.dispatcherRunnable);
        this.dispatcherThread.setName("in-memory-dispatcher@" + this.hashCode());
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void dispatch() {
        synchronized (this.runners) {
            for (Job job : this.pendingJobs()) {
                if (this.runners.containsKey(job.getJob()) && !this.runners.get(job.getJob()).isEmpty()) {
                    JobRunner runner = this.runners.get(job.getJob()).pop();
                    try {
                        this.start(job.getUuid());
                        runner.run(job);
                    } catch (NoSuchJobException | InconsistentJobStatusException e) {
                        e.printStackTrace();
                    } finally {
                        this.runners.get(job.getJob()).push(runner);
                    }
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.stop();
        super.finalize();
    }
}
