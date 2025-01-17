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
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;

import org.xwiki.jakartabridge.JavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <R> the type of http servlet request
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JavaxToJakartaServletRequestWrapper<R extends jakarta.servlet.ServletRequestWrapper>
    extends ServletRequestWrapper implements JavaxToJakartaWrapper<R>
{
    protected final R jakarta;

    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaServletRequestWrapper(R jakarta)
    {
        // Make sure that HttpServletRequestWrapper#getRequest returns something accurate
        super(JakartaServletBridge.toJavax(jakarta.getRequest()));

        this.jakarta = jakarta;
    }

    // JavaxToJakartaWrapper

    /**
     * @return the jakarta version
     */
    public R getJakarta()
    {
        return this.jakarta;
    }

    // ServletRequestWrapper

    @Override
    public void setRequest(ServletRequest request)
    {
        super.setRequest(request);

        this.jakarta.setRequest(JakartaServletBridge.toJakarta(request));
    }

    // HttpServletRequest

    @Override
    public Object getAttribute(String name)
    {
        return this.jakarta.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return this.jakarta.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding()
    {
        return this.jakarta.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException
    {
        this.jakarta.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength()
    {
        return this.jakarta.getContentLength();
    }

    @Override
    public long getContentLengthLong()
    {
        return this.jakarta.getContentLengthLong();
    }

    @Override
    public String getContentType()
    {
        return this.jakarta.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        return JakartaServletBridge.toJavax(this.jakarta.getInputStream());
    }

    @Override
    public String getParameter(String name)
    {
        return this.jakarta.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames()
    {
        return this.jakarta.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name)
    {
        return this.jakarta.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap()
    {
        return this.jakarta.getParameterMap();
    }

    @Override
    public String getProtocol()
    {
        return this.jakarta.getProtocol();
    }

    @Override
    public String getScheme()
    {
        return this.jakarta.getScheme();
    }

    @Override
    public String getServerName()
    {
        return this.jakarta.getServerName();
    }

    @Override
    public int getServerPort()
    {
        return this.jakarta.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        return this.jakarta.getReader();
    }

    @Override
    public String getRemoteAddr()
    {
        return this.jakarta.getRemoteAddr();
    }

    @Override
    public String getRemoteHost()
    {
        return this.jakarta.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object o)
    {
        this.jakarta.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name)
    {
        this.jakarta.removeAttribute(name);
    }

    @Override
    public Locale getLocale()
    {
        return this.jakarta.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales()
    {
        return this.jakarta.getLocales();
    }

    @Override
    public boolean isSecure()
    {
        return this.jakarta.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path)
    {
        return JakartaServletBridge.toJavax(this.jakarta.getRequestDispatcher(path));
    }

    @Override
    public String getRealPath(String path)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        return this.jakarta.getServletContext().getRealPath(path);
    }

    @Override
    public int getRemotePort()
    {
        return this.jakarta.getRemotePort();
    }

    @Override
    public String getLocalName()
    {
        return this.jakarta.getLocalName();
    }

    @Override
    public String getLocalAddr()
    {
        return this.jakarta.getLocalAddr();
    }

    @Override
    public int getLocalPort()
    {
        return this.jakarta.getLocalPort();
    }

    @Override
    public ServletContext getServletContext()
    {
        return JakartaServletBridge.toJavax(this.jakarta.getServletContext());
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException
    {
        return JakartaServletBridge.toJavax(this.jakarta.startAsync());
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
        throws IllegalStateException
    {
        return JakartaServletBridge.toJavax(this.jakarta.startAsync(JakartaServletBridge.toJakarta(servletRequest),
            JakartaServletBridge.toJakarta(servletResponse)));
    }

    @Override
    public boolean isAsyncStarted()
    {
        return this.jakarta.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported()
    {
        return this.jakarta.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext()
    {
        return JakartaServletBridge.toJavax(this.jakarta.getAsyncContext());
    }

    @Override
    public DispatcherType getDispatcherType()
    {
        return JakartaServletBridge.toJavax(this.jakarta.getDispatcherType());
    }
}
