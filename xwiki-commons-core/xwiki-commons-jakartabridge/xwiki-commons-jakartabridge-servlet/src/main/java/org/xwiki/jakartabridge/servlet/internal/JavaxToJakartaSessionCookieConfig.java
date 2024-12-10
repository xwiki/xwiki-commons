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

import javax.servlet.SessionCookieConfig;

import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaSessionCookieConfig
    extends AbstractJavaxToJakartaWrapper<jakarta.servlet.SessionCookieConfig> implements SessionCookieConfig
{
    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaSessionCookieConfig(jakarta.servlet.SessionCookieConfig jakarta)
    {
        super(jakarta);
    }

    @Override
    public void setName(String name)
    {
        this.jakarta.setName(name);
    }

    @Override
    public String getName()
    {
        return this.jakarta.getName();
    }

    @Override
    public void setDomain(String domain)
    {
        this.jakarta.setDomain(domain);
    }

    @Override
    public String getDomain()
    {
        return this.jakarta.getDomain();
    }

    @Override
    public void setPath(String path)
    {
        this.jakarta.setPath(path);
    }

    @Override
    public String getPath()
    {
        return this.jakarta.getPath();
    }

    @Override
    public void setComment(String comment)
    {
        this.jakarta.setComment(comment);
    }

    @Override
    public String getComment()
    {
        return this.jakarta.getComment();
    }

    @Override
    public void setHttpOnly(boolean httpOnly)
    {
        this.jakarta.setHttpOnly(httpOnly);
    }

    @Override
    public boolean isHttpOnly()
    {
        return this.jakarta.isHttpOnly();
    }

    @Override
    public void setSecure(boolean secure)
    {
        this.jakarta.setSecure(secure);
    }

    @Override
    public boolean isSecure()
    {
        return this.jakarta.isSecure();
    }

    @Override
    public void setMaxAge(int maxAge)
    {
        this.jakarta.setMaxAge(maxAge);
    }

    @Override
    public int getMaxAge()
    {
        return this.jakarta.getMaxAge();
    }
}
