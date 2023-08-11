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

import javax.servlet.http.Cookie;

import org.xwiki.jakartabridge.JavaxToJakartaWrapper;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaCookie extends Cookie implements JavaxToJakartaWrapper<jakarta.servlet.http.Cookie>
{
    private static final long serialVersionUID = 869891252570863118L;

    private final jakarta.servlet.http.Cookie jakarta;

    /**
     * @param jakarta the wrapped version
     * @see Cookie#Cookie(String, String)
     */
    public JavaxToJakartaCookie(jakarta.servlet.http.Cookie jakarta)
    {
        super("name", "value");

        this.jakarta = jakarta;
    }

    @Override
    public jakarta.servlet.http.Cookie getJakarta()
    {
        return this.jakarta;
    }

    @Override
    public String getComment()
    {
        return this.jakarta.getComment();
    }

    @Override
    public String getDomain()
    {
        return this.jakarta.getDomain();
    }

    @Override
    public int getMaxAge()
    {
        return this.jakarta.getMaxAge();
    }

    @Override
    public String getName()
    {
        return this.jakarta.getName();
    }

    @Override
    public String getPath()
    {
        return this.jakarta.getPath();
    }

    @Override
    public boolean getSecure()
    {
        return this.jakarta.getSecure();
    }

    @Override
    public String getValue()
    {
        return this.jakarta.getValue();
    }

    @Override
    public int getVersion()
    {
        return this.jakarta.getVersion();
    }

    @Override
    public boolean isHttpOnly()
    {
        return this.jakarta.isHttpOnly();
    }

    @Override
    public void setComment(String purpose)
    {
        this.jakarta.setComment(purpose);
    }

    @Override
    public void setDomain(String domain)
    {
        this.jakarta.setDomain(domain);
    }

    @Override
    public void setHttpOnly(boolean isHttpOnly)
    {
        this.jakarta.setHttpOnly(isHttpOnly);
    }

    @Override
    public void setMaxAge(int expiry)
    {
        this.jakarta.setMaxAge(expiry);
    }

    @Override
    public void setPath(String uri)
    {
        this.jakarta.setPath(uri);
    }

    @Override
    public void setSecure(boolean flag)
    {
        this.jakarta.setSecure(flag);
    }

    @Override
    public void setValue(String newValue)
    {
        this.jakarta.setValue(newValue);
    }

    @Override
    public void setVersion(int v)
    {
        this.jakarta.setVersion(v);
    }

    @Override
    public Object clone()
    {
        return new JavaxToJakartaCookie((jakarta.servlet.http.Cookie) this.jakarta.clone());
    }

    @Override
    public String toString()
    {
        return this.jakarta.toString();
    }
}
