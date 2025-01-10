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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JavaxToJakartaRequestDispatcher extends AbstractJavaxToJakartaWrapper<jakarta.servlet.RequestDispatcher>
    implements RequestDispatcher
{
    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaRequestDispatcher(jakarta.servlet.RequestDispatcher jakarta)
    {
        super(jakarta);
    }

    /**
     * @return the wrapped version
     */
    public jakarta.servlet.RequestDispatcher getJakarta()
    {
        return this.jakarta;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        try {
            this.jakarta.forward(JakartaServletBridge.toJakarta(request), JakartaServletBridge.toJakarta(response));
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        try {
            this.jakarta.include(JakartaServletBridge.toJakarta(request), JakartaServletBridge.toJakarta(response));
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }
}
