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

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

import org.xwiki.jakartabridge.internal.AbstractJakartaToJavaxWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaToJavaxHttpSession extends AbstractJakartaToJavaxWrapper<javax.servlet.http.HttpSession>
    implements HttpSession
{
    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxHttpSession(javax.servlet.http.HttpSession javax)
    {
        super(javax);
    }

    @Override
    public long getCreationTime()
    {
        return this.javax.getCreationTime();
    }

    @Override
    public String getId()
    {
        return this.javax.getId();
    }

    @Override
    public long getLastAccessedTime()
    {
        return this.javax.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext()
    {
        return JakartaServletBridge.toJakarta(this.javax.getServletContext());
    }

    @Override
    public void setMaxInactiveInterval(int interval)
    {
        this.javax.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval()
    {
        return this.javax.getMaxInactiveInterval();
    }

    @Override
    public HttpSessionContext getSessionContext()
    {
        return JakartaServletBridge.toJakarta(this.javax.getSessionContext());
    }

    @Override
    public Object getAttribute(String name)
    {
        return this.javax.getAttribute(name);
    }

    @Override
    public Object getValue(String name)
    {
        return this.javax.getValue(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return this.javax.getAttributeNames();
    }

    @Override
    public String[] getValueNames()
    {
        return this.javax.getValueNames();
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        this.javax.setAttribute(name, value);
    }

    @Override
    public void putValue(String name, Object value)
    {
        this.javax.putValue(name, value);
    }

    @Override
    public void removeAttribute(String name)
    {
        this.javax.removeAttribute(name);
    }

    @Override
    public void removeValue(String name)
    {
        this.javax.removeValue(name);
    }

    @Override
    public void invalidate()
    {
        this.javax.invalidate();
    }

    @Override
    public boolean isNew()
    {
        return this.javax.isNew();
    }

}
