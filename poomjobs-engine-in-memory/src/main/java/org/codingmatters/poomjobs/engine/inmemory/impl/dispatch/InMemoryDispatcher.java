package org.codingmatters.poomjobs.engine.inmemory.impl.dispatch;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.NoSuchJobException;
import org.codingmatters.poomjobs.engine.inmemory.impl.store.InMemoryJobStore;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryDispatcher {

    private final HashMap<String, LinkedList<JobRunner>> runners = new HashMap<>();

    private final InMemoryJobStore store;
    private final JobQueueService queueService;

    public InMemoryDispatcher(InMemoryJobStore store, JobQueueService queueService) {
        this.store = store;
        this.queueService = queueService;
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

    public void start() {
        new Thread(() -> {
            while(true) {
                synchronized (this.runners) {
                    for (Job job : this.store.pendingJobs()) {
                        if(this.runners.containsKey(job.getJob()) && ! this.runners.get(job.getJob()).isEmpty()) {
                            JobRunner runner = this.runners.get(job.getJob()).pop();
                            try {
                                this.queueService.start(job.getUuid());
                                runner.run(job);
                            } catch (NoSuchJobException | InconsistentJobStatusException e) {
                                e.printStackTrace();
                            } finally {
                                this.runners.get(job.getJob()).push(runner);
                            }
                        }
                    }
                    try {
                        this.runners.wait(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
