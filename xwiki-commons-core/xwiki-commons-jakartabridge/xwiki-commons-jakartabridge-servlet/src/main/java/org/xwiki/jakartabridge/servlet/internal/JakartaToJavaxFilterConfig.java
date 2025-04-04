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

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;

import org.xwiki.jakartabridge.internal.AbstractJakartaToJavaxWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaToJavaxFilterConfig extends AbstractJakartaToJavaxWrapper<javax.servlet.FilterConfig>
    implements FilterConfig
{
    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxFilterConfig(javax.servlet.FilterConfig javax)
    {
        super(javax);
    }

    @Override
    public String getFilterName()
    {
        return this.javax.getFilterName();
    }

    @Override
    public ServletContext getServletContext()
    {
        return JakartaServletBridge.toJakarta(this.javax.getServletContext());
    }

    @Override
    public String getInitParameter(String name)
    {
        return this.javax.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames()
    {
        return this.javax.getInitParameterNames();
    }
}
