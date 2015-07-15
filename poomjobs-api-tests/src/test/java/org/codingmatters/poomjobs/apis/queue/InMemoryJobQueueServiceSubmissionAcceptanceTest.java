package org.codingmatters.poomjobs.apis.queue;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.queue.JobSubmission;
import org.codingmatters.poomjobs.engine.EngineConfiguration;
import org.junit.Before;
import org.junit.Test;

import static java.time.LocalDateTime.now;
import static org.codingmatters.poomjobs.apis.Configuration.defaults;
import static org.codingmatters.poomjobs.test.utils.Helpers.array;
import static org.codingmatters.poomjobs.test.utils.TimeMatchers.near;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 09/07/15.
 */
public class InMemoryJobQueueServiceSubmissionAcceptanceTest extends JobQueueServiceSubmissionAcceptanceTest {


    @Override
    protected Configuration getQueueServiceConfig() throws ServiceFactoryException {
        return defaults("test").config();
    }

    @Override
    protected Long getExpectedDefaultRetentionDelay() {
        return EngineConfiguration.defaults().config().getDefaultRetentionDelay();
    }


}
