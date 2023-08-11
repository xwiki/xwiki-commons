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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponseWrapper;

import org.xwiki.jakartabridge.servlet.JakartaServletBridge;
import org.xwiki.jakartabridge.servlet.JavaxToJakartaWrapper;

/**
 * @param <R> the type of http servlet response
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaHttpServletResponseWrapper<R extends jakarta.servlet.http.HttpServletResponseWrapper>
    extends HttpServletResponseWrapper implements JavaxToJakartaWrapper<R>
{
    protected final R wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JavaxToJakartaHttpServletResponseWrapper(R wrapped)
    {
        // Make sure that HttpServletRequestWrapper#getResponse returns something accurate
        super(JakartaServletBridge.toJavax((jakarta.servlet.http.HttpServletResponse) wrapped.getResponse()));

        this.wrapped = wrapped;
    }

    // JavaxToJakartaWrapper

    /**
     * @return the jakarta version
     */
    public R getJakarta()
    {
        return this.wrapped;
    }

    // HttpServletResponse

    @Override
    public String getCharacterEncoding()
    {
        return this.wrapped.getCharacterEncoding();
    }

    @Override
    public String getContentType()
    {
        return this.wrapped.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        return JakartaServletBridge.toJavax(this.wrapped.getOutputStream());
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        return this.wrapped.getWriter();
    }

    @Override
    public void setCharacterEncoding(String charset)
    {
        this.wrapped.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len)
    {
        this.wrapped.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len)
    {
        this.wrapped.setContentLengthLong(len);
    }

    @Override
    public void setContentType(String type)
    {
        this.wrapped.setContentType(type);
    }

    @Override
    public void setBufferSize(int size)
    {
        this.wrapped.setBufferSize(size);
    }

    @Override
    public int getBufferSize()
    {
        return this.wrapped.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException
    {
        this.wrapped.flushBuffer();
    }

    @Override
    public void resetBuffer()
    {
        this.wrapped.resetBuffer();
    }

    @Override
    public boolean isCommitted()
    {
        return this.wrapped.isCommitted();
    }

    @Override
    public void reset()
    {
        this.wrapped.reset();
    }

    @Override
    public void setLocale(Locale loc)
    {
        this.wrapped.setLocale(loc);
    }

    @Override
    public Locale getLocale()
    {
        return this.wrapped.getLocale();
    }

    @Override
    public void addCookie(Cookie cookie)
    {
        this.wrapped.addCookie(JakartaServletBridge.toJakarta(cookie));
    }

    @Override
    public boolean containsHeader(String name)
    {
        return this.wrapped.containsHeader(name);
    }

    @Override
    public String encodeURL(String url)
    {
        return this.wrapped.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url)
    {
        return this.wrapped.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        return this.wrapped.encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        return this.wrapped.encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        this.wrapped.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        this.wrapped.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException
    {
        this.wrapped.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date)
    {
        this.wrapped.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date)
    {
        this.wrapped.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value)
    {
        this.wrapped.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value)
    {
        this.wrapped.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value)
    {
        this.wrapped.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value)
    {
        this.wrapped.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc)
    {
        this.wrapped.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        this.wrapped.setStatus(sc);
    }

    @Override
    public int getStatus()
    {
        return this.wrapped.getStatus();
    }

    @Override
    public String getHeader(String name)
    {
        return this.wrapped.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name)
    {
        return this.wrapped.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames()
    {
        return this.wrapped.getHeaderNames();
    }
}
