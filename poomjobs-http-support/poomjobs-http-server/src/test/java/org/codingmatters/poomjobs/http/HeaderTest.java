package org.codingmatters.poomjobs.http;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.codingmatters.poomjobs.http.RestService.root;
import static org.codingmatters.poomjobs.http.RestServiceHandler.from;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 06/11/15.
 */
public class HeaderTest {

    @Rule
    public TestUndertowServer server = new TestUndertowServer();
    private HttpClient httpClient;

    @Before
    public void setUp() throws Exception {
        this.httpClient = new HttpClient();
        this.httpClient.start();
    }

    @Test
    public void testStringHeader() throws Exception {
        this.server.setHandler(from(root("/service")
                .resource("/named", RestService
                        .resource().GET(io -> {
                            io.header("Bla", "blu");
                        })
                )));

        ContentResponse response = this.httpClient.GET(this.server.url("/service/named?name=value"));
        assertThat(response.getStatus(), is(200));

        assertThat(response.getHeaders().get("Bla"), is("blu"));
    }
}
