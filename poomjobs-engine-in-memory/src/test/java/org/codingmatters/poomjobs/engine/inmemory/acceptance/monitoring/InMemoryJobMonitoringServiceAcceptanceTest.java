package org.codingmatters.poomjobs.engine.inmemory.acceptance.monitoring;

import org.codingmatters.poomjobs.apis.Configuration;
import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.factory.ServiceFactoryException;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.monitoring.JobMonitoringServiceAcceptanceTest;
import org.codingmatters.poomjobs.apis.services.monitoring.JobMonitoringService;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryJobMonitoringServiceAcceptanceTest extends JobMonitoringServiceAcceptanceTest {

    @Override
    protected Configuration getMonitoringServiceConfig() throws ServiceFactoryException {
        return defaults("test").config();
    }

    @Override
    protected Configuration getQueueServiceConfig() throws ServiceFactoryException {
        return defaults("test").config();
    }

}
