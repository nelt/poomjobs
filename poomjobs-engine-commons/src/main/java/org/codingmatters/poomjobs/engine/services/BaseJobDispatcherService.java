package org.codingmatters.poomjobs.engine.services;

import org.codingmatters.poomjobs.apis.services.dispatch.JobDispatcherService;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.engine.JobDispatcher;

import java.lang.ref.WeakReference;

/**
 * Created by nel on 21/08/15.
 */
public class BaseJobDispatcherService implements JobDispatcherService {
    private final JobDispatcher dispatcher;

    public BaseJobDispatcherService(JobDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void register(JobRunner runner, String jobSpec) {
        this.dispatcher.register(runner, jobSpec);
    }

}
