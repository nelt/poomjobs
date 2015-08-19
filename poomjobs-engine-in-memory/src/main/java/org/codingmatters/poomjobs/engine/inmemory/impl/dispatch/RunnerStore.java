package org.codingmatters.poomjobs.engine.inmemory.impl.dispatch;

import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by nel on 19/08/15.
 */
public class RunnerStore {

    private final HashMap<String, LinkedList<JobRunner>> runners = new HashMap<>();
    private final HashSet<JobRunner> lockedRunners = new HashSet<>();
    private final HashMap<Future, JobRunner> runningRunners = new HashMap<>();


    public synchronized void register(JobRunner runner, String jobSpec) {
        if (!this.runners.containsKey(jobSpec)) {
            this.runners.put(jobSpec, new LinkedList<>());
        }
        this.runners.get(jobSpec).add(runner);
    }

    public synchronized String[] registeredJobSpecs() {
        Set<String> results = this.runners.keySet();
        return results.toArray(new String[results.size()]);
    }

    public synchronized JobRunner lock(String jobSpec) {
        if (this.runners.containsKey(jobSpec) && !this.runners.get(jobSpec).isEmpty()) {
            for (JobRunner runner : this.runners.get(jobSpec)) {
                if(! this.lockedRunners.contains(runner)) {
                    this.lockedRunners.add(runner);
                    this.runners.get(jobSpec).remove(runner);
                    this.runners.get(jobSpec).add(runner);
                    return runner;
                }
            }
        }
        return null;
    }

    public synchronized void running(Future<?> running, JobRunner runner) {
        this.runningRunners.put(running, runner);
    }

    public synchronized void unlock(JobRunner runner) {
        this.lockedRunners.remove(runner);
    }

    public synchronized void unlockTerminated() {
        new HashMap<>(this.runningRunners).forEach((future, runner) -> {
            if (future.isDone()) {
                this.unlock(runner);
                this.runningRunners.remove(future);
            }
        });
    }
}
