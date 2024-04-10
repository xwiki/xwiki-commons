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

import org.xwiki.jakartabridge.servlet.ServletBridge;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * @version $Id$
 * @since 17-jakarta
 */
public class JakartaToJavaxServlet implements Servlet
{
    private final javax.servlet.Servlet wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JakartaToJavaxServlet(javax.servlet.Servlet wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        try {
            this.wrapped.init(ServletBridge.toJavax(config));
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return ServletBridge.toJakarta(this.wrapped.getServletConfig());
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        try {
            this.wrapped.service(ServletBridge.toJavax(req), ServletBridge.toJavax(res));
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public String getServletInfo()
    {
        return this.wrapped.getServletInfo();
    }

    @Override
    public void destroy()
    {
        this.wrapped.destroy();
    }
}
