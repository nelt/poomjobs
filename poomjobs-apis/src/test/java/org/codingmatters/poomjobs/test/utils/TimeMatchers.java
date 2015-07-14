package org.codingmatters.poomjobs.test.utils;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.CombinableMatcher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;

/**
 * Created by nel on 06/07/15.
 */
public class TimeMatchers {

    public static <T extends  ChronoLocalDateTime> Matcher<T> before(final T dateTime) {
        return new LocalDateTimeMatcher() {
            public void describeTo(Description description) {
                description.appendText("before ").appendValue(dateTime);
            }

            @Override
            protected boolean matchesDateTime(ChronoLocalDateTime item) {
                return item.isBefore(dateTime);
            }
        };
    }

    public static <T extends  ChronoLocalDateTime> Matcher<T> beforeOrSame(final T dateTime) {
        return new LocalDateTimeMatcher() {
            public void describeTo(Description description) {
                description.appendText("before or same ").appendValue(dateTime);
            }

            @Override
            protected boolean matchesDateTime(ChronoLocalDateTime item) {
                return item.isBefore(dateTime) || item.equals(dateTime);
            }
        };
    }

    public static <T extends  ChronoLocalDateTime> Matcher<T> after(final T dateTime) {
        return new LocalDateTimeMatcher() {
            @Override
            protected boolean matchesDateTime(ChronoLocalDateTime item) {
                return item.isAfter(dateTime);
            }

            public void describeTo(Description description) {
                description.appendText("after ").appendValue(dateTime);
            }
        };
    }

    public static <T extends  ChronoLocalDateTime> Matcher<T> afterOrSame(final T dateTime) {
        return new LocalDateTimeMatcher() {
            @Override
            protected boolean matchesDateTime(ChronoLocalDateTime item) {
                return item.isAfter(dateTime) || item.equals(dateTime);
            }

            public void describeTo(Description description) {
                description.appendText("after or same ").appendValue(dateTime);
            }
        };
    }


    static public <T extends  ChronoLocalDateTime> Matcher<T> between(final T start, final T end) {
        return new CombinableMatcher<T>(after(start)).and(before(end));
    }

    static public <T extends  ChronoLocalDateTime> Matcher<T> near(final T to, Duration precision) {
        return new CombinableMatcher<T>(afterOrSame(to.minus(precision))).and(beforeOrSame(to.plus(precision)));
    }

    static public <T extends  ChronoLocalDateTime> Matcher<T> near(final T to) {
        return near(to, Duration.ofMillis(100L));
    }

    static private abstract class LocalDateTimeMatcher<T extends  ChronoLocalDateTime> extends BaseMatcher<T> {

        protected abstract boolean matchesDateTime(T item);

        private Matcher<ChronoLocalDateTime> isaMatcher = Matchers.isA(ChronoLocalDateTime.class);

        public boolean matches(Object item) {
            return isaMatcher.matches(item) && item != null && this.matchesDateTime((T) item);
        }
    }



}
