package org.codingmatters.poomjobs.http.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nel on 12/11/15.
 */
public interface SendingHandler {
    Logger log = LoggerFactory.getLogger(SendingHandler.class);

    SendingHandler NOOP = sending -> log.warn("sending handler not defined, couldn't send {}", sending.getEvent());

    void sendRequested(ServerSentEventSender sending);
}
