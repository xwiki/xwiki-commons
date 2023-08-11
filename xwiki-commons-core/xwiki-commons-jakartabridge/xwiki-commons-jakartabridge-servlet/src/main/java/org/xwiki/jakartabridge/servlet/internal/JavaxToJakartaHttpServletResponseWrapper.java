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

import org.xwiki.jakartabridge.JavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <R> the type of http servlet response
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaHttpServletResponseWrapper<R extends jakarta.servlet.http.HttpServletResponseWrapper>
    extends HttpServletResponseWrapper implements JavaxToJakartaWrapper<R>
{
    protected final R jakarta;

    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaHttpServletResponseWrapper(R jakarta)
    {
        // Make sure that HttpServletRequestWrapper#getResponse returns something accurate
        super(JakartaServletBridge.toJavax((jakarta.servlet.http.HttpServletResponse) jakarta.getResponse()));

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

    // HttpServletResponse

    @Override
    public String getCharacterEncoding()
    {
        return this.jakarta.getCharacterEncoding();
    }

    @Override
    public String getContentType()
    {
        return this.jakarta.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        return JakartaServletBridge.toJavax(this.jakarta.getOutputStream());
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        return this.jakarta.getWriter();
    }

    @Override
    public void setCharacterEncoding(String charset)
    {
        this.jakarta.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len)
    {
        this.jakarta.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len)
    {
        this.jakarta.setContentLengthLong(len);
    }

    @Override
    public void setContentType(String type)
    {
        this.jakarta.setContentType(type);
    }

    @Override
    public void setBufferSize(int size)
    {
        this.jakarta.setBufferSize(size);
    }

    @Override
    public int getBufferSize()
    {
        return this.jakarta.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException
    {
        this.jakarta.flushBuffer();
    }

    @Override
    public void resetBuffer()
    {
        this.jakarta.resetBuffer();
    }

    @Override
    public boolean isCommitted()
    {
        return this.jakarta.isCommitted();
    }

    @Override
    public void reset()
    {
        this.jakarta.reset();
    }

    @Override
    public void setLocale(Locale loc)
    {
        this.jakarta.setLocale(loc);
    }

    @Override
    public Locale getLocale()
    {
        return this.jakarta.getLocale();
    }

    @Override
    public void addCookie(Cookie cookie)
    {
        this.jakarta.addCookie(JakartaServletBridge.toJakarta(cookie));
    }

    @Override
    public boolean containsHeader(String name)
    {
        return this.jakarta.containsHeader(name);
    }

    @Override
    public String encodeURL(String url)
    {
        return this.jakarta.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url)
    {
        return this.jakarta.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        return this.jakarta.encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        return this.jakarta.encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        this.jakarta.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        this.jakarta.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException
    {
        this.jakarta.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date)
    {
        this.jakarta.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date)
    {
        this.jakarta.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value)
    {
        this.jakarta.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value)
    {
        this.jakarta.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value)
    {
        this.jakarta.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value)
    {
        this.jakarta.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc)
    {
        this.jakarta.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        this.jakarta.setStatus(sc);
    }

    @Override
    public int getStatus()
    {
        return this.jakarta.getStatus();
    }

    @Override
    public String getHeader(String name)
    {
        return this.jakarta.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name)
    {
        return this.jakarta.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames()
    {
        return this.jakarta.getHeaderNames();
    }
}
