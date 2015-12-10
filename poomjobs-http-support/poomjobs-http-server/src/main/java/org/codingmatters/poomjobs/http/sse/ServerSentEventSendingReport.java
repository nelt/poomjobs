package org.codingmatters.poomjobs.http.sse;

/**
 * Created by nel on 09/12/15.
 */
public class ServerSentEventSendingReport {

    private final long successCount;
    private final long failureCount;

    public ServerSentEventSendingReport(long successCount, long failureCount) {
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    @Override
    public String toString() {
        return "ServerSentEventSendingReport{" +
                "successCount=" + successCount +
                ", failureCount=" + failureCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerSentEventSendingReport that = (ServerSentEventSendingReport) o;

        if (successCount != that.successCount) return false;
        return failureCount == that.failureCount;

    }

    @Override
    public int hashCode() {
        int result = (int) (successCount ^ (successCount >>> 32));
        result = 31 * result + (int) (failureCount ^ (failureCount >>> 32));
        return result;
    }
}
