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
        return new Builder().withLimit(limit);
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
                ", statuses=" + statuses +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListQuery listQuery = (ListQuery) o;

        if (limit != null ? !limit.equals(listQuery.limit) : listQuery.limit != null) return false;
        if (offset != null ? !offset.equals(listQuery.offset) : listQuery.offset != null) return false;
        return !(statuses != null ? !statuses.equals(listQuery.statuses) : listQuery.statuses != null);

    }

    @Override
    public int hashCode() {
        int result = limit != null ? limit.hashCode() : 0;
        result = 31 * result + (offset != null ? offset.hashCode() : 0);
        result = 31 * result + (statuses != null ? statuses.hashCode() : 0);
        return result;
    }

    public static ListQuery query() {
        return new Builder().query();
    }

    static public class Builder {
        private Long limit = 100L;
        private Long offset = 0L;
        private HashSet<JobStatus> statuses = new HashSet<>();

        private Builder() {
        }

        public Builder withLimit(Long limit) {
            this.limit = limit;
            return this;
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
