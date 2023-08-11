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

import javax.servlet.http.Part;

import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaPart extends AbstractJavaxToJakartaWrapper<jakarta.servlet.http.Part> implements Part
{
    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaPart(jakarta.servlet.http.Part jakarta)
    {
        super(jakarta);
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return this.jakarta.getInputStream();
    }

    @Override
    public String getContentType()
    {
        return this.jakarta.getContentType();
    }

    @Override
    public String getName()
    {
        return this.jakarta.getName();
    }

    @Override
    public String getSubmittedFileName()
    {
        return this.jakarta.getSubmittedFileName();
    }

    @Override
    public long getSize()
    {
        return this.jakarta.getSize();
    }

    @Override
    public void write(String fileName) throws IOException
    {
        this.jakarta.write(fileName);
    }

    @Override
    public void delete() throws IOException
    {
        this.jakarta.delete();
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
