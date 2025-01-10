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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.xwiki.jakartabridge.JakartaToJavaxWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <R> the type of http servlet response
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaToJavaxHttpServletResponseWrapper<R extends javax.servlet.http.HttpServletResponseWrapper>
    extends HttpServletResponseWrapper implements JakartaToJavaxWrapper<R>
{
    protected final R javax;

    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxHttpServletResponseWrapper(R javax)
    {
        super(JakartaServletBridge.toJakarta((javax.servlet.http.HttpServletResponse) javax.getResponse()));

        this.javax = javax;
    }

    // JakartaToJavaxWrapper

    @Override
    public R getJavax()
    {
        return this.javax;
    }

    // HttpServletResponse

    @Override
    public String getCharacterEncoding()
    {
        return this.javax.getCharacterEncoding();
    }

    @Override
    public String getContentType()
    {
        return this.javax.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        return JakartaServletBridge.toJakarta(this.javax.getOutputStream());
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        return this.javax.getWriter();
    }

    @Override
    public void setCharacterEncoding(String charset)
    {
        this.javax.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len)
    {
        this.javax.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len)
    {
        this.javax.setContentLengthLong(len);
    }

    @Override
    public void setContentType(String type)
    {
        this.javax.setContentType(type);
    }

    @Override
    public void setBufferSize(int size)
    {
        this.javax.setBufferSize(size);
    }

    @Override
    public int getBufferSize()
    {
        return this.javax.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException
    {
        this.javax.flushBuffer();
    }

    @Override
    public void resetBuffer()
    {
        this.javax.resetBuffer();
    }

    @Override
    public boolean isCommitted()
    {
        return this.javax.isCommitted();
    }

    @Override
    public void reset()
    {
        this.javax.reset();
    }

    @Override
    public void setLocale(Locale loc)
    {
        this.javax.setLocale(loc);
    }

    @Override
    public Locale getLocale()
    {
        return this.javax.getLocale();
    }

    @Override
    public void addCookie(Cookie cookie)
    {
        this.javax.addCookie(JakartaServletBridge.toJavax(cookie));
    }

    @Override
    public boolean containsHeader(String name)
    {
        return this.javax.containsHeader(name);
    }

    @Override
    public String encodeURL(String url)
    {
        return this.javax.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url)
    {
        return this.javax.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url)
    {
        return this.javax.encodeUrl(url);
    }

    @Override
    public String encodeRedirectUrl(String url)
    {
        return this.javax.encodeRedirectUrl(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        this.javax.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        this.javax.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException
    {
        this.javax.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date)
    {
        this.javax.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date)
    {
        this.javax.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value)
    {
        this.javax.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value)
    {
        this.javax.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value)
    {
        this.javax.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value)
    {
        this.javax.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc)
    {
        this.javax.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm)
    {
        this.javax.setStatus(sc, sm);
    }

    @Override
    public int getStatus()
    {
        return this.javax.getStatus();
    }

    @Override
    public String getHeader(String name)
    {
        return this.javax.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name)
    {
        return this.javax.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames()
    {
        return this.javax.getHeaderNames();
    }
}
