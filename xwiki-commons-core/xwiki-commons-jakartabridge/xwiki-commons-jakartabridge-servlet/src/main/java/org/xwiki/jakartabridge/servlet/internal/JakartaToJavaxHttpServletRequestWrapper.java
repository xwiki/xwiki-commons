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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import org.xwiki.jakartabridge.servlet.JakartaServletBridge;
import org.xwiki.jakartabridge.servlet.JakartaToJavaxWrapper;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

/**
 * @param <R> the type of http servlet request
 * @version $Id$
 * @since 42.0.0
 */
public class JakartaToJavaxHttpServletRequestWrapper<R extends javax.servlet.http.HttpServletRequestWrapper>
    extends HttpServletRequestWrapper implements JakartaToJavaxWrapper<R>
{
    protected final R wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JakartaToJavaxHttpServletRequestWrapper(R wrapped)
    {
        super(JakartaServletBridge.toJakarta((javax.servlet.http.HttpServletRequest) wrapped.getRequest()));

        this.wrapped = wrapped;
    }

    // JakartaToJavaxWrapper

    @Override
    public R getJavax()
    {
        return this.wrapped;
    }

    // HttpServletRequest

    @Override
    public Object getAttribute(String name)
    {
        return this.wrapped.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return this.wrapped.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding()
    {
        return this.wrapped.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException
    {
        this.wrapped.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength()
    {
        return this.wrapped.getContentLength();
    }

    @Override
    public long getContentLengthLong()
    {
        return this.wrapped.getContentLengthLong();
    }

    @Override
    public String getContentType()
    {
        return this.wrapped.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        return JakartaServletBridge.toJakarta(this.wrapped.getInputStream());
    }

    @Override
    public String getParameter(String name)
    {
        return this.wrapped.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames()
    {
        return this.wrapped.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name)
    {
        return this.wrapped.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap()
    {
        return this.wrapped.getParameterMap();
    }

    @Override
    public String getProtocol()
    {
        return this.wrapped.getProtocol();
    }

    @Override
    public String getScheme()
    {
        return this.wrapped.getScheme();
    }

    @Override
    public String getServerName()
    {
        return this.wrapped.getServerName();
    }

    @Override
    public int getServerPort()
    {
        return this.wrapped.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        return this.wrapped.getReader();
    }

    @Override
    public String getRemoteAddr()
    {
        return this.wrapped.getRemoteAddr();
    }

    @Override
    public String getRemoteHost()
    {
        return this.wrapped.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object o)
    {
        this.wrapped.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name)
    {
        this.wrapped.removeAttribute(name);
    }

    @Override
    public Locale getLocale()
    {
        return this.wrapped.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales()
    {
        return this.wrapped.getLocales();
    }

    @Override
    public boolean isSecure()
    {
        return this.wrapped.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path)
    {
        return JakartaServletBridge.toJakarta(this.wrapped.getRequestDispatcher(path));
    }

    @Override
    public String getRealPath(String path)
    {
        return this.wrapped.getRealPath(path);
    }

    @Override
    public int getRemotePort()
    {
        return this.wrapped.getRemotePort();
    }

    @Override
    public String getLocalName()
    {
        return this.wrapped.getLocalName();
    }

    @Override
    public String getLocalAddr()
    {
        return this.wrapped.getLocalAddr();
    }

    @Override
    public int getLocalPort()
    {
        return this.wrapped.getLocalPort();
    }

    @Override
    public ServletContext getServletContext()
    {
        return JakartaServletBridge.toJakarta(this.wrapped.getServletContext());
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException
    {
        return JakartaServletBridge.toJakarta(this.wrapped.startAsync());
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
        throws IllegalStateException
    {
        return JakartaServletBridge.toJakarta(this.wrapped.startAsync(JakartaServletBridge.toJavax(servletRequest),
            JakartaServletBridge.toJavax(servletResponse)));
    }

    @Override
    public boolean isAsyncStarted()
    {
        return this.wrapped.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported()
    {
        return this.wrapped.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext()
    {
        return JakartaServletBridge.toJakarta(this.wrapped.getAsyncContext());
    }

    @Override
    public DispatcherType getDispatcherType()
    {
        return JakartaServletBridge.toJakarta(this.wrapped.getDispatcherType());
    }

    @Override
    public String getAuthType()
    {
        return this.wrapped.getAuthType();
    }

    @Override
    public Cookie[] getCookies()
    {
        return JakartaServletBridge.toJakarta(this.wrapped.getCookies());
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
        return JakartaServletBridge.toJakarta(this.wrapped.getSession(create));
    }

    @Override
    public HttpSession getSession()
    {
        return JakartaServletBridge.toJakarta(this.wrapped.getSession());
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
            return this.wrapped.authenticate(JakartaServletBridge.toJavax(response));
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void login(String username, String password) throws ServletException
    {
        try {
            this.wrapped.login(username, password);
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void logout() throws ServletException
    {
        try {
            this.wrapped.logout();
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException
    {
        try {
            return this.wrapped.getParts().stream().map(JakartaServletBridge::toJakarta).toList();
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException
    {
        try {
            return JakartaServletBridge.toJakarta(this.wrapped.getPart(name));
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
