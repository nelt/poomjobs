package org.codingmatters.poomjobs.service.rest.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobBuilders;
import org.codingmatters.poomjobs.apis.services.queue.JobSubmission;

import java.io.IOException;

/**
 * Created by nel on 06/11/15.
 */
public class JsonJobCodec {
    private final ObjectMapper mapper;

    public JsonJobCodec(ObjectMapper mapper) {
        this.mapper = mapper;
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.setVisibility(this.mapper.getVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        );
    }

    public JsonJobCodec() {
        this(new ObjectMapper());
    }

    public <T> String write(T job) throws JsonCodecException {
        try {
            return this.mapper.writeValueAsString(job);
        } catch (JsonProcessingException e) {
            throw new JsonCodecException("cannot generate JSON representation for job : " + job, e);
        }
    }

    public Job readJob(String json) throws JsonCodecException {
        try {
            JobBuilders.Builder builder = this.mapper.readValue(json, JobBuilders.Builder.class);
            return builder.job();
        } catch (IOException e) {
            throw new JsonCodecException("couldn't parse as job : " + json, e);
        }
    }

    public JobSubmission readJobSubmission(String json) throws JsonCodecException {
        try {
            JobSubmission.Builder builder = this.mapper.readValue(json, JobSubmission.Builder.class);
            return builder.submission();
        } catch (IOException e) {
            throw new JsonCodecException("couldn't parse as job : " + json, e);
        }
    }

}
