package org.codingmatters.poomjobs.service.rest.api;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobBuilders;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 06/11/15.
 */
public class JsonJobCodecTest {

    @Test
    public void testJson() throws Exception {
        Job job = JobBuilders.build("a job type")
                .withArguments("a", "b", "c")
                .job();
        JsonJobCodec codec = new JsonJobCodec();

        Job readJob = codec.readJob(codec.write(job));

        assertThat(JobBuilders.from(readJob), is(JobBuilders.from(job)));
    }
}