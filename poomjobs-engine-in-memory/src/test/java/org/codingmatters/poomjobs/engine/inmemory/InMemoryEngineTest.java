package org.codingmatters.poomjobs.engine.inmemory;

import org.junit.Test;

import java.lang.ref.WeakReference;

import static org.codingmatters.poomjobs.engine.inmemory.InMemoryEngine.getEngine;
import static org.codingmatters.poomjobs.engine.inmemory.InMemoryServiceFactory.defaults;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

/**
 * Created by nel on 15/07/15.
 */
public class InMemoryEngineTest {

    @Test
    public void testUniqueByUrl() throws Exception {

        assertThat(getEngine(defaults("T1").config()), is(getEngine(defaults("T1").config())));
        assertThat(getEngine(defaults("T1").config()), is(not(getEngine(defaults("T2").config()))));

    }

    @Test
    public void testGarbageCollectionWhenUnreferenced() throws Exception {
        InMemoryEngine engine = getEngine(defaults("test").config());
        WeakReference<InMemoryEngine> weakReference = new WeakReference<InMemoryEngine>(engine);

        System.gc();
        assertThat(weakReference.get(), is(not(nullValue())));

        engine = null;
        System.gc();
        System.runFinalization();

        assertThat(weakReference.get(), is(not(nullValue())));

        InMemoryEngine.removeEngine(defaults("test").config());

        System.gc();
        System.runFinalization();

        assertThat(weakReference.get(), is(nullValue()));
    }
}