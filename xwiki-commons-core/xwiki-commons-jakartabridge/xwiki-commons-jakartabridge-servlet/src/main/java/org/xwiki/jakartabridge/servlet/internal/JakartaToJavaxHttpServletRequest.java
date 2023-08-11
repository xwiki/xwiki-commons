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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <R> the type of http servlet request
 * @version $Id$
 * @since 42.0.0
 */
public class JakartaToJavaxHttpServletRequest<R extends javax.servlet.http.HttpServletRequest>
    extends JakartaToJavaxServletRequest<R> implements HttpServletRequest
{
    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxHttpServletRequest(R javax)
    {
        super(javax);
    }

    @Override
    public String getAuthType()
    {
        return this.javax.getAuthType();
    }

    @Override
    public Cookie[] getCookies()
    {
        return JakartaServletBridge.toJakarta(this.javax.getCookies());
    }

    @Override
    public long getDateHeader(String name)
    {
        return this.javax.getDateHeader(name);
    }

    @Override
    public String getHeader(String name)
    {
        return this.javax.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name)
    {
        return this.javax.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames()
    {
        return this.javax.getHeaderNames();
    }

    @Override
    public int getIntHeader(String name)
    {
        return this.javax.getIntHeader(name);
    }

    @Override
    public String getMethod()
    {
        return this.javax.getMethod();
    }

    @Override
    public String getPathInfo()
    {
        return this.javax.getPathInfo();
    }

    @Override
    public String getPathTranslated()
    {
        return this.javax.getPathTranslated();
    }

    @Override
    public String getContextPath()
    {
        return this.javax.getContextPath();
    }

    @Override
    public String getQueryString()
    {
        return this.javax.getQueryString();
    }

    @Override
    public String getRemoteUser()
    {
        return this.javax.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role)
    {
        return this.javax.isUserInRole(role);
    }

    @Override
    public Principal getUserPrincipal()
    {
        return this.javax.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId()
    {
        return this.javax.getRequestedSessionId();
    }

    @Override
    public String getRequestURI()
    {
        return this.javax.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL()
    {
        return this.javax.getRequestURL();
    }

    @Override
    public String getServletPath()
    {
        return this.javax.getServletPath();
    }

    @Override
    public HttpSession getSession(boolean create)
    {
        return JakartaServletBridge.toJakarta(this.javax.getSession(create));
    }

    @Override
    public HttpSession getSession()
    {
        return JakartaServletBridge.toJakarta(this.javax.getSession());
    }

    @Override
    public String changeSessionId()
    {
        return this.javax.changeSessionId();
    }

    @Override
    public boolean isRequestedSessionIdValid()
    {
        return this.javax.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie()
    {
        return this.javax.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL()
    {
        return this.javax.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl()
    {
        return this.javax.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException
    {
        try {
            return this.javax.authenticate(JakartaServletBridge.toJavax(response));
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void login(String username, String password) throws ServletException
    {
        try {
            this.javax.login(username, password);
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void logout() throws ServletException
    {
        try {
            this.javax.logout();
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException
    {
        try {
            return JakartaServletBridge.toJakarta(this.javax.getParts());
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException
    {
        try {
            return JakartaServletBridge.toJakarta(this.javax.getPart(name));
        } catch (javax.servlet.ServletException e) {
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
