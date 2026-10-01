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
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xwiki.component.manager.ComponentManager;

/**
 * Add back to {@link ComponentAnnotationLoader} the support of the deprecated
 * {@code META-INF/component-overrides.txt} file: each component it lists is registered with the highest priority
 * ({@code 0}).
 *
 * @version $Id$
 * @since 18.9.0RC1
 */
public privileged aspect ComponentAnnotationLoaderOverridesCompatibilityAspect
{
    declare parents : ComponentAnnotationLoader implements CompatibilityComponentAnnotationLoaderConstants;

    /**
     * Merge the components listed in the override files of the classloader into the declarations found in the
     * classloader component list files, before they are registered.
     */
    void around(ComponentAnnotationLoader loader, ComponentManager manager, ClassLoader classLoader,
        List<ComponentDeclaration> componentDeclarations) :
        call(void ComponentAnnotationLoader.initialize(ComponentManager, ClassLoader, List))
        && withincode(void ComponentAnnotationLoader.initialize(ComponentManager, ClassLoader))
        && target(loader) && args(manager, classLoader, componentDeclarations)
    {
        List<ComponentDeclaration> componentOverrideDeclarations;
        try {
            componentOverrideDeclarations = loader.getDeclaredComponents(classLoader,
                CompatibilityComponentAnnotationLoaderConstants.COMPONENT_OVERRIDE_LIST);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        for (ComponentDeclaration componentOverrideDeclaration : componentOverrideDeclarations) {
            // Since the old way to declare an override was to define it in both a component.txt and a
            // component-overrides.txt file we first need to remove the override component declaration stored in
            // componentDeclarations.
            componentDeclarations.remove(componentOverrideDeclaration);
            // Add it to the end of the list with the highest priority.
            componentDeclarations
                .add(new ComponentDeclaration(componentOverrideDeclaration.getImplementationClassName(), 0));
        }

        proceed(loader, manager, classLoader, componentDeclarations);
    }

    /**
     * Read the override file of the JAR too, and merge the components it lists with a priority of {@code 0}.
     */
    List<ComponentDeclaration> around(ComponentAnnotationLoader loader, InputStream jarFile) throws IOException :
        execution(List<ComponentDeclaration> ComponentAnnotationLoader.getDeclaredComponentsFromJAR(InputStream))
        && this(loader) && args(jarFile)
    {
        ZipInputStream zis = new ZipInputStream(jarFile);

        List<ComponentDeclaration> componentDeclarations = null;
        List<ComponentDeclaration> componentOverrideDeclarations = null;

        for (ZipEntry entry = zis.getNextEntry(); entry != null
            && (componentDeclarations == null || componentOverrideDeclarations == null); entry = zis.getNextEntry()) {
            if (entry.getName().equals(ComponentAnnotationLoader.COMPONENT_LIST)) {
                componentDeclarations = loader.getDeclaredComponents(zis);
            } else if (entry.getName()
                .equals(CompatibilityComponentAnnotationLoaderConstants.COMPONENT_OVERRIDE_LIST)) {
                componentOverrideDeclarations = loader.getDeclaredComponents(zis);
            }
        }

        if (componentOverrideDeclarations != null) {
            if (componentDeclarations == null) {
                componentDeclarations = new ArrayList<>();
            }
            for (ComponentDeclaration componentOverrideDeclaration : componentOverrideDeclarations) {
                componentDeclarations
                    .add(new ComponentDeclaration(componentOverrideDeclaration.getImplementationClassName(), 0));
            }
        }

        return componentDeclarations;
    }
}
