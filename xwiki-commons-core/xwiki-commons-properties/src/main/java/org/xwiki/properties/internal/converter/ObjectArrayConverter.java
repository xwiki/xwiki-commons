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
package org.xwiki.properties.internal.converter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.Converter;
import org.xwiki.stability.Unstable;

/**
 * Converter for Object array type, since it's not supported by Apache Bean Utils.
 * <p>
 * Mostly relies on {@link ListConverter} for the String syntax.
 * 
 * @version $Id$
 * @since 18.4.0RC1
 * @since 17.10.9
 */
@Component
@Singleton
@Unstable
public class ObjectArrayConverter extends AbstractConverter<Object[]>
{
    @Inject
    private Converter<List> listConverter;

    @Override
    @SuppressWarnings("unchecked")
    protected Object[] convertToType(Type targetType, Object value)
    {
        if (value instanceof Iterable<?> iterableValue) {
            return fromIterable(iterableValue);
        } else {
            List<Object> list = this.listConverter.convert(List.class, value);
            return list != null ? list.toArray() : null;
        }
    }

    private Object[] fromIterable(Iterable<?> values)
    {
        List<Object> collection = new ArrayList<>();

        for (Object value : values) {
            collection.add(value);
        }

        return collection.toArray();
    }

    @Override
    protected String convertToString(Object[] value)
    {
        return this.listConverter.convert(String.class, List.of(value));
    }
}
