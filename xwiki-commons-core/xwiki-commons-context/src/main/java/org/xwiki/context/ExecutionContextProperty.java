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
package org.xwiki.context;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

/**
 * Contains the property value and metadata that governs the maintainance of the property.
 *
 * This class may require ReflectPermission suppressAccessChecks to clone the object value.
 *
 * @version $Id$ 
 * @since 4.3M1
 */
public class ExecutionContextProperty implements Cloneable
{
    /** The key that is the name of this property in the execution context. */
    private String key;

    /** This is the actual property value.  */
    private Object value;

    /**
     * Clone the value when this property is cloned.
     *
     * ReflectPermission suppressAccessChecks may be required.
     */
    private boolean cloneValue;

    /** Controls whether this property is read-only. */
    private boolean readonly;

    /**
     *  Controls whether this property should be inherited across execution contexts.  It will be inherited as long as
     *  an execution context containing the property is the current execution context.  It will not be propagated to
     *  parent execution contexts.  Hence, it may be removed by a call to popExecutionContext.
     */
    private boolean inherited;

    /** Indicate that the value may not be {@code null}. */
    private boolean nonNull;

    /** The type of the value. */
    private Class<?> type;

    /** @see isClonedFrom(ExecutionContextProperty property). */
    private WeakReference<ExecutionContextProperty> clonedFrom;

    /**
     * @param key The execution context key.
     */
    public ExecutionContextProperty(String key)
    {
        this.key = key;
    }

    /**
     * @param value The object value.
     * @throws IllegalArgumentException if the value is null and this property has the nonNull attribute set, or if the
     * type is set for this value, but the value is not assignable to the set type.
     */
    public void setValue(Object value)
    {
        if (nonNull && value == null) {
            throw new IllegalArgumentException(String.format("The property [%s] may not be null!", key));
        }
        if (type != null && !type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException(
                String.format("The value of property [%s] must be of type [%s], but was [%s]",
                              key, type, value.getClass()));
        }
        this.value = value;
    }

    /** @return The object value. */
    public Object getValue()
    {
        return this.value;
    }

    /** @return The key of this property. */
    public String getKey()
    {
        return this.key;
    }

    /** @param cloneValue Set wether the value should be cloned when this property is cloned. */
    public void setCloneValue(boolean cloneValue)
    {
        this.cloneValue = cloneValue;
    }

    /** @param readonly Set wether the property is read-only or not. */
    public void setReadonly(boolean readonly)
    {
        this.readonly = readonly;
    }

    /** @return wether this property is read-only or not. */
    public boolean isReadonly()
    {
        return this.readonly;
    }

    /** @param inherited Set wether this property should be inherited across execution contexts or not. */
    public void setInherited(boolean inherited)
    {
        this.inherited = inherited;
    }

    /** @return wether this property should be inherited across execution contexts or not. */
    public boolean isInherited()
    {
        return this.inherited;
    }

    /** @param nonNull Indicate wether it is an error if th value of this property is set to {@code null} or not. */
    public void setNonNull(boolean nonNull)
    {
        this.nonNull = nonNull;
    }

    /** @param type The type of this property's value.  If set, the value will be typechecked when set. */
    public void setType(Class<?> type)
    {
        this.type = type;
    }

    @Override
    public ExecutionContextProperty clone()
    {
        ExecutionContextProperty clone;
        try {
            clone = (ExecutionContextProperty) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        if (cloneValue) {
            try {
                clone.value = value.getClass().getMethod("clone").invoke(value);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(String.format(
                     "cloneValue attribute was set on property [%s], "
                     + "but the value had class [%s] which has no public clone method", key,
                     value.getClass().getName()));
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            clone.value = value;
        }
        clone.key        = key;
        clone.cloneValue = cloneValue;
        clone.readonly   = readonly;
        clone.inherited  = inherited;
        clone.nonNull    = nonNull;
        clone.type       = type;
        if (readonly && inherited) {
            // We make this a weak reference, because we are only interested in it as long
            // as it is references by the current execution co
            clone.clonedFrom = new WeakReference<ExecutionContextProperty>(this);
        }

        return clone;
    }

    /**
     * Check that this instance was cloned from the specified instance.
     *
     * When aggressively enforcing the inheritance of a property p, it will be disallowed to inherit from the execution
     * context containing p if the inheriting context contains property p' with the same key as p, unless p' was cloned
     * from p.
     *
     * @param property The original property.
     * @return If the return value is {@code true}, then this property was cloned from the given property.
     */
    boolean isClonedFrom(ExecutionContextProperty property)
    {
        return clonedFrom != null && clonedFrom.get() == property;
    }
    
}
