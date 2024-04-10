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

import org.xwiki.jakartabridge.servlet.ServletBridge;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

/**
 * @version $Id$
 * @since 17-jakarta
 */
public class JakartaToJavaxServletConfig implements ServletConfig
{
    private final javax.servlet.ServletConfig wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JakartaToJavaxServletConfig(javax.servlet.ServletConfig wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public String getServletName()
    {
        return this.wrapped.getServletName();
    }

    @Override
    public ServletContext getServletContext()
    {
        return ServletBridge.toJakarta(this.wrapped.getServletContext());
    }

    @Override
    public String getInitParameter(String name)
    {
        return this.wrapped.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames()
    {
        return this.wrapped.getInitParameterNames();
    }
}
