package org.codingmatters.poomjobs.http;

import io.undertow.Handlers;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 02/11/15.
 */
public class RestServiceTest {

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
    public void testServiceNotFound() throws Exception {
        this.server.setHandler(RestServiceHandler.from(RestService.root("/service")));

        ContentResponse response = this.httpClient.GET(this.server.url("/no/service"));
        assertThat(response.getStatus(), is(404));
        assertThat(response.getContentAsString(), is("Service Not Found"));
        assertThat(response.getMediaType(), is("text/plain"));
    }

    @Test
    public void testResourceNotFound() throws Exception {
        this.server.setHandler(RestServiceHandler.from(RestService.root("/service")));

        ContentResponse response = this.httpClient.GET(this.server.url("/service/resource"));
        assertThat(response.getStatus(), is(404));
        assertThat(response.getContentAsString(), is("Resource Not Found"));
        assertThat(response.getMediaType(), is("text/plain"));
    }

    @Test
    public void testGet() throws Exception {
        this.server.setHandler(RestServiceHandler.from(RestService.root("/service")
                .resource("/named", RestService
                                .resource().GET(io ->
                                                io.status(RestStatus.OK)
                                                        .contentType("text/plain")
                                                        .encoding("UTF-8")
                                                        .content("Hello World")
                                )
                )));

        ContentResponse response = this.httpClient.GET(this.server.url("/service/named"));
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContentAsString(), is("Hello World"));
        assertThat(response.getMediaType(), is("text/plain"));
    }

    @Test
    public void testAllMethods() throws Exception {
        this.server.setHandler(RestServiceHandler.from(RestService.root("/service")
                .resource("/named", RestService.resource()
                                .GET(io -> io.content("GET"))
                                .POST(io -> io.content("POST"))
                                .PUT(io -> io.content("PUT"))
                                .DELETE(io -> io.content("DELETE"))
                )));

        assertThat(this.httpClient.GET(this.server.url("/service/named")).getContentAsString(), is("GET"));
        assertThat(this.httpClient.POST(this.server.url("/service/named")).send().getContentAsString(), is("POST"));
        assertThat(this.httpClient.newRequest(this.server.url("/service/named")).method("PUT").send().getContentAsString(), is("PUT"));
        assertThat(this.httpClient.newRequest(this.server.url("/service/named")).method("DELETE").send().getContentAsString(), is("DELETE"));
    }

    @Test
    public void testPostNotDefined() throws Exception {
        this.server.setHandler(RestServiceHandler.from(RestService.root("/service")
                .resource("/named", RestService
                                .resource().GET(io ->
                                                io.status(RestStatus.OK)
                                                        .contentType("text/plain")
                                                        .encoding("UTF-8")
                                                        .content("Hello World")
                                )
                )));

        ContentResponse response = this.httpClient.POST(this.server.url("/service/named")).send();
        assertThat(response.getStatus(), is(405));
        assertThat(response.getContentAsString(), is("Method Not Allowed for Resource"));
        assertThat(response.getMediaType(), is("text/plain"));
    }

    @Test
    public void testOptionsNeverAllowed() throws Exception {
        this.server.setHandler(RestServiceHandler.from(RestService.root("/service")
                .resource("/named", RestService
                                .resource().GET(io ->
                                                io.status(RestStatus.OK)
                                                        .contentType("text/plain")
                                                        .encoding("UTF-8")
                                                        .content("Hello World")
                                )
                )));

        ContentResponse response = this.httpClient.newRequest(this.server.url("/service/named")).method("OPTIONS").send();
        assertThat(response.getStatus(), is(405));
        assertThat(response.getContentAsString(), is("Method Not Allowed for Resource"));
        assertThat(response.getMediaType(), is("text/plain"));
    }

    @Test
    public void testHeadNeverAllowed() throws Exception {
        this.server.setHandler(RestServiceHandler.from(RestService.root("/service")
                .resource("/named", RestService
                                .resource().GET(io ->
                                                io.status(RestStatus.OK)
                                                        .contentType("text/plain")
                                                        .encoding("UTF-8")
                                                        .content("Hello World")
                                )
                )));

        ContentResponse response = this.httpClient.newRequest(this.server.url("/service/named")).method("HEAD").send();
        assertThat(response.getStatus(), is(405));
        assertThat(response.getContentAsString(), is(""));
        assertThat(response.getMediaType(), is("text/plain"));
    }

    @Test
    public void testSubpathGet() throws Exception {
        this.server.setHandler(Handlers.path()
                .addPrefixPath("/root",
                    RestServiceHandler.from(RestService.root("/service")
                        .resource("/named", RestService
                                        .resource().GET(io ->
                                                        io.status(RestStatus.OK)
                                                                .contentType("text/plain")
                                                                .encoding("UTF-8")
                                                                .content("Hello World")
                                        )
                        )
                    )
                )
        );

        ContentResponse response = this.httpClient.GET(this.server.url("/root/service/named"));
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContentAsString(), is("Hello World"));
        assertThat(response.getMediaType(), is("text/plain"));
    }



}
