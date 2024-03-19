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
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.xwiki.jakartabridge.servlet.ServletBridge;

/**
 * Bridge a {@link jakarta.servlet.http.HttpServletRequest} through a {@link HttpServletRequest}.
 * 
 * @version $Id$
 * @since jakarta
 */
public class JavaxToJakartaHttpServletRequest
    extends JavaxToJakartaServletRequest<jakarta.servlet.http.HttpServletRequest> implements HttpServletRequest
{
    /**
     * @param wrapped the wrapped version
     */
    public JavaxToJakartaHttpServletRequest(jakarta.servlet.http.HttpServletRequest wrapped)
    {
        super(wrapped);
    }

    @Override
    public String getAuthType()
    {
        return this.wrapped.getAuthType();
    }

    @Override
    public Cookie[] getCookies()
    {
        return ServletBridge.toJavax(this.wrapped.getCookies());
    }

    @Override
    public long getDateHeader(String name)
    {
        return this.wrapped.getDateHeader(name);
    }

    @Override
    public String getHeader(String name)
    {
        return this.wrapped.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name)
    {
        return this.wrapped.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames()
    {
        return this.wrapped.getHeaderNames();
    }

    @Override
    public int getIntHeader(String name)
    {
        return this.wrapped.getIntHeader(name);
    }

    @Override
    public String getMethod()
    {
        return this.wrapped.getMethod();
    }

    @Override
    public String getPathInfo()
    {
        return this.wrapped.getPathInfo();
    }

    @Override
    public String getPathTranslated()
    {
        return this.wrapped.getPathTranslated();
    }

    @Override
    public String getContextPath()
    {
        return this.wrapped.getContextPath();
    }

    @Override
    public String getQueryString()
    {
        return this.wrapped.getQueryString();
    }

    @Override
    public String getRemoteUser()
    {
        return this.wrapped.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role)
    {
        return this.wrapped.isUserInRole(role);
    }

    @Override
    public Principal getUserPrincipal()
    {
        return this.wrapped.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId()
    {
        return this.wrapped.getRequestedSessionId();
    }

    @Override
    public String getRequestURI()
    {
        return this.wrapped.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL()
    {
        return this.wrapped.getRequestURL();
    }

    @Override
    public String getServletPath()
    {
        return this.wrapped.getServletPath();
    }

    @Override
    public HttpSession getSession(boolean create)
    {
        return ServletBridge.toJavax(this.wrapped.getSession(create));
    }

    @Override
    public HttpSession getSession()
    {
        return ServletBridge.toJavax(this.wrapped.getSession());
    }

    @Override
    public String changeSessionId()
    {
        return this.wrapped.changeSessionId();
    }

    @Override
    public boolean isRequestedSessionIdValid()
    {
        return this.wrapped.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie()
    {
        return this.wrapped.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL()
    {
        return this.wrapped.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl()
    {
        return this.wrapped.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException
    {
        try {
            return this.wrapped.authenticate(ServletBridge.toJakarta(response));
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void login(String username, String password) throws ServletException
    {
        try {
            this.wrapped.login(username, password);
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void logout() throws ServletException
    {
        try {
            this.wrapped.logout();
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException
    {
        try {
            return this.wrapped.getParts().stream().map(p -> (Part) ServletBridge.toJavax(p)).toList();
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException
    {
        try {
            return ServletBridge.toJavax(this.wrapped.getPart(name));
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException
    {
        // TODO
        throw new UnsupportedOperationException();
    }
}
