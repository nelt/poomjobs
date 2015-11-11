package org.codingmatters.poomjobs.service.rest.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobBuilders;
import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.codingmatters.poomjobs.apis.services.list.ListQuery;
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

    public <T> String write(T element) throws JsonCodecException {
        try {
            return this.mapper.writeValueAsString(element);
        } catch (JsonProcessingException e) {
            throw new JsonCodecException("cannot generate JSON representation for job : " + element, e);
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
            throw new JsonCodecException("couldn't parse as job submission : " + json, e);
        }
    }

    public String[] readArray(String json) throws JsonCodecException {
        try {
            return this.mapper.readValue(json, String[].class);
        } catch (IOException e) {
            throw new JsonCodecException("couldn't parse as array : " + json, e);
        }
    }

    public JobList readJobList(String json) throws JsonCodecException {
        try {
            JobBuilders.Builder[] builders = this.mapper.readValue(json, JobBuilders.Builder[].class);
            RestJobList result = new RestJobList();
            for (JobBuilders.Builder builder : builders) {
                result.add(builder.job());
            }

            return result;
        } catch (IOException e) {
            throw new JsonCodecException("couldn't parse as job list : " + json, e);
        }
    }

    public ListQuery readListQuery(String json) throws IOException {
        return this.mapper.readValue(json, ListQuery.Builder.class).query();
    }
}
