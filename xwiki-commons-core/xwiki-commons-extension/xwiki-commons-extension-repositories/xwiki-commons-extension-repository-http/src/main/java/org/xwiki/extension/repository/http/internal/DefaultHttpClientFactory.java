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

import java.net.ProxySelector;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
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
    public HttpClient createClient(String user, String password)
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();

        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, this.configuration.getUserAgent());
        httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
        httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);

        // Setup proxy
        ProxySelectorRoutePlanner routePlanner =
            new ProxySelectorRoutePlanner(httpClient.getConnectionManager().getSchemeRegistry(),
                ProxySelector.getDefault());
        httpClient.setRoutePlanner(routePlanner);

        // Setup authentication
        if (user != null) {
            httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(user, password));
        }

        return httpClient;
    }
}
