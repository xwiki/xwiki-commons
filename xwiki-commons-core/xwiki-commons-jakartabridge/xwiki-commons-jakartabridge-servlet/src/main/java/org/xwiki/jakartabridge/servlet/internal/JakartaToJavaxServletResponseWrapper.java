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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletResponseWrapper;

import org.xwiki.jakartabridge.JakartaToJavaxWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <R> the type of http servlet response
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaToJavaxServletResponseWrapper<R extends javax.servlet.ServletResponseWrapper>
    extends ServletResponseWrapper implements JakartaToJavaxWrapper<R>
{
    protected final R javax;

    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxServletResponseWrapper(R javax)
    {
        super(JakartaServletBridge.toJakarta(javax.getResponse()));

        this.javax = javax;
    }

    // JakartaToJavaxWrapper

    @Override
    public R getJavax()
    {
        return this.javax;
    }

    // ServletResponse

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
}
