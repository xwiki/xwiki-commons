/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.http.internal;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

/**
 * Encapsulate an Apache HTTP Client 5 instance and add some helpers.
 * 
 * @version $Id$
 * @since 17.10.0RC1
 * @since 17.4.8
 * @since 16.10.15
 */
public class XWikiHTTPClient implements Closeable
{
    private static final String DEFAULT_USER_AGENT = "XWiki";

    private final CloseableHttpClient client;

    /**
     * The cookies shared by all the requests executed by this client, so that the state associated with them on the
     * server side (an authenticated session, for example) is kept between requests.
     */
    private final BasicCookieStore cookieStore = new BasicCookieStore();

    private XWikiCredentials defaultCredentials;

    /**
     * Create the client.
     */
    public XWikiHTTPClient()
    {
        this(builder());
    }

    /**
     * Create the client.
     * 
     * @param userAgent the user agent to use
     * @param timeout the socket and connection timeout
     */
    public XWikiHTTPClient(String userAgent, int timeout)
    {
        this(builder(userAgent, timeout));
    }

    /**
     * @param builder the builder to use
     */
    public XWikiHTTPClient(HttpClientBuilder builder)
    {
        this.client = builder.build();
    }

    /**
     * Create the client.
     * 
     * @param client the client
     */
    public XWikiHTTPClient(CloseableHttpClient client)
    {
        this.client = client;
    }

    /**
     * @return a pre configured {@link HttpClientBuilder}
     */
    public static HttpClientBuilder builder()
    {
        return builder(DEFAULT_USER_AGENT);
    }

    /**
     * @param userAgent a custom user agent
     * @return a pre configured {@link HttpClientBuilder}
     */
    public static HttpClientBuilder builder(String userAgent)
    {
        return HttpClients.custom().useSystemProperties().setUserAgent(userAgent);
    }

    /**
     * @param userAgent a custom user agent
     * @param timeout a custom timeout
     * @return a pre configured {@link HttpClientBuilder}
     */
    public static HttpClientBuilder builder(String userAgent, int timeout)
    {
        ConnectionConfig connConfig = ConnectionConfig.custom().setConnectTimeout(timeout, TimeUnit.MILLISECONDS)
            .setSocketTimeout(timeout, TimeUnit.MILLISECONDS).build();

        BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        cm.setConnectionConfig(connConfig);

        return builder(userAgent).setConnectionManager(cm);
    }

    /**
     * Sets the credentials to use for the requests that don't specify any. The cookies are cleared along with them,
     * see {@link #clearCookies()}: the server keeps the authenticated user in the session associated with the cookies
     * of this client and that session wins over the credentials passed with the request, so the following requests
     * would otherwise keep being executed as the previously authenticated user.
     *
     * @param defaultCredentials the default credentials to use for requests
     */
    public void setDefaultCredentials(XWikiCredentials defaultCredentials)
    {
        this.defaultCredentials = defaultCredentials;

        clearCookies();
    }

    /**
     * @return the default credentials to use for requests
     */
    public XWikiCredentials getDefaultCredentials()
    {
        return this.defaultCredentials;
    }

    /**
     * Removes all the cookies currently stored by this client, and thus the state the server associated with them, for
     * example an authenticated session. Useful when reusing the same client for requests that should not share any
     * state, typically requests executed on behalf of different users.
     *
     * @since 18.8.0RC1
     */
    public void clearCookies()
    {
        this.cookieStore.clear();
    }

    /**
     * @return the client
     */
    public CloseableHttpClient getClient()
    {
        return this.client;
    }

    @Override
    public void close() throws IOException
    {
        this.client.close();
    }

    /**
     * Executes a request using the default context and processes the response using the given response handler. The
     * content entity associated with the response is fully consumed and the underlying connection is released back to
     * the connection manager automatically in all cases relieving individual {@link HttpClientResponseHandler}s from
     * having to manage resource deallocation internally.
     *
     * @param <T> the type of the result determined by the response handler
     * @param request the request to execute
     * @param responseHandler the response handler
     * @return the response object as generated by the response handler.
     * @throws IOException in case of a problem or the connection was aborted
     * @throws ClientProtocolException in case of an http protocol error
     */
    public <T> T execute(ClassicHttpRequest request, XWikiHTTPClientResponseHandler<? extends T> responseHandler)
        throws IOException
    {
        return execute(request, null, responseHandler);
    }

    /**
     * Executes a request using the default context and processes the response using the given response handler. The
     * content entity associated with the response is fully consumed and the underlying connection is released back to
     * the connection manager automatically in all cases relieving individual {@link HttpClientResponseHandler}s from
     * having to manage resource deallocation internally.
     *
     * @param <T> the type of the result determined by the response handler
     * @param request the request to execute
     * @param credentials the credentials to use for the request
     * @param responseHandler the response handler
     * @return the response object as generated by the response handler.
     * @throws IOException in case of a problem or the connection was aborted
     * @throws ClientProtocolException in case of an http protocol error
     */
    public <T> T execute(ClassicHttpRequest request, XWikiCredentials credentials,
        XWikiHTTPClientResponseHandler<? extends T> responseHandler) throws IOException
    {
        HttpClientContext context = getHttpClientContext(request, credentials);

        return this.client.execute(request, context, response -> responseHandler.handleResponse(response, context));
    }

    /**
     * @param request the request to get the context for
     * @param credentials the credentials to use for the request
     * @return the context to use for the request, holding the cookies of this client and, when there are credentials,
     *         the authentication to use
     * @throws IOException when failing to get the URI of the request
     */
    public HttpClientContext getHttpClientContext(HttpRequest request, XWikiCredentials credentials) throws IOException
    {
        // Always pass the cookies of this client, so that they are shared by all the requests whether they are
        // authenticated or not, and so that they can be cleared with #clearCookies().
        ContextBuilder contextBuilder = ContextBuilder.create().useCookieStore(this.cookieStore);

        XWikiCredentials finalCredentials = credentials != null ? credentials : this.defaultCredentials;
        if (finalCredentials != null) {
            URI uri;
            try {
                uri = request.getUri();
            } catch (URISyntaxException e) {
                // Should fail before arriving here
                throw new IOException("Cannot get the URI of the request", e);
            }

            contextBuilder.preemptiveBasicAuth(new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort()),
                new UsernamePasswordCredentials(finalCredentials.getUserName(),
                    finalCredentials.getPassword().toCharArray()));
        }

        return contextBuilder.build();
    }

    /**
     * @param <T> the type of the result determined by the response handler
     * @param uri the URI to get
     * @param responseHandler the response handler
     * @return the response object as generated by the response handler.
     * @throws IOException in case of a problem or the connection was aborted
     */
    public <T> T executeGet(String uri, XWikiHTTPClientResponseHandler<? extends T> responseHandler) throws IOException
    {
        return execute(new HttpGet(uri), responseHandler);
    }

    /**
     * @param <T> the type of the result determined by the response handler
     * @param uri the URI to get
     * @param entity the entity to put
     * @param responseHandler the response handler
     * @return the response object as generated by the response handler.
     * @throws IOException in case of a problem or the connection was aborted
     */
    public <T> T executePut(String uri, HttpEntity entity, XWikiHTTPClientResponseHandler<? extends T> responseHandler)
        throws IOException
    {
        ClassicHttpRequest request = new HttpPut(uri);
        request.setEntity(entity);

        return execute(request, responseHandler);
    }
}
