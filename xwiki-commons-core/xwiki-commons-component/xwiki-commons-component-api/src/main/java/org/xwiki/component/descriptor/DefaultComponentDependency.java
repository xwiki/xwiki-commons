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
package org.xwiki.component.descriptor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Provider;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Default implementation of {@link ComponentDependency}.
 *
 * @version $Id$
 * @param <T> the type of the component role
 * @since 1.7M1
 */
public class DefaultComponentDependency<T> extends DefaultComponentRole<T> implements ComponentDependency<T>
{
    /**
     * @see #getName()
     */
    private String name;

    /**
     * @see #getHints()
     */
    private String[] hints;

    /**
     * Default constructor.
     */
    public DefaultComponentDependency()
    {
    }

    /**
     * Create a new DefaultComponentDependency by cloning the provided {@link ComponentDependency}.
     *
     * @param dependency the component dependency to clone
     * @since 3.4M1
     */
    public DefaultComponentDependency(ComponentDependency<T> dependency)
    {
        super(dependency);

        setName(dependency.getName());
        if (dependency.getHints() != null) {
            setHints(dependency.getHints().clone());
        }
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String[] getHints()
    {
        return this.hints.clone();
    }

    /**
     * @param name the name of the injection point (can be the name of the field for field injection or the name of the
     *            method for method injection
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param hints a list of hints used when the mapping type is a collection or map so that only component
     *            implementations matching passed hints are injected
     */
    public void setHints(String[] hints)
    {
        this.hints = hints.clone();
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.3M1
     */
    @Override
    public boolean equals(Object object)
    {
        boolean result;

        // See http://www.technofundo.com/tech/java/equalhash.html for the detail of this algorithm.
        if (this == object) {
            result = true;
        } else {
            if (object == null || object.getClass() != getClass()) {
                result = false;
            } else {
                // object must be Syntax at this point
                result = equals((ComponentDependency) object);
            }
        }
        return result;
    }

    /**
     * @param dependency the dependency to compare to
     * @return true if the passed dependency is equals to the current instance or false otherwise
     */
    private boolean equals(ComponentDependency dependency)
    {
        return super.equals(dependency) && Objects.equals(getName(), dependency.getName())
            && Arrays.equals(getHints(), dependency.getHints());
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.3M1
     */
    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.appendSuper(super.hashCode());

        builder.append(getRoleType());
        builder.append(getName());
        builder.append(getHints());

        return builder.toHashCode();
    }

    // deprecated

    @Override
    @Deprecated
    public Class<?> getMappingType()
    {
        return ReflectionUtils.getTypeClass(getRoleType());
    }

    /**
     * @param mappingType the class of the type for the injection (java.lang.String, java.util.List, etc)
     * @deprecated since 4.0M1 use {@link #setRoleType(java.lang.reflect.Type)} instead
     */
    @Deprecated
    public void setMappingType(Class<?> mappingType)
    {
        Type ownerType;
        Type[] parameters;
        if (getRoleType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) getRoleType();
            ownerType = parameterizedType.getOwnerType();
            parameters = parameterizedType.getActualTypeArguments();

            setRoleType(new DefaultParameterizedType(ownerType, mappingType, parameters));
        } else {
            setRoleType(mappingType);
        }
    }

    @Override
    public Class<T> getRole()
    {
        Class mapping = getMappingType();

        if (mapping == List.class || mapping == Collection.class || mapping == Map.class || mapping == Provider.class) {
            return ReflectionUtils.getTypeClass(ReflectionUtils.getLastTypeGenericArgument(getRoleType()));
        } else {
            return mapping;
        }
    }

    @Override
    public void setRole(Class<T> role)
    {
        Class mapping = getMappingType();

        if (mapping == List.class || mapping == Collection.class || mapping == Map.class || mapping == Provider.class) {
            Type ownerType;
            Class<?> rawType;
            if (getRoleType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) getRoleType();
                ownerType = parameterizedType.getOwnerType();
                rawType = (Class<?>) parameterizedType.getRawType();
            } else {
                ownerType = null;
                rawType = mapping;
            }

            setRoleType(new DefaultParameterizedType(ownerType, rawType, role));
        } else {
            super.setRole(role);
        }
    }
}
