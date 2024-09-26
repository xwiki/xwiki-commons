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

import jakarta.servlet.SessionCookieConfig;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JakartaToJavaxSessionCookieConfig extends AbstractJakartaToJavaxWrapper<javax.servlet.SessionCookieConfig>
    implements SessionCookieConfig
{
    /**
     * @param wrapped the wrapped version
     */
    public JakartaToJavaxSessionCookieConfig(javax.servlet.SessionCookieConfig wrapped)
    {
        super(wrapped);
    }

    @Override
    public void setName(String name)
    {
        this.wrapped.setName(name);
    }

    @Override
    public String getName()
    {
        return this.wrapped.getName();
    }

    @Override
    public void setDomain(String domain)
    {
        this.wrapped.setDomain(domain);
    }

    @Override
    public String getDomain()
    {
        return this.wrapped.getDomain();
    }

    @Override
    public void setPath(String path)
    {
        this.wrapped.setPath(path);
    }

    @Override
    public String getPath()
    {
        return this.wrapped.getPath();
    }

    @Override
    public void setComment(String comment)
    {
        this.wrapped.setComment(comment);
    }

    @Override
    public String getComment()
    {
        return this.wrapped.getComment();
    }

    @Override
    public void setHttpOnly(boolean httpOnly)
    {
        this.wrapped.setHttpOnly(httpOnly);
    }

    @Override
    public boolean isHttpOnly()
    {
        return this.wrapped.isHttpOnly();
    }

    @Override
    public void setSecure(boolean secure)
    {
        this.wrapped.setSecure(secure);
    }

    @Override
    public boolean isSecure()
    {
        return this.wrapped.isSecure();
    }

    @Override
    public void setMaxAge(int maxAge)
    {
        this.wrapped.setMaxAge(maxAge);
    }

    @Override
    public int getMaxAge()
    {
        return this.wrapped.getMaxAge();
    }
}
