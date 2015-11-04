package org.codingmatters.poomjobs.http;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static org.codingmatters.poomjobs.http.RestService.root;
import static org.codingmatters.poomjobs.http.RestServiceHandler.from;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 04/11/15.
 */
public class RestParametersTest {

    static private Logger log = LoggerFactory.getLogger(HttpServerTest.class);

    @Rule
    public TestUndertowServer server = new TestUndertowServer();
    private HttpClient httpClient;

    @Before
    public void setUp() throws Exception {
        this.httpClient = new HttpClient();
        this.httpClient.start();
    }

    @Test
    public void testGetParameter() throws Exception {
        this.server.setHandler(from(root("/service")
                .resource("/named", RestService
                                .resource().GET(io -> {
                                    assertThat(io.parameters().get("name"), is(asList("value")));
                                })
                )));

        ContentResponse response = this.httpClient.GET(this.server.url("/service/named?name=value"));
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void testGetPath() throws Exception {
        this.server.setHandler(from(root("/service")
                .resource("/{level1}/{level2}/{level1}", RestService
                                .resource().GET(io -> {
                                    assertThat(io.pathParameters().get("level1"), is(asList("v1", "v3")));
                                    assertThat(io.pathParameters().get("level2"), is(asList("v2")));
                                })
                )));

        ContentResponse response = this.httpClient.GET(this.server.url("/service/v1/v2/v3"));
        assertThat(response.getStatus(), is(200));
    }
}
