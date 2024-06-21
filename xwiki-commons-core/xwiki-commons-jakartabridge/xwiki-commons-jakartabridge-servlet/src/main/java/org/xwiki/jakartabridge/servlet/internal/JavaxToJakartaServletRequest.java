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
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.xwiki.jakartabridge.servlet.ServletBridge;

/**
 * @param <R> the type of servlet request
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaServletRequest<R extends jakarta.servlet.ServletRequest> implements ServletRequest
{
    protected final R wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JavaxToJakartaServletRequest(R wrapped)
    {
        this.wrapped = wrapped;
    }

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
        return ServletBridge.toJavax(this.wrapped.getInputStream());
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
        return ServletBridge.toJavax(this.wrapped.getRequestDispatcher(path));
    }

    @Override
    public String getRealPath(String path)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        return this.wrapped.getServletContext().getRealPath(path);
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
        return ServletBridge.toJavax(this.wrapped.getServletContext());
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException
    {
        return ServletBridge.toJavax(this.wrapped.startAsync());
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
        throws IllegalStateException
    {
        return ServletBridge.toJavax(this.wrapped.startAsync(ServletBridge.toJakarta(servletRequest),
            ServletBridge.toJakarta(servletResponse)));
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
        return ServletBridge.toJavax(this.wrapped.getAsyncContext());
    }

    @Override
    public DispatcherType getDispatcherType()
    {
        return ServletBridge.toJavax(this.wrapped.getDispatcherType());
    }
}
