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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jakarta.inject.Provider;

import org.xwiki.component.manager.ComponentManager;

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

    /**
     * Location in the classloader of the file specifying which component implementation to use when several components
     * with the same role/hint are found.
     *
     * @deprecated starting with 3.3M1 use the notion of priorities instead (see {@link ComponentDeclaration}).
     */
    @Deprecated
    public static final String ComponentAnnotationLoader.COMPONENT_OVERRIDE_LIST = "META-INF/component-overrides.txt";


    private pointcut initializePointcut(ComponentAnnotationLoader cal, ComponentManager manager,
        ClassLoader classLoader):
        this(cal) && call(void ComponentAnnotationLoader.initialize(ComponentManager, ClassLoader)) && args(manager, classLoader);

    void around(ComponentAnnotationLoader cal, ComponentManager manager, ClassLoader classLoader):
        initializePointcut(cal, manager, classLoader){
        try {
            // Find all declared components by retrieving the list defined in COMPONENT_LIST.
            List<ComponentDeclaration> componentDeclarations =
                cal.getDeclaredComponents(classLoader,
                    ComponentAnnotationLoader.COMPONENT_LIST);

            // Find all the Component overrides and adds them to the bottom of the list as component declarations with
            // the highest priority of 0. This is purely for backward compatibility since the override files is now
            // deprecated.
            List<ComponentDeclaration> componentOverrideDeclarations =
                cal.getDeclaredComponents(classLoader, ComponentAnnotationLoader.COMPONENT_OVERRIDE_LIST);
            for (ComponentDeclaration componentOverrideDeclaration : componentOverrideDeclarations) {
                // Since the old way to declare an override was to define it in both a component.txt and a
                // component-overrides.txt file we first need to remove the override component declaration stored in
                // componentDeclarations.
                componentDeclarations.remove(componentOverrideDeclaration);
                // Add it to the end of the list with the highest priority.
                componentDeclarations.add(new ComponentDeclaration(componentOverrideDeclaration
                    .getImplementationClassName(), 0));
            }

            cal.initialize(manager, classLoader, componentDeclarations);
        } catch (Exception e) {
            // Make sure we make the calling code fail in order to fail fast and prevent the application to start
            // if something is amiss.
            throw new RuntimeException("Failed to get the list of components to load", e);
        }

    }

    private pointcut getDeclaredComponentsFromJARPointcut(ComponentAnnotationLoader cal, InputStream jarFile) :
        this(cal) && call(List<ComponentDeclaration> ComponentAnnotationLoader.getDeclaredComponentsFromJAR(InputStream)) && args(jarFile);

    List<ComponentDeclaration> around(ComponentAnnotationLoader cal,  InputStream jarFile) throws IOException:
        getDeclaredComponentsFromJARPointcut(cal, jarFile) {
        ZipInputStream zis = new ZipInputStream(jarFile);

        List<ComponentDeclaration> componentDeclarations = null;
        List<ComponentDeclaration> componentOverrideDeclarations = null;

        for (ZipEntry entry = zis.getNextEntry(); entry != null
            && (componentDeclarations == null || componentOverrideDeclarations == null); entry = zis.getNextEntry()) {
            if (entry.getName().equals(ComponentAnnotationLoader.COMPONENT_LIST)) {
                componentDeclarations = cal.getDeclaredComponents(zis);
            } else if (entry.getName().equals(ComponentAnnotationLoader.COMPONENT_OVERRIDE_LIST)) {
                componentOverrideDeclarations = cal.getDeclaredComponents(zis);
            }
        }

        // Merge all overrides found with a priority of 0. This is purely for backward compatibility since the
        // override files is now deprecated.
        if (componentOverrideDeclarations != null) {
            if (componentDeclarations == null) {
                componentDeclarations = new ArrayList<>();
            }
            for (ComponentDeclaration componentOverrideDeclaration : componentOverrideDeclarations) {
                componentDeclarations.add(new ComponentDeclaration(componentOverrideDeclaration
                    .getImplementationClassName(), 0));
            }
        }

        return componentDeclarations;

    }
}
