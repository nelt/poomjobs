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

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryDispatcher {

    private final HashMap<String, LinkedList<JobRunner>> runners = new HashMap<>();

    private final WeakReference<InMemoryJobStore> storeReference;
    private final WeakReference<JobQueueService> queueServiceReference;

    public InMemoryDispatcher(InMemoryJobStore store, JobQueueService queueService) {
        this.storeReference = new WeakReference<>(store);
        this.queueServiceReference = new WeakReference<>(queueService);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (InMemoryDispatcher.this.runners) {
                        for (Job job : InMemoryDispatcher.this.pendingJobs()) {
                            if (InMemoryDispatcher.this.runners.containsKey(job.getJob()) && !InMemoryDispatcher.this.runners.get(job.getJob()).isEmpty()) {
                                JobRunner runner = InMemoryDispatcher.this.runners.get(job.getJob()).pop();
                                try {
                                    InMemoryDispatcher.this.start(job.getUuid());
                                    runner.run(job);
                                } catch (NoSuchJobException | InconsistentJobStatusException e) {
                                    e.printStackTrace();
                                } finally {
                                    InMemoryDispatcher.this.runners.get(job.getJob()).push(runner);
                                }
                            }
                        }
                        try {
                            InMemoryDispatcher.this.runners.wait(100L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }
}
