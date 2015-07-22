package org.codingmatters.poomjobs.apis.services.list;

/**
 * Created by nel on 21/07/15.
 */
public class ListQuery {

    private final Long limit;
    private final Long offset;


    private ListQuery(Long limit, Long offset) {
        this.limit = limit;
        this.offset = offset;
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

        private Builder(long limit) {
            this.limit = limit;
        }

        public Builder withOffset(long offset) {
            this.offset = offset;
            return this;
        }

        public ListQuery query() {
            return new ListQuery(this.limit, this.offset);
        }
    }
}
