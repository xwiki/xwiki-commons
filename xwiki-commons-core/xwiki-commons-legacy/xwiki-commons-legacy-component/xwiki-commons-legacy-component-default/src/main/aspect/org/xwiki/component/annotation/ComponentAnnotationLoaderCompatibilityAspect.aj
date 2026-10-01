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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.inject.Provider;

/**
 * Add a backward compatibility layer to the {@link ComponentAnnotationLoader} class.
 *
 * @version $Id$
 * @since 18.9.0RC1
 */
public privileged aspect ComponentAnnotationLoaderCompatibilityAspect
{
    /**
     * Finds the interfaces that implement component roles by looking recursively in all interfaces of the passed
     * component implementation class. If the roles annotation value is specified then use the specified list instead of
     * doing auto-discovery. Also note that we support component classes implementing JSR 330's
     * {@link javax.inject.Provider} (and thus without a component role annotation).
     *
     * @param componentClass the component implementation class for which to find the component roles it implements
     * @return the list of component role classes implemented
     * @deprecated use {@link ComponentAnnotationLoader#findComponentRoleTypes(Class)} instead
     */
    @Deprecated(since = "4.0M1")
    public Set<Class<?>> ComponentAnnotationLoader.findComponentRoleClasses(Class<?> componentClass)
    {
        // Note: We use a Set to ensure that we don't register duplicate roles.
        Set<Class<?>> classes = new LinkedHashSet<>();

        Component component = componentClass.getAnnotation(Component.class);
        if (component != null && component.roles().length > 0) {
            classes.addAll(Arrays.asList(component.roles()));
        } else {
            // Look in both superclass and interfaces for @Role or javax.inject.Provider
            for (Class<?> interfaceClass : componentClass.getInterfaces()) {
                // Handle superclass of interfaces
                classes.addAll(findComponentRoleClasses(interfaceClass));

                // Handle interfaces directly declared in the passed component class
                for (Annotation annotation : interfaceClass.getDeclaredAnnotations()) {
                    if (annotation.annotationType() == ComponentRole.class) {
                        classes.add(interfaceClass);
                    }
                }

                // Handle javax.inject.Provider (retro-compatibility since 17.0.0RC1)
                if (javax.inject.Provider.class.isAssignableFrom(interfaceClass)) {
                    classes.add(interfaceClass);
                }
                // Handle jakarta.inject.Provider
                if (Provider.class.isAssignableFrom(interfaceClass)) {
                    classes.add(interfaceClass);
                }
            }

            // Note that we need to look into the superclass since the super class can itself implements an interface
            // that has the @Role annotation.
            Class<?> superClass = componentClass.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                classes.addAll(findComponentRoleClasses(superClass));
            }
        }

        return classes;
    }
}
