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

import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaHttpSessionContext
    extends AbstractJavaxToJakartaWrapper<jakarta.servlet.http.HttpSessionContext> implements HttpSessionContext
{
    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaHttpSessionContext(jakarta.servlet.http.HttpSessionContext jakarta)
    {
        super(jakarta);
    }

    @Override
    public HttpSession getSession(String sessionId)
    {
        return JakartaServletBridge.toJavax(this.jakarta.getSession(sessionId));
    }

    @Override
    public Enumeration<String> getIds()
    {
        return this.jakarta.getIds();
    }

}
