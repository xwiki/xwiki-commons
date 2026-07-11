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
package org.xwiki.extension.jar.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.classloader.NamespaceURLClassLoader;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.annotation.ComponentDeclaration;
import org.xwiki.component.internal.QueueComponentEventManager;
import org.xwiki.component.internal.multi.ComponentManagerManager;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtensionFile;
import org.xwiki.observation.ObservationManager;

/**
 * Helper to load JAR extensions in the classpath.
 * 
 * @version $Id$
 * @since 18.5.0RC1
 * @since 18.4.2
 * @since 17.10.10
 */
@Component(roles = JarLoader.class)
@Singleton
public class JarLoader implements Initializable
{
    @Inject
    private ComponentManagerManager componentManagerManager;

    @Inject
    private Logger logger;

    private ComponentAnnotationLoader loader;

    @Override
    public void initialize() throws InitializationException
    {
        this.loader = new ComponentAnnotationLoader();
    }

    /**
     * @param jarFile the JAR file to load
     * @param classLoader the classloader in which to load the JAR file
     * @param namespace the namespace in which to load the JAR
     * @throws InstallException when failing to load the JAR
     */
    public void loadComponents(LocalExtensionFile jarFile, NamespaceURLClassLoader classLoader, String namespace)
        throws InstallException
    {
        try {
            List<ComponentDeclaration> componentDeclarations = getDeclaredComponents(jarFile);

            if (componentDeclarations == null) {
                this.logger.debug("[{}] does not contain any components to load", jarFile.getName());
                return;
            }

            ComponentManager componentManager = this.componentManagerManager.getComponentManager(namespace, true);

            ComponentEventManager componentEventManager = componentManager.getComponentEventManager();

            // Make sure to send events only when the extension is fully ready
            QueueComponentEventManager queueComponentEventManager;
            if (componentEventManager instanceof QueueComponentEventManager queuedComponentEventManager) {
                queueComponentEventManager = queuedComponentEventManager;
                if (queueComponentEventManager.isQueued()) {
                    // If already stacked don't do anything (and more importantly don't disabled queueing)
                    queueComponentEventManager = null;
                } else {
                    queueComponentEventManager.shouldQueue(true);
                }
            } else {
                queueComponentEventManager = new QueueComponentEventManager();
                componentManager.setComponentEventManager(queueComponentEventManager);
            }

            // Initialize the JAR
            try {
                this.loader.initialize(componentManager, classLoader, componentDeclarations);
            } finally {
                if (queueComponentEventManager != null) {
                    if (componentEventManager != queueComponentEventManager) {
                        componentManager.setComponentEventManager(componentEventManager);
                    }

                    queueComponentEventManager.setObservationManager(
                        componentManager.<ObservationManager>getInstance(ObservationManager.class));
                    queueComponentEventManager.shouldQueue(false);
                    queueComponentEventManager.flushEvents();
                }
            }
        } catch (Exception | LinkageError e) {
            throw new InstallException("Failed to load components for file [" + jarFile.getAbsolutePath() + "]", e);
        }
    }

    private List<ComponentDeclaration> getDeclaredComponents(LocalExtensionFile jarFile) throws IOException
    {
        try (InputStream is = jarFile.openStream()) {
            return this.loader.getDeclaredComponentsFromJAR(is);
        }
    }

    /**
     * @param jarFile the JAR file to unload
     * @param classLoader the classloader from which to unload the JAR file
     * @param namespace the namespace from which to unload the JAR
     * @throws IOException when failing to unload the JAR
     */
    public void unloadComponents(LocalExtensionFile jarFile, NamespaceURLClassLoader classLoader, String namespace)
        throws IOException
    {
        List<ComponentDeclaration> componentDeclarations = getDeclaredComponents(jarFile);

        if (componentDeclarations == null) {
            this.logger.debug("[{}] does not contain any component", jarFile.getName());
            return;
        }

        this.loader.unregister(this.componentManagerManager.getComponentManager(namespace, false), classLoader,
            componentDeclarations);
    }
}
