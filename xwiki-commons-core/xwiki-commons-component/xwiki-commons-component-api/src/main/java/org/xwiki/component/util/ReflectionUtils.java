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
package org.xwiki.component.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Various Reflection utilities.
 * 
 * @version $Id$
 * @since 2.1RC1
 */
public final class ReflectionUtils
{
    /**
     * Utility class.
     */
    private ReflectionUtils()
    {
        // Utility class
    }

    /**
     * @param componentClass the class for which to return all fields
     * @return all fields declared by the passed class and its superclasses
     */
    public static Collection<Field> getAllFields(Class< ? > componentClass)
    {
        // Note: use a linked hash map to keep the same order as the one used to declare the fields.
        Map<String, Field> fields = new LinkedHashMap<String, Field>();
        Class< ? > targetClass = componentClass;
        while (targetClass != null) {
            Field[] componentClassFields;
            try {
                componentClassFields = targetClass.getDeclaredFields();
            } catch (NoClassDefFoundError e) {
                // Provide a better exception message to more easily debug component loading issue.
                // Specifically with this error message we'll known which component failed to be initialized.
                throw new NoClassDefFoundError("Failed to get fields for class [" + targetClass.getName()
                    + "] because the class [" + e.getMessage() + "] couldn't be found in the ClassLoader.");
            }
            
            for (Field field : componentClassFields) {
                // Make sure that if the same field is declared in a class and its superclass
                // only the field used in the class will be returned. Note that we need to do
                // this check since the Field object doesn't implement the equals method using
                // the field name.
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        return fields.values();
    }

    /**
     * Sets a value to a field using reflection even if the field is private.
     * 
     * @param instanceContainingField the object containing the field
     * @param fieldName the name of the field in the object
     * @param fieldValue the value to set for the provided field
     */
    public static void setFieldValue(Object instanceContainingField, String fieldName, Object fieldValue)
    {
        // Find the class containing the field to set
        Class< ? > targetClass = instanceContainingField.getClass();
        while (targetClass != null) {
            for (Field field : targetClass.getDeclaredFields()) {
                if (field.getName().equalsIgnoreCase(fieldName)) {
                    try {
                        boolean isAccessible = field.isAccessible();
                        try {
                            field.setAccessible(true);
                            field.set(instanceContainingField, fieldValue);
                        } finally {
                            field.setAccessible(isAccessible);
                        }
                    } catch (Exception e) {
                        // This shouldn't happen but if it does then the Component manager will not function properly
                        // and we need to abort. It probably means the Java security manager has been configured to
                        // prevent accessing private fields.
                        throw new RuntimeException("Failed to set field [" + fieldName + "] in instance of ["
                            + instanceContainingField.getClass().getName() + "]. The Java Security Manager has "
                            + "probably been configured to prevent settting private field values. XWiki requires "
                            + "this ability to work.", e);
                    }
                    return;
                }
            }
            targetClass = targetClass.getSuperclass();
        }
    }

    /**
     * Extract the last generic type from the passed field. For example {@code private List&lt;A, B&gt; field}
     * would return the {@code B} class.
     *
     * @param field the field from which to extract the generic type
     * @return the class of the last generic type of null if the field doesn't have a generic type
     */
    public static Class<?> getLastGenericFieldType(Field field)
    {
        Type type = field.getGenericType();

        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type[] types = pType.getActualTypeArguments();
            if (types.length > 0 && types[types.length - 1] instanceof Class) {
                return (Class) types[types.length - 1];
            }
        }

        return null;
    }

    /**
     * Extract the last generic type from the passed class. For example
     * {@code public Class MyClass implements FilterClass&lt;A, B&gt;, SomeOtherClass&lt;C&gt;} will return {@code B}.
     *
     * @param clazz the class to extract from
     * @param filterClass the class of the generic type we're looking for
     * @return the last generic type from the interfaces of the passed class, filtered by the passed filter class
     */
    public static Class<?> getLastGenericClassType(Class clazz, Class filterClass)
    {
        // Get all interfaces implemented and find the one that's a Provider with a Generic type
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                if (filterClass.isAssignableFrom((Class) pType.getRawType())) {
                    Type[] actualTypes = pType.getActualTypeArguments();
                    if (actualTypes.length > 0 && actualTypes[actualTypes.length - 1] instanceof Class) {
                        return (Class) actualTypes[actualTypes.length - 1];
                    }
                }
            }
        }

        return null;
    }
}
