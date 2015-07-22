package org.codingmatters.poomjobs.apis.list;

import org.codingmatters.poomjobs.apis.jobs.JobList;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.UUID;

/**
 * Created by nel on 21/07/15.
 */
public class JobListMatchers {

    static public Matcher<JobList> exactlyUUIDS(final UUID... uuids) {
        return  new BaseMatcher<JobList>() {
            @Override
            public void describeTo(Description description) {
                description.appendValueList("job list with uuids ", ", ", "", uuids);
            }

            @Override
            public boolean matches(Object item) {
                if(! (item instanceof JobList)) return false;
                JobList list = (JobList) item;

                if(uuids == null) {
                    return list == null || list.isEmpty();
                }

                if(list == null) {
                    return uuids == null || uuids.length == 0;
                }

                if(list.size() != uuids.length) return false;

                for(int i = 0 ; i < uuids.length ; i++) {
                    if(! list.get(i).getUuid().equals(uuids[i])) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

}
