package org.codingmatters.poomjobs.apis.services.monitoring;

import org.codingmatters.poomjobs.apis.jobs.Job;
import org.codingmatters.poomjobs.apis.jobs.JobStatus;

import java.lang.ref.WeakReference;

/**
 * Created by nel on 16/07/15.
 */
public interface StatusChangedMonitor {

    class Weak implements StatusChangedMonitor {
        static public StatusChangedMonitor monitor(StatusChangedMonitor monitor) {
            return new Weak(monitor);
        }

        private final WeakReference<StatusChangedMonitor> deleguate;

        private Weak(StatusChangedMonitor monitor) {
            this.deleguate = new WeakReference<>(monitor);
        }

        @Override
        public void statusChanged(Job job, JobStatus old) {
            StatusChangedMonitor monitor = this.deleguate.get();
            if(monitor != null) {
                monitor.statusChanged(job, old);
            }
        }

        public boolean isValid() {
            return this.deleguate.get() != null;
        }
    }

    void statusChanged(Job job, JobStatus old);
}
