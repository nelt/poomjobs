package org.codingmatters.poomjobs.http.sse;

import java.util.UUID;

/**
 * Created by nel on 12/11/15.
 */
public class ServerSentEvent {

    static public Builder data(String data) {
        return new Builder().withData(data);
    }

    static public class Builder {
        private String data;
        private String event;
        private String id = UUID.randomUUID().toString();

        private Builder() {}

        public Builder withData(String data) {
            this.data = data;
            return this;
        }

        public Builder withEvent(String event) {
            this.event = event;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public ServerSentEvent event() {
            return new ServerSentEvent(this.data, this.event, this.id);
        }
    }

    private final String data;
    private final String event;
    private final String id;

    private ServerSentEvent(String data, String event, String id) {
        this.data = data;
        this.event = event;
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public String getEvent() {
        return event;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ServerSentEvent{" +
                "data='" + data + '\'' +
                ", event='" + event + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
