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

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.DefaultComponentDependency;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Uses {@link javax.inject.Inject} and {@link javax.inject.Named} annotations to recognize a Component Dependency.
 * 
 * @version $Id$
 * @since 3.2RC1
 */
public class DefaultComponentDependencyFactory extends AbstractComponentDependencyFactory
{
    @Override
    public ComponentDependency createComponentDependency(Field field)
    {
        DefaultComponentDependency dependency;

        Inject inject = field.getAnnotation(Inject.class);
        if (inject != null) {
            dependency = new DefaultComponentDependency();

            Class< ? > fieldClass = field.getType();
            if (ReflectionUtils.getDirectAnnotation(ComponentRole.class, fieldClass) != null
                && ReflectionUtils.getDirectAnnotation(Role.class, fieldClass) == null) {
                // since 4.0M1, retro-compatibility (generic type used to not be taken into account)
                dependency.setRoleType(fieldClass);
            } else {
                dependency.setRoleType(field.getGenericType());
            }

            dependency.setName(field.getName());

            // Look for a Named annotation
            Named named = field.getAnnotation(Named.class);
            if (named != null) {
                dependency.setRoleHint(named.value());
            }
        } else {
            dependency = null;
        }

        return dependency;
    }
}
