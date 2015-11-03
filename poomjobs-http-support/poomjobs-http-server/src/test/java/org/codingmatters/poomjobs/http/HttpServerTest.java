package org.codingmatters.poomjobs.http;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nel on 28/10/15.
 */
public class HttpServerTest {

    static private Logger log = LoggerFactory.getLogger(HttpServerTest.class);

    private Undertow server;

    @Before
    public void setUp() throws Exception {
        this.server = Undertow.builder()
                .addHttpListener(9999, "localhost")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        log.debug("HANDLE");
                        if (exchange.isInIoThread()) {
                            exchange.dispatch(this);
                            return;
                        }
                        if("/hello".equals(exchange.getRequestPath())) {
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain; charset=UTF-8");
                            exchange.getResponseSender().send("Hello World");
                        } else if("/go-fast".equals(exchange.getRequestPath())) {
                            if("POST".equals(exchange.getRequestMethod().toString())) {
                                exchange.setStatusCode(303);
                                exchange.getResponseHeaders().put(Headers.LOCATION, "/hello");
                            } else {
                                exchange.setStatusCode(405);
                                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain; charset=UTF-8");
                                exchange.getResponseSender().send("method not allowed on resource");
                            }
                        } else {
                            exchange.setStatusCode(404);
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain; charset=UTF-8");  //Response Headers
                            exchange.getResponseSender().send("resource not found");
                        }
                    }
                })

                .build();


        this.server.start();
    }

    @After
    public void tearDown() throws Exception {
        this.server.stop();
    }

    @Test
    public void testNotFound() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        ContentResponse response = httpClient.GET("http://localhost:9999/not/found");
        assertThat(response.getStatus(), is(404));
        assertThat(response.getContentAsString(), is("resource not found"));
        assertThat(response.getMediaType(), is("text/plain"));
        assertThat(response.getEncoding(), is("UTF-8"));
    }

    @Test
    public void testHello() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        ContentResponse response = httpClient.GET("http://localhost:9999/hello");
        assertThat(response.getContentAsString(), is("Hello World"));
        assertThat(response.getMediaType(), is("text/plain"));
        assertThat(response.getEncoding(), is("UTF-8"));
    }

    @Test
    public void testPostRedirectGet() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        ContentResponse response = httpClient.POST("http://localhost:9999/go-fast")
                .param("where", "hello")
                .send();
        assertThat(response.getContentAsString(), is("Hello World"));
        assertThat(response.getMediaType(), is("text/plain"));
        assertThat(response.getEncoding(), is("UTF-8"));
    }


    @Test
    public void testPostRedirect() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.setFollowRedirects(false);
        httpClient.start();

        ContentResponse response = httpClient.POST("http://localhost:9999/go-fast")
                .param("where", "hello")
                .send();
        assertThat(response.getStatus(), is(303));
        assertThat(response.getHeaders().get("Location"), is("/hello"));
    }

    @Test
    public void testWrongMethod() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        ContentResponse response = httpClient.GET("http://localhost:9999/go-fast");

        assertThat(response.getStatus(), is(405));
        assertThat(response.getContentAsString(), is("method not allowed on resource"));
        assertThat(response.getMediaType(), is("text/plain"));
        assertThat(response.getEncoding(), is("UTF-8"));
    }


}
