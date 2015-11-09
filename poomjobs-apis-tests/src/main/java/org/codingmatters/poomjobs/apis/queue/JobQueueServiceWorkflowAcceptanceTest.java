package org.codingmatters.poomjobs.apis.queue;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.exception.NoSuchJobException;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.codingmatters.poomjobs.test.utils.Helpers.array;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 15/07/15.
 */
public abstract class JobQueueServiceWorkflowAcceptanceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private JobQueueService queue;
    private UUID uuid;

    protected abstract TestConfigurationProvider getConfigurationProvider();

    @Before
    public void setUp() throws Exception {
        TestConfigurationProvider config = this.getConfigurationProvider();

        this.queue = PoorMansJob.queue(config.getQueueConfig());
        this.uuid = this.queue.submit(JobSubmission.job("job").submission()).getUuid();
    }

    @Test
    public void testDoneOnStarted() throws Exception {
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.queue.start(uuid);
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.RUNNING));

        this.queue.done(uuid, "r", "e", "s");
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.DONE));
        assertThat(this.queue.get(uuid).getResults(), is(array("r", "e", "s")));
    }

    @Test
    public void testDoneFailureOnSubmitted() throws Exception {
        thrown.expect(InconsistentJobStatusException.class);
        thrown.expectMessage("cannot stop job " + uuid + " with status PENDING (should be one of [RUNNING])");
        this.queue.done(this.uuid);
    }

    @Test
    public void testDoneFailureOnNotSubmitted() throws Exception {
        UUID random = UUID.randomUUID();
        thrown.expect(NoSuchJobException.class);
        thrown.expectMessage("no such job with uuid=" + random);
        this.queue.done(random);
    }

    @Test
    public void testFailOnStarted() throws Exception {
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.queue.start(uuid);
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.RUNNING));

        this.queue.fail(uuid, "e", "r", "r");
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.FAILED));
        assertThat(this.queue.get(uuid).getErrors(), is(array("e", "r", "r")));
    }

    @Test
    public void testFailFailureOnSubmitted() throws Exception {
        thrown.expect(InconsistentJobStatusException.class);
        thrown.expectMessage("cannot fail job " + uuid + " with status PENDING (should be one of [RUNNING])");
        this.queue.fail(this.uuid);
    }

    @Test
    public void testFailFailureOnNotSubmitted() throws Exception {
        UUID random = UUID.randomUUID();
        thrown.expect(NoSuchJobException.class);
        thrown.expectMessage("no such job with uuid=" + random);
        this.queue.fail(random);
    }

    @Test
    public void testCancelOnStarted() throws Exception {
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.queue.start(uuid);
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.RUNNING));

        this.queue.cancel(uuid);
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.CANCELED));
    }

    @Test
    public void testCancelOnSubmitted() throws Exception {
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.PENDING));

        this.queue.cancel(uuid);
        assertThat(this.queue.get(uuid).getStatus(), is(JobStatus.CANCELED));
    }

    @Test
    public void testCancelFailureOnDone() throws Exception {
        this.queue.start(uuid);
        this.queue.done(uuid);

        thrown.expect(InconsistentJobStatusException.class);
        thrown.expectMessage("cannot cancel job " + uuid + " with status DONE (should be one of [RUNNING, PENDING])");

        this.queue.cancel(uuid);
    }

    @Test
    public void testCancelFailureOnNotSubmitted() throws Exception {
        UUID random = UUID.randomUUID();
        thrown.expect(NoSuchJobException.class);
        thrown.expectMessage("no such job with uuid=" + random);

        this.queue.cancel(random);
    }
}
