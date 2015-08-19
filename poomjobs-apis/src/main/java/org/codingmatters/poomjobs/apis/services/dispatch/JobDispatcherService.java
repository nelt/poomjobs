package org.codingmatters.poomjobs.apis.services.dispatch;

/**
 * Created by nel on 16/07/15.
 */
public interface JobDispatcherService {
    void register(JobRunner runner, String jobSpec);
}
