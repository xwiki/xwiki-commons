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

import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.xwiki.jakartabridge.servlet.ServletBridge;

/**
 * @version $Id$
 * @since -1.jakarta
 */
public class JavaxToJakartaHttpSessionContext implements HttpSessionContext
{
    private final jakarta.servlet.http.HttpSessionContext wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JavaxToJakartaHttpSessionContext(jakarta.servlet.http.HttpSessionContext wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public HttpSession getSession(String sessionId)
    {
        return ServletBridge.toJavax(this.wrapped.getSession(sessionId));
    }

    @Override
    public Enumeration<String> getIds()
    {
        return this.wrapped.getIds();
    }

}
