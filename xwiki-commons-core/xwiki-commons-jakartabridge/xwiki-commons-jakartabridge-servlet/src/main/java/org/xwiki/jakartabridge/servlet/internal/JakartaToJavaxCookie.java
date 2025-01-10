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

import jakarta.servlet.http.Cookie;

import org.xwiki.jakartabridge.JakartaToJavaxWrapper;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JakartaToJavaxCookie extends Cookie implements JakartaToJavaxWrapper<javax.servlet.http.Cookie>
{
    private static final long serialVersionUID = 869891252570863118L;

    private final javax.servlet.http.Cookie javax;

    /**
     * @param javax the wrapped version
     * @see Cookie#Cookie(String, String)
     */
    public JakartaToJavaxCookie(javax.servlet.http.Cookie javax)
    {
        super("name", "value");

        this.javax = javax;
    }

    @Override
    public javax.servlet.http.Cookie getJavax()
    {
        return this.javax;
    }

    @Override
    public String getComment()
    {
        return this.javax.getComment();
    }

    @Override
    public String getDomain()
    {
        return this.javax.getDomain();
    }

    @Override
    public int getMaxAge()
    {
        return this.javax.getMaxAge();
    }

    @Override
    public String getName()
    {
        return this.javax.getName();
    }

    @Override
    public String getPath()
    {
        return this.javax.getPath();
    }

    @Override
    public boolean getSecure()
    {
        return this.javax.getSecure();
    }

    @Override
    public String getValue()
    {
        return this.javax.getValue();
    }

    @Override
    public int getVersion()
    {
        return this.javax.getVersion();
    }

    @Override
    public boolean isHttpOnly()
    {
        return this.javax.isHttpOnly();
    }

    @Override
    public void setComment(String purpose)
    {
        this.javax.setComment(purpose);
    }

    @Override
    public void setDomain(String domain)
    {
        this.javax.setDomain(domain);
    }

    @Override
    public void setHttpOnly(boolean isHttpOnly)
    {
        this.javax.setHttpOnly(isHttpOnly);
    }

    @Override
    public void setMaxAge(int expiry)
    {
        this.javax.setMaxAge(expiry);
    }

    @Override
    public void setPath(String uri)
    {
        this.javax.setPath(uri);
    }

    @Override
    public void setSecure(boolean flag)
    {
        this.javax.setSecure(flag);
    }

    @Override
    public void setValue(String newValue)
    {
        this.javax.setValue(newValue);
    }

    @Override
    public void setVersion(int v)
    {
        this.javax.setVersion(v);
    }

    @Override
    public Object clone()
    {
        return new JakartaToJavaxCookie((javax.servlet.http.Cookie) this.javax.clone());
    }

    @Override
    public String toString()
    {
        return this.javax.toString();
    }
}
