package org.codingmatters.poomjobs.engine.inmemory.impl.monitor;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobBuilders;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor;
import org.codingmatters.poomjobs.apis.services.monitoring.StatusChangedMonitor.Weak;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 16/07/15.
 */
public class InMemoryStatusMonitorerTest {

    private StatusMonitorer group;
    private Job job;
    private AtomicInteger counter;

    @Before
    public void setUp() throws Exception {
        this.group = new InMemoryStatusMonitorer();
        this.job = JobBuilders.build("job").withStatus(JobStatus.RUNNING).job();

        this.counter = new AtomicInteger(0);
    }

    @Test
    public void testNotWeak() throws Exception {
        StatusChangedMonitor monitor = (job, old) -> {
            this.counter.incrementAndGet();
        };
        this.group.monitor(this.job.getUuid(), monitor);
        this.group.changed(this.job, this.job.getStatus());

        assertThat(this.group.monitorCount(), is(1));
        assertThat(this.counter.get(), is(1));

        monitor = null;
        System.gc();

        this.group.changed(this.job, this.job.getStatus());

        assertThat(this.group.monitorCount(), is(1));
        assertThat(this.counter.get(), is(2));
    }

    @Test
    public void testWeak() throws Exception {
        StatusChangedMonitor monitor = (job, old) -> {
            this.counter.incrementAndGet();
        };
        this.group.monitor(job.getUuid(), Weak.monitor(monitor));
        assertThat(this.group.monitorCount(), is(1));

        this.group.changed(this.job, this.job.getStatus());
        assertThat(this.counter.get(), is(1));

        monitor = null;
        System.gc();

        this.group.changed(this.job, this.job.getStatus());

        assertThat(this.group.monitorCount(), is(0));
        assertThat(this.counter.get(), is(1));
    }

    @Test
    public void testIndirectWeak() throws Exception {
        HashMap<String, StatusChangedMonitor> holder = new HashMap<>();
        holder.put("monitor", ((job1, old) -> {this.counter.incrementAndGet();}));
        this.group.monitor(job.getUuid(), Weak.monitor(holder.get("monitor")));
        assertThat(this.group.monitorCount(), is(1));

        this.group.changed(this.job, this.job.getStatus());
        assertThat(this.counter.get(), is(1));

        holder = null;
        System.gc();

        this.group.changed(this.job, this.job.getStatus());

        assertThat(this.group.monitorCount(), is(0));
        assertThat(this.counter.get(), is(1));
    }
}