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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.maven.ArtifactExtensionToExtensionType;
import org.xwiki.extension.maven.ArtifactPackagingToExtensionType;
import org.xwiki.extension.maven.ArtifactTypeToExtensionType;
import org.xwiki.extension.repository.maven.internal.handler.MavenArtifactHandler;
import org.xwiki.extension.repository.maven.internal.handler.MavenArtifactHandlerManager;

/**
 * Helpers to convert various inputs into an extension type.
 * 
 * @version $Id$
 * @since 16.4.0RC1
 */
@Component(roles = ExtensionTypeConverter.class)
@Singleton
public class ExtensionTypeConverter
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private MavenArtifactHandlerManager handlers;

    /**
     * @param handler the Maven artifact handler
     * @return the extension type
     * @throws ComponentLookupException when failing to resolve the extension type
     */
    public String toExtensionType(MavenArtifactHandler handler) throws ComponentLookupException
    {
        ComponentManager componentManager = this.componentManagerProvider.get();

        // In last resort, fallback in the Maven file extension
        String extensionType = handler.getExtension();

        if (componentManager.hasComponent(ArtifactPackagingToExtensionType.class, handler.getPackaging())) {
            extensionType =
                componentManager.<ArtifactPackagingToExtensionType>getInstance(ArtifactPackagingToExtensionType.class,
                    handler.getPackaging()).getExtensionType();
        } else if (componentManager.hasComponent(ArtifactTypeToExtensionType.class, handler.getType())) {
            extensionType = componentManager
                .<ArtifactTypeToExtensionType>getInstance(ArtifactTypeToExtensionType.class, handler.getType())
                .getExtensionType();
        } else if (componentManager.hasComponent(ArtifactExtensionToExtensionType.class, handler.getExtension())) {
            extensionType =
                componentManager.<ArtifactExtensionToExtensionType>getInstance(ArtifactExtensionToExtensionType.class,
                    handler.getExtension()).getExtensionType();
        }

        return extensionType;
    }

    /**
     * @param mavenPackaging the Maven packaging
     * @return the extension type
     * @throws ComponentLookupException when failing to resolve the extension type
     */
    public String mavenPackagingToExtensionType(String mavenPackaging) throws ComponentLookupException
    {
        MavenArtifactHandler handler = this.handlers.getByPackaging(mavenPackaging);

        if (handler != null) {
            return toExtensionType(handler);
        }

        ComponentManager componentManager = this.componentManagerProvider.get();

        // In last resort, fallback in the Maven file packaging
        String extensionType = mavenPackaging;

        if (componentManager.hasComponent(ArtifactPackagingToExtensionType.class, mavenPackaging)) {
            extensionType = componentManager
                .<ArtifactExtensionToExtensionType>getInstance(ArtifactPackagingToExtensionType.class, mavenPackaging)
                .getExtensionType();
        }

        return extensionType;
    }

    /**
     * @param mavenType the Maven type
     * @return the extension type
     * @throws ComponentLookupException when failing to resolve the extension type
     */
    public String mavenTypeToExtensionType(String mavenType) throws ComponentLookupException
    {
        MavenArtifactHandler handler = this.handlers.getByType(mavenType);

        if (handler != null) {
            return toExtensionType(handler);
        }

        ComponentManager componentManager = this.componentManagerProvider.get();

        // In last resort, fallback in the Maven file packaging
        String extensionType = mavenType;

        if (componentManager.hasComponent(ArtifactTypeToExtensionType.class, mavenType)) {
            extensionType = componentManager
                .<ArtifactExtensionToExtensionType>getInstance(ArtifactTypeToExtensionType.class, mavenType)
                .getExtensionType();
        }

        return extensionType;
    }

    /**
     * @param mavenExtension the Maven packaging
     * @return the extension type
     * @throws ComponentLookupException when failing to resolve the extension type
     */
    public String mavenExtensionToExtensionType(String mavenExtension) throws ComponentLookupException
    {
        ComponentManager componentManager = this.componentManagerProvider.get();

        // In last resort, fallback in the Maven file extension
        String extensionType = mavenExtension;

        if (componentManager.hasComponent(ArtifactExtensionToExtensionType.class, mavenExtension)) {
            extensionType = componentManager
                .<ArtifactExtensionToExtensionType>getInstance(ArtifactExtensionToExtensionType.class, mavenExtension)
                .getExtensionType();
        }

        return extensionType;
    }
}
