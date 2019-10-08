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

import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionProviderWrapper;

/**
 * Wrap the standard {@link ReflectionProvider} to skip to unserializable fields.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
public class SafeReflectionProvider extends ReflectionProviderWrapper
{
    private XStreamUtils utils;

    /**
     * @param provider the standard provider.
     */
    public SafeReflectionProvider(ReflectionProvider provider)
    {
        super(provider);
    }

    /**
     * @param utils the utils to set
     */
    public void setUtils(XStreamUtils utils)
    {
        this.utils = utils;
    }

    @Override
    public Object newInstance(Class type)
    {
        return this.wrapped.newInstance(type);
    }

    @Override
    public void visitSerializableFields(Object object, Visitor visitor)
    {
        this.wrapped.visitSerializableFields(object, (name, type, definedIn, value) -> {
            if (this.utils.isSerializable(type) && this.utils.isSerializable(value)) {
                visitor.visit(name, type, definedIn, value);
            }
        });
    }
}
