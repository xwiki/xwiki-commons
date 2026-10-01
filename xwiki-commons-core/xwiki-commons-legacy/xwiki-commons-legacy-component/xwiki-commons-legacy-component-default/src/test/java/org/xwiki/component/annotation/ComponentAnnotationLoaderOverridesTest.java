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
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Validate the support of the deprecated {@code META-INF/component-overrides.txt} file added back to
 * {@link ComponentAnnotationLoader} by {@code ComponentAnnotationLoaderOverridesCompatibilityAspect}.
 *
 * @version $Id$
 */
class ComponentAnnotationLoaderOverridesTest
{
    private static final String SIMPLE_ROLE = SimpleRole.class.getName();

    private static final String OVERRIDE_ROLE = OverrideRole.class.getName();

    @Role
    public interface TestRole
    {
    }

    @Component
    @Named("test")
    @Singleton
    public static class SimpleRole implements TestRole
    {
    }

    @Component
    @Named("test")
    @Singleton
    public static class OverrideRole implements TestRole
    {
    }

    private final ComponentAnnotationLoader loader = new ComponentAnnotationLoader();

    @Test
    void initializeRegistersOverridesWithHighestPriority() throws Exception
    {
        // Only expose the test component list files, so that the components declared by the module are not loaded.
        ClassLoader classLoader = new ClassLoader(getClass().getClassLoader())
        {
            @Override
            public Enumeration<URL> getResources(String name) throws IOException
            {
                return getParent().getResources("componentoverrides/" + name);
            }
        };
        ComponentManager componentManager = mock();

        this.loader.initialize(componentManager, classLoader);

        ArgumentCaptor<ComponentDescriptor<?>> captor = ArgumentCaptor.captor();
        verify(componentManager).registerComponent(captor.capture());
        assertEquals(OverrideRole.class, captor.getValue().getImplementation());
        assertEquals(0, captor.getValue().getRoleHintPriority());
    }

    @Test
    void getDeclaredComponentsFromJARMergesOverrides() throws IOException
    {
        ByteArrayOutputStream jar = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(jar)) {
            addEntry(zos, ComponentAnnotationLoader.COMPONENT_LIST, SIMPLE_ROLE + '\n' + OVERRIDE_ROLE);
            addEntry(zos, CompatibilityComponentAnnotationLoaderConstants.COMPONENT_OVERRIDE_LIST, OVERRIDE_ROLE);
        }

        List<ComponentDeclaration> declarations =
            this.loader.getDeclaredComponentsFromJAR(new ByteArrayInputStream(jar.toByteArray()));

        assertEquals(List.of(new ComponentDeclaration(SIMPLE_ROLE), new ComponentDeclaration(OVERRIDE_ROLE),
            new ComponentDeclaration(OVERRIDE_ROLE, 0)), declarations);
    }

    @Test
    void getDeclaredComponentsFromJARWithOnlyOverrides() throws IOException
    {
        ByteArrayOutputStream jar = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(jar)) {
            addEntry(zos, CompatibilityComponentAnnotationLoaderConstants.COMPONENT_OVERRIDE_LIST, OVERRIDE_ROLE);
        }

        assertEquals(List.of(new ComponentDeclaration(OVERRIDE_ROLE, 0)),
            this.loader.getDeclaredComponentsFromJAR(new ByteArrayInputStream(jar.toByteArray())));
    }

    private void addEntry(ZipOutputStream zos, String name, String content) throws IOException
    {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
