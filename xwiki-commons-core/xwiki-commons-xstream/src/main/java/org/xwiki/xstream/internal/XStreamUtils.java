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

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Various XStream related utilities.
 *
 * @version $Id$
 * @since 11.9RC1
 */
@Component(roles = XStreamUtils.class)
@Singleton
public class XStreamUtils
{
    /**
     * Some famous unserializable classes. Fields with this classes are supposed to be made <code>transient</code> in
     * placed that may end up serialized but never too careful...
     */
    private static final List<Class<?>> UNSERIALIZABLE_CLASSES = Arrays.<Class<?>>asList(Logger.class, Provider.class,
        ComponentManager.class, OutputStream.class, InputStream.class);

    @Inject
    private List<XStreamChecker> checkers;

    /**
     * @param obj the value to check
     * @return true if the type serialization cannot fail
     */
    public boolean isSafeType(Object obj)
    {
        return obj == null || obj instanceof String || obj instanceof Number || obj.getClass().isArray()
            || obj instanceof Enum;
    }

    /**
     * @param item the item to serialize
     * @return true if the item is serializable
     */
    public boolean isSerializable(Object item)
    {
        return item == null || isSerializable(item.getClass());
    }

    /**
     * @param field the field to serialize
     * @return true if the field is serializable
     */
    public boolean isSerializable(Field field)
    {
        return isSerializable(field.getType());
    }

    /**
     * @param itemClass the class of the object to serialize
     * @return true of the class describe serializable object
     */
    public boolean isSerializable(Class<?> itemClass)
    {
        for (XStreamChecker checker : this.checkers) {
            Boolean serializable = checker.isSerializable(itemClass);
            if (serializable != null) {
                return serializable;
            }
        }

        if (java.io.Serializable.class.isAssignableFrom(itemClass)) {
            return true;
        }

        // We don't serialize components by default since it does not make sense most of the time
        if (itemClass.isAnnotationPresent(Component.class)) {
            return false;
        }

        // Filter some well known unserializable classes
        for (Class<?> clazz : UNSERIALIZABLE_CLASSES) {
            if (clazz.isAssignableFrom(itemClass)) {
                return false;
            }
        }

        return true;
    }

    public static void serializeField(String name, Class<?> defaultType, Object value, HierarchicalStreamWriter writer,
        MarshallingContext context, Mapper mapper)
    {
        if (value != null) {
            // Start node
            writer.startNode(name);

            Class<?> actualType = value.getClass();

            if (actualType != defaultType) {
                String serializedClassName = mapper.serializedClass(actualType);
                String attributeName = mapper.aliasForSystemAttribute("class");
                if (attributeName != null) {
                    writer.addAttribute(attributeName, serializedClassName);
                }
            }

            // Value
            context.convertAnother(value);

            // End node
            writer.endNode();
        }
    }
}
