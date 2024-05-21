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
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

import org.xwiki.jakartabridge.servlet.ServletBridge;

/**
 * @param <R> the type of servlet response
 * @version $Id$
 * @since -1.jakarta
 */
public class JavaxToJakartaServletResponse<R extends jakarta.servlet.ServletResponse> implements ServletResponse
{
    protected final R wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JavaxToJakartaServletResponse(R wrapped)
    {
        this.wrapped = wrapped;
    }

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
        return ServletBridge.toJavax(this.wrapped.getOutputStream());
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

}
