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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
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
    public CloseableHttpClient createClient(String user, String password)
    {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        // Pre-configure with everything configured at JVM level
        httpClientBuilder.useSystemProperties();

        HttpHost proxy = null;
        CredentialsProvider proxyCredentialsProvider = null;
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if ((proxyHost != null) && !proxyHost.isEmpty()) {
            int port = 3128;
            if (proxyPort != null && !proxyPort.isEmpty()) {
                port = Integer.parseInt(proxyPort);
            }

            proxy =  new HttpHost(proxyHost, port);
            httpClientBuilder.setProxy(proxy);
        }

        if (proxy != null) {
            String proxyUser = System.getProperty("http.proxyUser");
            if (proxyUser != null && !proxyUser.isEmpty()) {
                String proxyPassword = System.getProperty("http.proxyPassword");

                proxyCredentialsProvider = new BasicCredentialsProvider();
                proxyCredentialsProvider.setCredentials(
                        new AuthScope(proxy),
                        new UsernamePasswordCredentials(proxyUser, proxyPassword)
                );

                httpClientBuilder.setDefaultCredentialsProvider(proxyCredentialsProvider);
            }
        }

        // Setup user agent
        httpClientBuilder.setUserAgent(this.configuration.getUserAgent());

        // Setup timeout
        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(60000).build();
        connectionManager.setSocketConfig(socketConfig);
        httpClientBuilder.setConnectionManager(connectionManager);

        // Setup authentication
        if (user != null) {
            CredentialsProvider credentialsProvider;
            if (proxyCredentialsProvider != null) {
                credentialsProvider = proxyCredentialsProvider;
            } else {
                credentialsProvider = new BasicCredentialsProvider();
            }

            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        return httpClientBuilder.build();
    }
}
