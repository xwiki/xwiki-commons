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

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JavaxToJakartaHttpSession extends AbstractJavaxToJakartaWrapper<jakarta.servlet.http.HttpSession>
    implements HttpSession
{
    private static final String[] EMPTY_ARRAY = new String[0];

    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaHttpSession(jakarta.servlet.http.HttpSession jakarta)
    {
        super(jakarta);
    }

    @Override
    public long getCreationTime()
    {
        return this.jakarta.getCreationTime();
    }

    @Override
    public String getId()
    {
        return this.jakarta.getId();
    }

    @Override
    public long getLastAccessedTime()
    {
        return this.jakarta.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext()
    {
        return JakartaServletBridge.toJavax(this.jakarta.getServletContext());
    }

    @Override
    public void setMaxInactiveInterval(int interval)
    {
        this.jakarta.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval()
    {
        return this.jakarta.getMaxInactiveInterval();
    }

    @Override
    public HttpSessionContext getSessionContext()
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        return EmptyJakartaHttpSessionContext.EMPTY;
    }

    @Override
    public Object getAttribute(String name)
    {
        return this.jakarta.getAttribute(name);
    }

    @Override
    public Object getValue(String name)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        return this.jakarta.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return this.jakarta.getAttributeNames();
    }

    @Override
    public String[] getValueNames()
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        return Collections.<String>list(this.jakarta.getAttributeNames()).toArray(EMPTY_ARRAY);
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        this.jakarta.setAttribute(name, value);
    }

    @Override
    public void putValue(String name, Object value)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        this.jakarta.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name)
    {
        this.jakarta.removeAttribute(name);
    }

    @Override
    public void removeValue(String name)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        this.jakarta.removeAttribute(name);
    }

    @Override
    public void invalidate()
    {
        this.jakarta.invalidate();
    }

    @Override
    public boolean isNew()
    {
        return this.jakarta.isNew();
    }

}
