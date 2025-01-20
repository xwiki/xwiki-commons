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
import javax.servlet.ServletResponseWrapper;

import org.xwiki.jakartabridge.JavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <R> the type of http servlet response
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JavaxToJakartaServletResponseWrapper<R extends jakarta.servlet.ServletResponseWrapper>
    extends ServletResponseWrapper implements JavaxToJakartaWrapper<R>
{
    protected final R jakarta;

    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaServletResponseWrapper(R jakarta)
    {
        super(JakartaServletBridge.toJavax(jakarta.getResponse()));

        this.jakarta = jakarta;
    }

    // JavaxToJakartaWrapper

    /**
     * @return the jakarta version
     */
    @Override
    public R getJakarta()
    {
        return this.jakarta;
    }

    // ServletResponseWrapper

    @Override
    public void setResponse(ServletResponse response)
    {
        super.setResponse(response);

        this.jakarta.setResponse(JakartaServletBridge.toJakarta(response));
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
}
