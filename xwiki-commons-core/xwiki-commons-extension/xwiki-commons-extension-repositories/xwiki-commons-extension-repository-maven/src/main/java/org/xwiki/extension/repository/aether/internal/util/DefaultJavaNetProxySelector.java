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
package org.xwiki.extension.repository.aether.internal.util;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ProxySelector} implementation of with the default Java proxy.
 * 
 * @version $Id$
 * @since 5.2M1
 */
public class DefaultJavaNetProxySelector implements ProxySelector
{
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJavaNetProxySelector.class);

    @Override
    public Proxy getProxy(RemoteRepository repository)
    {
        try {
            return determineProxy(new URI(repository.getUrl()));
        } catch (URISyntaxException e) {
            LOGGER.warn("Invalid repository URL [{}]", repository.getUrl());
        }

        return null;
    }

    /**
     * @param targetURI the URI for which to find a proxy
     * @return the corresponding proxy
     */
    public static Proxy determineProxy(URI targetURI)
    {
        // There is obviously no proxy need to filesystem repository
        if (targetURI.getScheme().equals("file")) {
            return null;
        }

        // the proxy selector can be 'unset', so we better deal with null here
        java.net.ProxySelector psel = java.net.ProxySelector.getDefault();

        if (psel == null) {
            return null;
        }

        List<java.net.Proxy> proxies = psel.select(targetURI);

        if (proxies == null || proxies.isEmpty()) {
            return null;
        }

        java.net.Proxy javaProxy = proxies.get(0);

        Proxy result = null;
        if (javaProxy.type() == java.net.Proxy.Type.HTTP) {
            // convert the socket address to an HttpHost
            if (javaProxy.address() instanceof InetSocketAddress) {
                final InetSocketAddress isa = (InetSocketAddress) javaProxy.address();

                // TODO: Where to get the user/password ?
                result = new Proxy(targetURI.getScheme(), isa.getHostName(), isa.getPort(), null);
            } else {
                LOGGER.warn("Unsupported proxy [{}]", javaProxy);
            }
        }

        return result;
    }
}
