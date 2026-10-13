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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Unit tests proving that we keep backward compatibility for the {@code META-INF/component-overrides.txt} file
 * supported by {@link ComponentAnnotationLoader}.
 *
 * @version $Id$
 */
class ComponentAnnotationLoaderTest
{
    private static final String COMPONENT_HINT = "test";

    @Role
    public interface TestRole
    {
    }

    @Component
    @Named(COMPONENT_HINT)
    @Singleton
    public static class PrioritizedRoleImpl implements TestRole
    {
    }

    @Component
    @Named(COMPONENT_HINT)
    @Singleton
    public static class OverrideRoleImpl implements TestRole
    {
    }

    /**
     * Without the override file, {@link PrioritizedRoleImpl} would win since its declared priority (500) is higher
     * than the default one of {@link OverrideRoleImpl}.
     */
    @Test
    void initializeWithComponentOverrides() throws Exception
    {
        URL overridesRoot = getClass().getClassLoader().getResource("overrides/");
        try (URLClassLoader classLoader =
            new URLClassLoader(new URL[] { overridesRoot }, getClass().getClassLoader())) {
            // Initialize through the component manager to make sure the backward compatibility applies when
            // ComponentAnnotationLoader#initialize is called from another class.
            EmbeddableComponentManager componentManager = new EmbeddableComponentManager();
            componentManager.initialize(classLoader);

            assertInstanceOf(OverrideRoleImpl.class, componentManager.getInstance(TestRole.class, COMPONENT_HINT));
            assertEquals(0,
                componentManager.getComponentDescriptor(TestRole.class, COMPONENT_HINT).getRoleHintPriority());
        }
    }

    @Test
    void getDeclaredComponentsFromJARWithComponentOverrides() throws Exception
    {
        ByteArrayOutputStream jar = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(jar)) {
            addEntry(zos, ComponentAnnotationLoader.COMPONENT_LIST);
            addEntry(zos, CompatibilityComponentAnnotationLoaderConstants.COMPONENT_OVERRIDE_LIST);
        }

        List<ComponentDeclaration> declarations = new ComponentAnnotationLoader()
            .getDeclaredComponentsFromJAR(new ByteArrayInputStream(jar.toByteArray()));

        assertEquals(List.of(
            declaration(OverrideRoleImpl.class, ComponentDescriptor.DEFAULT_PRIORITY),
            declaration(PrioritizedRoleImpl.class, 500),
            declaration(OverrideRoleImpl.class, 0)), declarations.stream().map(ComponentDeclaration::toString).toList());
    }

    private void addEntry(ZipOutputStream zos, String path) throws Exception
    {
        zos.putNextEntry(new ZipEntry(path));
        try (InputStream content = getClass().getClassLoader().getResourceAsStream("overrides/" + path)) {
            content.transferTo(zos);
        }
        zos.closeEntry();
    }

    private String declaration(Class<?> implementation, int priority)
    {
        return new ComponentDeclaration(implementation.getName(), priority).toString();
    }
}
