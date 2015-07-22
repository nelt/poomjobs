package org.codingmatters.poomjobs.apis.services.list;

import org.codingmatters.poomjobs.apis.jobs.JobStatus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nel on 21/07/15.
 */
public class ListQuery {

    private final Long limit;
    private final Long offset;
    private final Set<JobStatus> statuses;


    private ListQuery(Long limit, Long offset, Set<JobStatus> statuses) {
        this.limit = limit;
        this.offset = offset;
        this.statuses = statuses;
    }

    static public Builder limit(long limit) {
        return new Builder(limit);
    }

    public Long getLimit() {
        return limit;
    }

    public Long getOffset() {
        return offset;
    }

    public Set<JobStatus> getStatuses() {
        return this.statuses;
    }

    @Override
    public String toString() {
        return "ListQuery{" +
                "limit=" + limit +
                ", offset=" + offset +
                '}';
    }

    static public class Builder {
        private final Long limit;
        private Long offset = 0L;
        private HashSet<JobStatus> statuses = new HashSet<>();

        private Builder(long limit) {
            this.limit = limit;
        }

        public Builder withOffset(long offset) {
            this.offset = offset;
            return this;
        }

        public Builder status(JobStatus status) {
            this.statuses.add(status);
            return this;
        }

        public ListQuery query() {
            if(this.statuses.isEmpty()) {
                this.statuses.addAll(Arrays.asList(JobStatus.values()));
            }
            return new ListQuery(this.limit, this.offset, new HashSet<>(this.statuses));
        }
    }
}
