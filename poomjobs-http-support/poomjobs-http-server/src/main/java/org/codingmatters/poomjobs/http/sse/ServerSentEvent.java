package org.codingmatters.poomjobs.http.sse;

/**
 * Created by nel on 12/11/15.
 */
public class ServerSentEvent {
    private final String data;
    private final String event;
    private final String id;

    public ServerSentEvent(String data, String event, String id) {
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
