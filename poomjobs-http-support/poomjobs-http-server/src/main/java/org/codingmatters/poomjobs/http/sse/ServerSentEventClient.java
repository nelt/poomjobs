package org.codingmatters.poomjobs.http.sse;

import org.codingmatters.poomjobs.http.RestInput;

/**
 * Created by nel on 12/11/15.
 */
public interface ServerSentEventClient {
    RestInput connectionInput();
    String uuid();
}
