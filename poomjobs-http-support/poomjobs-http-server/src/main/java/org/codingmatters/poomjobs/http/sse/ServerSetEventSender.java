package org.codingmatters.poomjobs.http.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nel on 12/11/15.
 */
public interface ServerSetEventSender {
    Logger log = LoggerFactory.getLogger(ServerSetEventSender.class);

    ServerSetEventSender NOOP = sending -> {
        log.warn("sending handler not defined, couldn't send {}", sending.getEvent());
        return new ServerSentEventFuture(0);
    };

    ServerSentEventFuture sendRequested(ServerSentEventSender sending);
}
