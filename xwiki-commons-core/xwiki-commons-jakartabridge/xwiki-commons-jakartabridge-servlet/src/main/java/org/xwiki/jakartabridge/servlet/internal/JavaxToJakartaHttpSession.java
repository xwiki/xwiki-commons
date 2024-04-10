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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.xwiki.jakartabridge.servlet.ServletBridge;

/**
 * @version $Id$
 * @since 17-jakarta
 */
public class JavaxToJakartaHttpSession implements HttpSession
{
    private final jakarta.servlet.http.HttpSession wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JavaxToJakartaHttpSession(jakarta.servlet.http.HttpSession wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public long getCreationTime()
    {
        return this.wrapped.getCreationTime();
    }

    @Override
    public String getId()
    {
        return this.wrapped.getId();
    }

    @Override
    public long getLastAccessedTime()
    {
        return this.wrapped.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext()
    {
        return ServletBridge.toJavax(this.wrapped.getServletContext());
    }

    @Override
    public void setMaxInactiveInterval(int interval)
    {
        this.wrapped.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval()
    {
        return this.wrapped.getMaxInactiveInterval();
    }

    @Override
    public HttpSessionContext getSessionContext()
    {
        return ServletBridge.toJavax(this.wrapped.getSessionContext());
    }

    @Override
    public Object getAttribute(String name)
    {
        return this.wrapped.getAttribute(name);
    }

    @Override
    public Object getValue(String name)
    {
        return this.wrapped.getValue(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return this.wrapped.getAttributeNames();
    }

    @Override
    public String[] getValueNames()
    {
        return this.wrapped.getValueNames();
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        this.wrapped.setAttribute(name, value);
    }

    @Override
    public void putValue(String name, Object value)
    {
        this.wrapped.putValue(name, value);
    }

    @Override
    public void removeAttribute(String name)
    {
        this.wrapped.removeAttribute(name);
    }

    @Override
    public void removeValue(String name)
    {
        this.wrapped.removeValue(name);
    }

    @Override
    public void invalidate()
    {
        this.wrapped.invalidate();
    }

    @Override
    public boolean isNew()
    {
        return this.wrapped.isNew();
    }

}
