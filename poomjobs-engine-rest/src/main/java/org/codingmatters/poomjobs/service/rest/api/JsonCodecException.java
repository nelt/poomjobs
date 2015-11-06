package org.codingmatters.poomjobs.service.rest.api;

/**
 * Created by nel on 06/11/15.
 */
public class JsonCodecException extends Exception {
    public JsonCodecException(String msg, Exception e) {
        super(msg, e);
    }
}
