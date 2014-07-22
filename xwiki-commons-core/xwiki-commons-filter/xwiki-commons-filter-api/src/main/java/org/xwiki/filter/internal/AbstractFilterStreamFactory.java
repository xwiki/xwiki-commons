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
package org.xwiki.filter.internal;

import org.xwiki.filter.FilterStreamFactory;
import org.xwiki.filter.descriptor.FilterStreamDescriptor;
import org.xwiki.filter.type.FilterStreamType;

/**
 * 
 * @version $Id$
 * @since 6.2M1
 */
public abstract class AbstractFilterStreamFactory implements FilterStreamFactory
{
    protected final FilterStreamType type;

    protected FilterStreamDescriptor descriptor;

    public AbstractFilterStreamFactory(FilterStreamType type)
    {
        this.type = type;
    }

    @Override
    public FilterStreamType getType()
    {
        return this.type;
    }

    @Override
    public FilterStreamDescriptor getDescriptor()
    {
        return this.descriptor;
    }

    protected void setDescriptor(FilterStreamDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }
}
