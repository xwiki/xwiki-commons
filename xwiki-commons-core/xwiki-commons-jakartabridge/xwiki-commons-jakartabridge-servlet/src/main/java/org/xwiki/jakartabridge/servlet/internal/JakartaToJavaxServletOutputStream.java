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

import org.xwiki.jakartabridge.servlet.ServletBridge;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

/**
 * @version $Id$
 * @since 17-jakarta
 */
public class JakartaToJavaxServletOutputStream extends ServletOutputStream
{
    private final javax.servlet.ServletOutputStream wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JakartaToJavaxServletOutputStream(javax.servlet.ServletOutputStream wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isReady()
    {
        return this.wrapped.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener)
    {
        this.wrapped.setWriteListener(ServletBridge.toJavax(writeListener));
    }

    @Override
    public void write(int b) throws IOException
    {
        this.wrapped.write(b);
    }

    @Override
    public void print(String s) throws IOException
    {
        this.wrapped.print(s);
    }

    @Override

    public void print(boolean b) throws IOException
    {
        this.wrapped.print(b);
    }

    @Override
    public void print(char c) throws IOException
    {
        this.wrapped.print(c);
    }

    @Override

    public void print(int i) throws IOException
    {
        this.wrapped.print(i);
    }

    @Override
    public void print(long l) throws IOException
    {
        this.wrapped.print(l);
    }

    @Override
    public void print(float f) throws IOException
    {
        this.wrapped.print(f);
    }

    @Override

    public void print(double d) throws IOException
    {
        this.wrapped.print(d);
    }

    @Override
    public void println() throws IOException
    {
        this.wrapped.println();
    }

    @Override
    public void println(String s) throws IOException
    {
        this.wrapped.println(s);
    }

    @Override
    public void println(boolean b) throws IOException
    {
        this.wrapped.println(b);
    }

    @Override
    public void println(char c) throws IOException
    {
        this.wrapped.println(c);
    }

    @Override

    public void println(int i) throws IOException
    {
        this.wrapped.println(i);
    }

    @Override

    public void println(long l) throws IOException
    {
        this.wrapped.println(l);
    }

    @Override
    public void println(float f) throws IOException
    {
        this.wrapped.println(f);
    }

    @Override
    public void println(double d) throws IOException
    {
        this.wrapped.println(d);
    }
}
