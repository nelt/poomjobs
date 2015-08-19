package org.codingmatters.poomjobs.engine;

import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;

/**
 * Created by nel on 19/08/15.
 */
public interface JobDispatcher {
    void register(JobRunner runner, String jobSpec);

    void dispatch();

    void start();

    void stop();
}
