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

import org.apache.commons.lang3.exception.ExceptionUtils;
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

            // Make sure to send events only when the extension is fully ready
            withQueuedEvents(componentManager,
                () -> this.loader.initialize(componentManager, classLoader, componentDeclarations));
        } catch (Exception | LinkageError e) {
            throw new InstallException("Failed to load components for file [" + jarFile.getAbsolutePath() + "]", e);
        }
    }

    private void withQueuedEvents(ComponentManager componentManager, ComponentAction action) throws Exception
    {
        ComponentEventManager componentEventManager = componentManager.getComponentEventManager();

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

        try {
            action.run();
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

        ComponentManager componentManager = this.componentManagerManager.getComponentManager(namespace, false);

        // No component can be registered in a component manager which does not exist
        if (componentManager != null) {
            // Unregistering a component overriding another one makes the overridden one active again: make sure to
            // send events only when all the components of the extension are unregistered, so that listeners don't
            // look up a component which is about to be unregistered too.
            try {
                withQueuedEvents(componentManager,
                    () -> this.loader.unregister(componentManager, classLoader, componentDeclarations));
            } catch (Exception e) {
                // Only the lookup of the ObservationManager can fail, unregister() doesn't throw any checked exception
                this.logger.warn("Failed to send the events about the components unregistered from [{}]: [{}]",
                    jarFile.getName(), ExceptionUtils.getRootCauseMessage(e));
            }
        }
    }

    @FunctionalInterface
    private interface ComponentAction
    {
        void run() throws Exception;
    }
}
