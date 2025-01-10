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

import org.xwiki.jakartabridge.internal.AbstractJakartaToJavaxWrapper;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaToJavaxSessionCookieConfig extends AbstractJakartaToJavaxWrapper<javax.servlet.SessionCookieConfig>
    implements SessionCookieConfig
{
    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxSessionCookieConfig(javax.servlet.SessionCookieConfig javax)
    {
        super(javax);
    }

    @Override
    public void setName(String name)
    {
        this.javax.setName(name);
    }

    @Override
    public String getName()
    {
        return this.javax.getName();
    }

    @Override
    public void setDomain(String domain)
    {
        this.javax.setDomain(domain);
    }

    @Override
    public String getDomain()
    {
        return this.javax.getDomain();
    }

    @Override
    public void setPath(String path)
    {
        this.javax.setPath(path);
    }

    @Override
    public String getPath()
    {
        return this.javax.getPath();
    }

    @Override
    public void setComment(String comment)
    {
        this.javax.setComment(comment);
    }

    @Override
    public String getComment()
    {
        return this.javax.getComment();
    }

    @Override
    public void setHttpOnly(boolean httpOnly)
    {
        this.javax.setHttpOnly(httpOnly);
    }

    @Override
    public boolean isHttpOnly()
    {
        return this.javax.isHttpOnly();
    }

    @Override
    public void setSecure(boolean secure)
    {
        this.javax.setSecure(secure);
    }

    @Override
    public boolean isSecure()
    {
        return this.javax.isSecure();
    }

    @Override
    public void setMaxAge(int maxAge)
    {
        this.javax.setMaxAge(maxAge);
    }

    @Override
    public int getMaxAge()
    {
        return this.javax.getMaxAge();
    }
}
