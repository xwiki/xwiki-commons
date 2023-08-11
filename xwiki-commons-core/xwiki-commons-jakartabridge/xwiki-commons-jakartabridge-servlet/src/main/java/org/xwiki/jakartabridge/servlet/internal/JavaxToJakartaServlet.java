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

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaServlet extends AbstractJavaxToJakartaWrapper<jakarta.servlet.Servlet> implements Servlet
{
    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaServlet(jakarta.servlet.Servlet jakarta)
    {
        super(jakarta);
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        try {
            this.jakarta.init(JakartaServletBridge.toJakarta(config));
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return new JavaxToJakartaServletConfig(this.jakarta.getServletConfig());
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        try {
            this.jakarta.service(JakartaServletBridge.toJakarta(req), JakartaServletBridge.toJakarta(res));
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public String getServletInfo()
    {
        return this.jakarta.getServletInfo();
    }

    @Override
    public void destroy()
    {
        this.jakarta.destroy();
    }
}
