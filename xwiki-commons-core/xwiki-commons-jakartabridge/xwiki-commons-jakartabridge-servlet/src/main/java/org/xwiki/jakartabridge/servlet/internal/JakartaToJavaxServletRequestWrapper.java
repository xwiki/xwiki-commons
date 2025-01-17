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

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.ServletResponse;

import org.xwiki.jakartabridge.JakartaToJavaxWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <R> the type of http servlet request
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaToJavaxServletRequestWrapper<R extends javax.servlet.ServletRequestWrapper>
    extends ServletRequestWrapper implements JakartaToJavaxWrapper<R>
{
    protected final R javax;

    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxServletRequestWrapper(R javax)
    {
        super(JakartaServletBridge.toJakarta(javax.getRequest()));

        this.javax = javax;
    }

    // JakartaToJavaxWrapper

    @Override
    public R getJavax()
    {
        return this.javax;
    }

    // ServletRequestWrapper

    @Override
    public void setRequest(ServletRequest request)
    {
        super.setRequest(request);

        this.javax.setRequest(JakartaServletBridge.toJavax(request));
    }

    // ServletRequest

    @Override
    public Object getAttribute(String name)
    {
        return this.javax.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return this.javax.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding()
    {
        return this.javax.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException
    {
        this.javax.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength()
    {
        return this.javax.getContentLength();
    }

    @Override
    public long getContentLengthLong()
    {
        return this.javax.getContentLengthLong();
    }

    @Override
    public String getContentType()
    {
        return this.javax.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        return JakartaServletBridge.toJakarta(this.javax.getInputStream());
    }

    @Override
    public String getParameter(String name)
    {
        return this.javax.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames()
    {
        return this.javax.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name)
    {
        return this.javax.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap()
    {
        return this.javax.getParameterMap();
    }

    @Override
    public String getProtocol()
    {
        return this.javax.getProtocol();
    }

    @Override
    public String getScheme()
    {
        return this.javax.getScheme();
    }

    @Override
    public String getServerName()
    {
        return this.javax.getServerName();
    }

    @Override
    public int getServerPort()
    {
        return this.javax.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        return this.javax.getReader();
    }

    @Override
    public String getRemoteAddr()
    {
        return this.javax.getRemoteAddr();
    }

    @Override
    public String getRemoteHost()
    {
        return this.javax.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object o)
    {
        this.javax.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name)
    {
        this.javax.removeAttribute(name);
    }

    @Override
    public Locale getLocale()
    {
        return this.javax.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales()
    {
        return this.javax.getLocales();
    }

    @Override
    public boolean isSecure()
    {
        return this.javax.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path)
    {
        return JakartaServletBridge.toJakarta(this.javax.getRequestDispatcher(path));
    }

    @Override
    public String getRealPath(String path)
    {
        return this.javax.getRealPath(path);
    }

    @Override
    public int getRemotePort()
    {
        return this.javax.getRemotePort();
    }

    @Override
    public String getLocalName()
    {
        return this.javax.getLocalName();
    }

    @Override
    public String getLocalAddr()
    {
        return this.javax.getLocalAddr();
    }

    @Override
    public int getLocalPort()
    {
        return this.javax.getLocalPort();
    }

    @Override
    public ServletContext getServletContext()
    {
        return JakartaServletBridge.toJakarta(this.javax.getServletContext());
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException
    {
        return JakartaServletBridge.toJakarta(this.javax.startAsync());
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
        throws IllegalStateException
    {
        return JakartaServletBridge.toJakarta(this.javax.startAsync(JakartaServletBridge.toJavax(servletRequest),
            JakartaServletBridge.toJavax(servletResponse)));
    }

    @Override
    public boolean isAsyncStarted()
    {
        return this.javax.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported()
    {
        return this.javax.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext()
    {
        return JakartaServletBridge.toJakarta(this.javax.getAsyncContext());
    }

    @Override
    public DispatcherType getDispatcherType()
    {
        return JakartaServletBridge.toJakarta(this.javax.getDispatcherType());
    }
}
