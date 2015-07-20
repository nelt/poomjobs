package org.codingmatters.poomjobs.apis.dispatch;

import org.codingmatters.poomjobs.apis.PoorMansJob;
import org.codingmatters.poomjobs.apis.TestConfigurationProvider;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.jobs.exception.InconsistentJobStatusException;
import org.codingmatters.poomjobs.apis.services.dispatch.JobDispatcherService;
import org.codingmatters.poomjobs.apis.services.dispatch.JobRunner;
import org.codingmatters.poomjobs.apis.services.queue.JobQueueService;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;
import org.codingmatters.poomjobs.apis.services.queue.NoSuchJobException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.codingmatters.poomjobs.apis.jobs.JobStatus.DONE;
import static org.codingmatters.poomjobs.apis.jobs.JobStatus.PENDING;
import static org.codingmatters.poomjobs.apis.services.queue.JobSubmission.job;
import static org.codingmatters.poomjobs.test.utils.Helpers.list;
import static org.codingmatters.poomjobs.test.utils.Helpers.waitUntil;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 16/07/15.
 */
public abstract class JobDispatcherServiceAcceptanceTest {

    protected abstract TestConfigurationProvider getConfigurationProvider();

    private JobQueueService queue;
    private JobDispatcherService dispatcher;
    private List<String> executed;

    @Before
    public void setUp() throws Exception {
        TestConfigurationProvider config = this.getConfigurationProvider();
        config.initialize();

        this.queue = PoorMansJob.queue(config.getQueueConfig());
        this.dispatcher = PoorMansJob.dispatcher(config.getDispatcherConfig());

        this.executed = Collections.synchronizedList(new LinkedList<String>());
    }

    @Test
    public void testRegisterWhenAlreadySubmitted() throws Exception {
        UUID uuid = this.queue.submit(job("job").submission()).getUuid();

        this.dispatcher.register(this.jobRunner("runner"), "job");

        Thread.sleep(200);
        assertThat(this.executed, is(list("runner/" + uuid.toString())));
    }

    @Test
    public void testRegisterThenSubmission() throws Exception {
        this.dispatcher.register(this.jobRunner("runner"), "job");

        UUID uuid = this.queue.submit(job("job").submission()).getUuid();

        Thread.sleep(200);
        assertThat(this.executed, is(list("runner/" + uuid.toString())));
    }

    @Test
    public void testRunOnlyOnce() throws Exception {
        this.dispatcher.register(this.jobRunner("runner1"), "job");
        this.dispatcher.register(this.jobRunner("runner2"), "job");

        UUID uuid = this.queue.submit(job("job").submission()).getUuid();

        Thread.sleep(200);
        assertThat(this.executed, hasSize(1));
    }


    @Test
    public void testRegisterThenSubmitManyJobs() throws Exception {
        this.dispatcher.register(this.jobRunner("runner"), "job");
        for(int i = 0 ; i < 20 ; i++) {
            this.queue.submit(job("job").submission()).getUuid();
        }
        waitUntil(() -> this.executed.size() == 20, 10 * 1000L);
        assertThat(this.executed, hasSize(20));
    }

    @Test
    public void testExecuteOnlyRegisteredJobType() throws Exception {
        this.dispatcher.register(this.jobRunner("runner"), "job1", "job2");

        UUID uuid = this.queue.submit(job("job1").submission()).getUuid();
        waitUntil(() -> this.executed.size() == 1, 500L);
        assertThat(this.queue.get(uuid).getStatus(), is(DONE));

        uuid = this.queue.submit(job("job2").submission()).getUuid();
        waitUntil(() -> this.executed.size() == 2, 500L);
        assertThat(this.queue.get(uuid).getStatus(), is(DONE));

        uuid = this.queue.submit(job("job3").submission()).getUuid();
        Thread.sleep(500L);
        assertThat(this.queue.get(uuid).getStatus(), is(PENDING));
    }

    @Test
    public void testRoundRobinExecution() throws Exception {
        this.dispatcher.register(this.jobRunner("runner1"), "job");
        this.dispatcher.register(this.jobRunner("runner2"), "job");

        this.queue.submit(job("job").submission());
        waitUntil(() -> this.executed.size() == 1, 500L);
        assertThat(this.executed.get(0), startsWith("runner1/"));

        this.queue.submit(job("job").submission());
        waitUntil(() -> this.executed.size() == 2, 500L);
        assertThat(this.executed.get(1), startsWith("runner2/"));
    }



    protected JobRunner jobRunner(String name) {
        return (job) -> {
            try {
                this.queue.done(job.getUuid());
                this.executed.add(name + "/" + job.getUuid().toString());
            } catch (NoSuchJobException | InconsistentJobStatusException e) {
                e.printStackTrace();
            }
        };
    }

}
