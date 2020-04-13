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
package org.xwiki.context.internal;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

/**
 * Contains the property value and metadata that governs the maintenance of the property.
 *
 * This class may require ReflectPermission suppressAccessChecks to clone the object value.
 *
 * @version $Id$
 * @since 4.3M1
 */
public class ExecutionContextProperty implements Cloneable
{
    /** The key that is the name of this property in the execution context. */
    private final String key;

    /** This is the actual property value. */
    private Object value;

    /**
     * Clone the value when this property is cloned.
     */
    private final boolean cloneValue;

    /** Controls whether the value of this property is final. */
    private final boolean isFinal;

    /**
     * Controls whether this property should be inherited across execution contexts. It will be inherited as long as an
     * execution context containing the property is the current execution context. It will not be propagated to parent
     * execution contexts. Hence, it may be removed by a call to popExecutionContext.
     */
    private final boolean inherited;

    /** Indicate that the value may not be {@code null}. */
    private final boolean nonNull;

    /**
     * @see #getType()
     */
    private final Class<?> type;

    /** @see #isClonedFrom(ExecutionContextProperty) */
    private WeakReference<ExecutionContextProperty> clonedFrom;

    /**
     * @param key The execution context key.
     * @param initialValue The initial value.
     * @param cloneValue Indicate that the value should be cloned when the property is cloned.
     * @param isFinal Indicate that the value may not be updated from the initial value.
     * @param inherited Indicate that the property should be inherited when activating a new execution context.
     * @param nonNull Indicate that the property value may not be {@literal null}.
     * @param type Set a class which the value must be assignable to.
     */
    public ExecutionContextProperty(String key, Object initialValue, boolean cloneValue, boolean isFinal,
        boolean inherited, boolean nonNull, Class<?> type)
    {
        this.key = key;
        this.value = initialValue;
        this.cloneValue = cloneValue;
        this.isFinal = isFinal;
        this.inherited = inherited;
        this.nonNull = nonNull;
        this.type = type;
        checkValue(initialValue);
    }

    /**
     * Check that the value is compatible with the configure constraints.
     *
     * @param value The value.
     * @throws IllegalArgumentException if the value is null and this property has the nonNull attribute set, or if the
     *             type is set for this value, but the value is not assignable to the set type.
     */
    private void checkValue(Object value)
    {
        if (this.nonNull && value == null) {
            throw new IllegalArgumentException(String.format("The property [%s] may not be null!", getKey()));
        }
        if (getType() != null && value != null && !getType().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException(
                String.format("The value of property [%s] must be of type [%s], but was [%s]",
                    getKey(), getType(), value.getClass()));
        }
    }

    /**
     * @param value The object value.
     * @throws IllegalArgumentException if the value is null and this property has the nonNull attribute set, or if the
     *             type is set for this value, but the value is not assignable to the set type.
     */
    public void setValue(Object value)
    {
        checkValue(value);
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

    /** @return wether this property is final or not. */
    public boolean isFinal()
    {
        return this.isFinal;
    }

    /** @return wether this property should be inherited across execution contexts or not. */
    public boolean isInherited()
    {
        return this.inherited;
    }

    /**
     * @return the type of the value
     */
    private Class<?> getType()
    {
        return this.type;
    }

    @Override
    public ExecutionContextProperty clone()
    {
        ExecutionContextProperty clone;

        Object clonedValue;

        if (this.cloneValue) {
            try {
                clonedValue = getValue().getClass().getMethod("clone").invoke(getValue());
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(String.format(
                    "cloneValue attribute was set on property [%s], "
                        + "but the value had class [%s] which has no public clone method", getKey(),
                    getValue().getClass().getName()));
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            clonedValue = getValue();
        }

        clone = new ExecutionContextProperty(getKey(), clonedValue, this.cloneValue, isFinal(), isInherited(),
            this.nonNull, getType());

        if (isFinal() && isInherited()) {
            // We make this a weak reference, because we are only interested in it as long
            // as it is references by the current execution co
            clone.clonedFrom = new WeakReference<>(this);
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
    public boolean isClonedFrom(ExecutionContextProperty property)
    {
        return this.clonedFrom != null && this.clonedFrom.get() == property;
    }
}
