package org.codingmatters.poomjobs.test.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nel on 08/07/15.
 */
public class Helpers {
    static public <T> T[] array(T ... elements) {
        return elements;
    }

    static public <T> List<T> list(T ... elements) {
        if(elements == null) return new ArrayList<>();
        return Arrays.asList(elements);
    }
}
