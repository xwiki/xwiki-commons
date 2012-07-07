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
package org.xwiki.component.annotation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a Component declaration (i.e. component implementation class name and component priority).
 * 
 * @version $Id$
 * @since 3.3M1
 */
public class ComponentDeclaration
{
    /**
     * Default priorities under which components are registered when no explicit priority is defined.
     */
    private static final int DEFAULT_PRIORITY = 1000;

    /**
     * @see #getPriority()
     */
    private int priority;

    /**
     * @see #getImplementationClassName()
     */
    private String implementationClassName;

    /**
     * @param implementationClassName see {@link #getImplementationClassName()}
     * @since 4.2M1
     */
    public ComponentDeclaration(String implementationClassName)
    {
        this(implementationClassName, DEFAULT_PRIORITY);
    }

    /**
     * @param implementationClassName see {@link #getImplementationClassName()}
     * @param priority see {@link #getPriority()}
     */
    public ComponentDeclaration(String implementationClassName, int priority)
    {
        this.implementationClassName = implementationClassName;
        this.priority = priority;
    }

    /**
     * @return the priority this component declaration has (when several components are registered for the same Role and
     *         Hint the priority is used to decide which one gets registered - the lowest value wins)
     */
    public int getPriority()
    {
        return this.priority;
    }

    /**
     * @return the component implementation class name for the declared component
     */
    public String getImplementationClassName()
    {
        return this.implementationClassName;
    }

    // Object

    @Override
    public boolean equals(Object object)
    {
        if (object == null) {
            return false;
        }

        if (object == this) {
            return true;
        }

        if (object.getClass() != getClass()) {
            return false;
        }

        ComponentDeclaration rhs = (ComponentDeclaration) object;

        return new EqualsBuilder().append(getImplementationClassName(), rhs.getImplementationClassName())
            .append(getPriority(), rhs.getPriority()).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(5, 45).append(getImplementationClassName()).append(getPriority()).toHashCode();
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.priority) + ':' + this.implementationClassName;
    }
}
