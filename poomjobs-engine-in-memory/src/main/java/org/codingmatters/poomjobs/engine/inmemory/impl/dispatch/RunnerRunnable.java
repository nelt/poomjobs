package org.codingmatters.poomjobs.engine.inmemory.impl.dispatch;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;

/**
 * Created by nel on 19/07/15.
 */
public class RunnerRunnable implements Runnable {
    private final Job job;
    private final JobRunner runner;

    public RunnerRunnable(Job job, JobRunner runner) {
        this.job = job;
        this.runner = runner;
    }

    @Override
    public void run() {
        this.runner.run(this.job);
    }
}
