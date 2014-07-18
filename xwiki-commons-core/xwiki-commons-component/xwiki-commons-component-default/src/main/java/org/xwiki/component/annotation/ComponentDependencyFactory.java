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

import java.lang.reflect.Field;

import org.xwiki.component.descriptor.ComponentDependency;

/**
 * Allows creating {@link ComponentDependency} instances from a Method {@link Field}. There can be several
 * implementations, for example one using {@code @Inject} and {@code @Named} annotations to describe a Component
 * Dependency. Note that this interface allows cleanly plugging new implementations to extend how Component Descriptor
 * are recognized. This allows for example to cleanly support legacy implementation (for example to use the old
 * {@code @Requirement} annotations we used to use).
 *
 * @version $Id$
 * @since 3.2RC1
 */
public interface ComponentDependencyFactory
{
    /**
     * @param field the field for which to extract a Component Dependency
     * @return the Component Dependency instance created from the passed field or null if the field doesn't match the
     *         requirements
     */
    ComponentDependency createComponentDependency(Field field);
}
