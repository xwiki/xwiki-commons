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

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.xwiki.component.annotation.Role;

/**
 * Constructs {@link CloseableHttpClient} objects that can be used to perform request on HTTP-based Extension
 * Repositories.
 *
 * @version $Id$
 * @since 5.2M1
 */
@Role
public interface HttpClientFactory
{
    /**
     * Defines the socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the timeout for waiting for data or,
     * put differently, a maximum period inactivity between two consecutive data packets). A timeout value of zero is
     * interpreted as an infinite timeout.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     * 
     * @see java.net.SocketOptions#SO_TIMEOUT
     * @since 8.3M1
     */
    String SOCKET_TIMEOUT = "http.socket.timeout";

    /**
     * Determines the timeout in milliseconds until a connection is established. A timeout value of zero is interpreted
     * as an infinite timeout.
     * <p>
     * Please note this parameter can only be applied to connections that are bound to a particular local address.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     * 
     * @since 8.3M1
     */
    String CONNECTION_TIMEOUT = "http.connection.timeout";

    /**
     * @param user the user if the remote repository requires authentication, or null if no authentication is required
     * @param password the password if the remote repository requires authentication, or null if no authentication is
     *            required
     * @return the {@link CloseableHttpClient} object that can be used to perform HTTP calls to an HTTP-based Extension
     *         Repository
     */
    CloseableHttpClient createClient(String user, String password);

    /**
     * @param user the user if the remote repository requires authentication, or null if no authentication is required
     * @param password the password if the remote repository requires authentication, or null if no authentication is
     *            required
     * @return the pre-configured {@link HttpClientBuilder} instance that can be customized and used to build the actual
     *         {@link HttpClient}
     * @since 8.3M1
     */
    HttpClientBuilder createHttpClientBuilder(String user, String password);

    /**
     * @param properties properties
     * @return the pre-configured {@link HttpClientBuilder} instance that can be customized and used to build the actual
     *         {@link HttpClient}
     * @since 8.3M1
     */
    HttpClientBuilder createHttpClientBuilder(Map<String, String> properties);
}
