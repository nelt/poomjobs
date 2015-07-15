package org.codingmatters.poomjobs.apis.queue;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.queue.JobSubmission;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.codingmatters.poomjobs.apis.Configuration.defaults;
import static org.codingmatters.poomjobs.test.utils.Helpers.array;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 08/07/15.
 */
public class InMemoryJobQueueServiceWorkflowTest extends JobQueueServiceWorkflowAcceptanceTest {

    @Override
    protected Configuration getQueueServiceConfig() throws ServiceFactoryException {
        return defaults("test").config();
    }

}
