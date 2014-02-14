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
package org.xwiki.job.internal.xstream;

import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * {@link ExtendedHierarchicalStreamWriter} doing nothing.
 * 
 * @version $Id$
 * @since 5.4M1
 */
public final class VoidWriter implements ExtendedHierarchicalStreamWriter
{
    /**
     * Unique instance of {@link VoidWriter}.
     */
    public static final VoidWriter WRITER = new VoidWriter();

    private VoidWriter()
    {
        // Unique instance
    }

    @Override
    public void startNode(String name)
    {
        // Ignore
    }

    @Override
    public void addAttribute(String name, String value)
    {
        // Ignore
    }

    @Override
    public void setValue(String text)
    {
        // Ignore
    }

    @Override
    public void endNode()
    {
        // Ignore
    }

    @Override
    public void flush()
    {
        // Ignore
    }

    @Override
    public void close()
    {
        // Ignore
    }

    @Override
    public HierarchicalStreamWriter underlyingWriter()
    {
        return this;
    }

    @Override
    public void startNode(String name, Class clazz)
    {
        // Ignore
    }
}
