package org.codingmatters.poomjobs.http;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 04/11/15.
 */
public class RestServiceTest {

    @Test
    public void testGetMatchingResource() throws Exception {
        RestService service = RestService.root("/service").resource("/resource", RestResource.resource());
        assertThat(service.getMatchingResource("/resource"), is(not(RestResource.notFound())));
    }

    @Test
    public void testGetMatchingResourceWithPathParameters() throws Exception {
        RestService service = RestService.root("/service").resource("/{p1}/{p2}", RestResource.resource());
        assertThat(service.getMatchingResource("/v1/v2"), is(not(RestResource.notFound())));
    }
}