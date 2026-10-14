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
package org.xwiki.extension.repository.maven.internal.converter;

import javax.inject.Named;
import javax.inject.Provider;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.maven.ArtifactExtensionToExtensionType;
import org.xwiki.extension.maven.ArtifactPackagingToExtensionType;
import org.xwiki.extension.maven.ArtifactTypeToExtensionType;
import org.xwiki.extension.repository.maven.internal.handler.MavenArtifactHandler;
import org.xwiki.extension.repository.maven.internal.handler.MavenArtifactHandlerManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExtensionTypeConverter}.
 *
 * @version $Id$
 */
@ComponentTest
class ExtensionTypeConverterTest
{
    @InjectMockComponents
    private ExtensionTypeConverter converter;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @MockComponent
    private MavenArtifactHandlerManager handlers;

    private ComponentManager componentManager;

    @BeforeEach
    void beforeEach()
    {
        this.componentManager = mock(ComponentManager.class);
        when(this.componentManagerProvider.get()).thenReturn(this.componentManager);
    }

    private static MavenArtifactHandler handler(String type, String packaging, String extension)
    {
        ArtifactHandler artifactHandler = mock(ArtifactHandler.class);
        when(artifactHandler.getPackaging()).thenReturn(packaging);
        when(artifactHandler.getExtension()).thenReturn(extension);

        return new MavenArtifactHandler(type, artifactHandler);
    }

    private void registerPackaging(String packaging, String extensionType) throws ComponentLookupException
    {
        ArtifactPackagingToExtensionType mapping = mock(ArtifactPackagingToExtensionType.class);
        when(mapping.getExtensionType()).thenReturn(extensionType);
        when(this.componentManager.hasComponent(ArtifactPackagingToExtensionType.class, packaging)).thenReturn(true);
        when(this.componentManager.getInstance(ArtifactPackagingToExtensionType.class, packaging))
            .thenReturn(mapping);
    }

    private void registerType(String type, String extensionType) throws ComponentLookupException
    {
        ArtifactTypeToExtensionType mapping = mock(ArtifactTypeToExtensionType.class);
        when(mapping.getExtensionType()).thenReturn(extensionType);
        when(this.componentManager.hasComponent(ArtifactTypeToExtensionType.class, type)).thenReturn(true);
        when(this.componentManager.getInstance(ArtifactTypeToExtensionType.class, type)).thenReturn(mapping);
    }

    private void registerExtension(String extension, String extensionType) throws ComponentLookupException
    {
        ArtifactExtensionToExtensionType mapping = mock(ArtifactExtensionToExtensionType.class);
        when(mapping.getExtensionType()).thenReturn(extensionType);
        when(this.componentManager.hasComponent(ArtifactExtensionToExtensionType.class, extension)).thenReturn(true);
        when(this.componentManager.getInstance(ArtifactExtensionToExtensionType.class, extension))
            .thenReturn(mapping);
    }

    @Test
    void toExtensionTypePrefersThePackaging() throws ComponentLookupException
    {
        registerPackaging("webjar", "webjar-node");
        registerType("webjar", "from-type");
        registerExtension("jar", "from-extension");

        assertEquals("webjar-node", this.converter.toExtensionType(handler("webjar", "webjar", "jar")));
    }

    @Test
    void toExtensionTypeFallsBackOnTheType() throws ComponentLookupException
    {
        registerType("test-jar", "from-type");
        registerExtension("jar", "from-extension");

        assertEquals("from-type", this.converter.toExtensionType(handler("test-jar", "jar", "jar")));
    }

    @Test
    void toExtensionTypeFallsBackOnTheExtension() throws ComponentLookupException
    {
        registerExtension("jar", "from-extension");

        assertEquals("from-extension", this.converter.toExtensionType(handler("test-jar", "jar", "jar")));
    }

    @Test
    void toExtensionTypeFallsBackOnTheMavenFileExtension() throws ComponentLookupException
    {
        assertEquals("zip", this.converter.toExtensionType(handler("assembly", "pom", "zip")));
    }

    @Test
    void mavenPackagingToExtensionTypeWithAHandler() throws ComponentLookupException
    {
        MavenArtifactHandler handler = handler("webjar", "webjar", "jar");
        when(this.handlers.getByPackaging("webjar")).thenReturn(handler);
        registerPackaging("webjar", "webjar-node");

        assertEquals("webjar-node", this.converter.mavenPackagingToExtensionType("webjar"));
    }

    @Test
    void mavenPackagingToExtensionTypeWithoutAHandler() throws ComponentLookupException
    {
        assertEquals("unknown", this.converter.mavenPackagingToExtensionType("unknown"));
    }

    @Test
    void mavenTypeToExtensionTypeWithAHandler() throws ComponentLookupException
    {
        MavenArtifactHandler handler = handler("test-jar", "jar", "jar");
        when(this.handlers.getByType("test-jar")).thenReturn(handler);
        registerType("test-jar", "from-type");

        assertEquals("from-type", this.converter.mavenTypeToExtensionType("test-jar"));
    }

    @Test
    void mavenTypeToExtensionTypeWithoutAHandler() throws ComponentLookupException
    {
        assertEquals("unknown", this.converter.mavenTypeToExtensionType("unknown"));
    }

    @Test
    void mavenExtensionToExtensionType() throws ComponentLookupException
    {
        registerExtension("xar", "xar");

        assertEquals("xar", this.converter.mavenExtensionToExtensionType("xar"));
        assertEquals("unknown", this.converter.mavenExtensionToExtensionType("unknown"));
    }
}
