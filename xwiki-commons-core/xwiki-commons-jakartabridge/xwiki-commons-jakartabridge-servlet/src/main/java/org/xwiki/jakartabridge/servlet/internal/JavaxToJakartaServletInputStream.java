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

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import org.xwiki.jakartabridge.servlet.ServletBridge;

/**
 * @version $Id$
 * @since jakarta
 */
public class JavaxToJakartaServletInputStream extends ServletInputStream
{
    private final jakarta.servlet.ServletInputStream wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JavaxToJakartaServletInputStream(jakarta.servlet.ServletInputStream wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isFinished()
    {
        return this.wrapped.isFinished();
    }

    @Override
    public boolean isReady()
    {
        return this.wrapped.isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener)
    {
        this.wrapped.setReadListener(ServletBridge.toJakarta(readListener));
    }

    @Override
    public int read() throws IOException
    {
        return this.wrapped.read();
    }

    @Override
    public int readLine(byte[] b, int off, int len) throws IOException
    {
        return this.wrapped.readLine(b, off, len);
    }
}
