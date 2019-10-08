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
package org.xwiki.xstream.internal;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.WriterWrapper;

/**
 * Help tracking and closing forgotten ending tags.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
public class SafeWriter extends WriterWrapper
{
    private int depth;

    /**
     * @param writer the actual writer
     */
    public SafeWriter(HierarchicalStreamWriter writer)
    {
        super(writer);
    }

    @Override
    public void startNode(String name)
    {
        super.startNode(name);

        ++this.depth;
    }

    @Override
    public void startNode(String name, Class clazz)
    {
        super.startNode(name, clazz);

        ++this.depth;
    }

    @Override
    public void endNode()
    {
        super.endNode();

        --this.depth;
    }

    /**
     * Close any open tag in case of error in the middle of the serialization.
     */
    public void fix()
    {
        while (this.depth > 0) {
            endNode();
        }
    }
}
