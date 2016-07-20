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
package org.xwiki.extension.repository.http.internal;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionManagerConfiguration;

/**
 * Configures user agent, timeouts, proxy and authentication.
 *
 * @version $Id$
 * @since 5.2M1
 */
@Component
@Singleton
public class DefaultHttpClientFactory implements HttpClientFactory
{
    /**
     * Used to get the user agent to use when performing HTTP calls to the remote Extension Repository.
     */
    @Inject
    private ExtensionManagerConfiguration configuration;

    @Override
    public HttpClientBuilder createHttpClientBuilder(String user, String password)
    {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        // Pre-configure with everything configured at JVM level
        httpClientBuilder.useSystemProperties();

        // Setup user agent
        httpClientBuilder.setUserAgent(this.configuration.getUserAgent());

        // Setup authentication
        if (user != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        return httpClientBuilder;
    }

    @Override
    public CloseableHttpClient createClient(String user, String password)
    {
        return createHttpClientBuilder(user, password).build();
    }

    @Override
    public HttpClientBuilder createHttpClientBuilder(Map<String, String> properties)
    {
        HttpClientBuilder httpClientBuilder =
            createHttpClientBuilder(properties.get("auth.user"), properties.get("auth.password"));

        // Set socket timeouts
        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
        SocketConfig.Builder socketConfigBuilder = SocketConfig.custom();
        socketConfigBuilder.setSoTimeout(getIntProperty(properties, SOCKET_TIMEOUT, 30000));
        connectionManager.setSocketConfig(socketConfigBuilder.build());
        httpClientBuilder.setConnectionManager(connectionManager);

        // Set request timeouts
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder = requestBuilder.setConnectTimeout(getIntProperty(properties, CONNECTION_TIMEOUT, 30000));
        httpClientBuilder.setDefaultRequestConfig(requestBuilder.build());

        return httpClientBuilder;
    }

    private int getIntProperty(Map<String, String> properties, String key, int def)
    {
        return NumberUtils.toInt(properties.get(key), def);
    }
}
