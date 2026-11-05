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
package org.xwiki.jakartabridge.servlet.internal;

import jakarta.servlet.ServletConnection;

/**
 * A {@link ServletConnection} built from a javax {@link javax.servlet.ServletRequest}. Servlet 3.1 does not expose any
 * information about the network connection so each request is considered to have its own connection.
 *
 * @version $Id$
 * @since 19.0.0RC1
 */
public class JakartaToJavaxServletConnection implements ServletConnection
{
    private final javax.servlet.ServletRequest javax;

    private final String connectionId;

    /**
     * @param javax the javax request from which to extract the connection information
     * @param connectionId the identifier of the connection
     */
    public JakartaToJavaxServletConnection(javax.servlet.ServletRequest javax, String connectionId)
    {
        this.javax = javax;
        this.connectionId = connectionId;
    }

    @Override
    public String getConnectionId()
    {
        return this.connectionId;
    }

    @Override
    public String getProtocol()
    {
        return this.javax.getProtocol();
    }

    @Override
    public String getProtocolConnectionId()
    {
        // No protocol connection identifier is available in Servlet 3.1
        return "";
    }

    @Override
    public boolean isSecure()
    {
        return this.javax.isSecure();
    }
}
