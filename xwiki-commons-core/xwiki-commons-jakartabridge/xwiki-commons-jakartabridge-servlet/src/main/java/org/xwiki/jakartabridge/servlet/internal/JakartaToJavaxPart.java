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
import java.io.InputStream;
import java.util.Collection;

import jakarta.servlet.http.Part;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JakartaToJavaxPart implements Part
{
    private final javax.servlet.http.Part wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JakartaToJavaxPart(javax.servlet.http.Part wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return this.wrapped.getInputStream();
    }

    @Override
    public String getContentType()
    {
        return this.wrapped.getContentType();
    }

    @Override
    public String getName()
    {
        return this.wrapped.getName();
    }

    @Override
    public String getSubmittedFileName()
    {
        return this.wrapped.getSubmittedFileName();
    }

    @Override
    public long getSize()
    {
        return this.wrapped.getSize();
    }

    @Override
    public void write(String fileName) throws IOException
    {
        this.wrapped.write(fileName);
    }

    @Override
    public void delete() throws IOException
    {
        this.wrapped.delete();
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
