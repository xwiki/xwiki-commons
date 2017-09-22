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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.classloader.ClassLoaderManager;
import org.xwiki.classloader.NamespaceURLClassLoader;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.annotation.ComponentDeclaration;
import org.xwiki.component.internal.StackingComponentEventManager;
import org.xwiki.component.internal.multi.ComponentManagerManager;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.LocalExtensionFile;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.internal.AbstractExtensionHandler;
import org.xwiki.job.Request;
import org.xwiki.observation.ObservationManager;

/**
 * Add support for JAR extensions.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Named("jar")
@Singleton
public class JarExtensionHandler extends AbstractExtensionHandler implements Initializable
{
    @Inject
    private ComponentManagerManager componentManagerManager;

    @Inject
    private ClassLoaderManager jarExtensionClassLoader;

    private ComponentAnnotationLoader jarLoader;

    @Override
    public void initialize() throws InitializationException
    {
        this.jarLoader = new ComponentAnnotationLoader();
    }

    @Override
    public void initialize(LocalExtension localExtension, String namespace) throws ExtensionException
    {
        install(localExtension, namespace, null);
    }

    private static URL getExtensionURL(LocalExtension localExtension) throws MalformedURLException
    {
        return new File(localExtension.getFile().getAbsolutePath()).toURI().toURL();
    }

    @Override
    public void install(LocalExtension localExtension, String namespace, Request request) throws InstallException
    {
        NamespaceURLClassLoader classLoader = this.jarExtensionClassLoader.getURLClassLoader(namespace, true);

        // 1) load jar into classloader
        try {
            classLoader.addURL(getExtensionURL(localExtension));
        } catch (MalformedURLException e) {
            throw new InstallException("Failed to load jar file", e);
        }

        // 2) load and register components
        loadComponents(localExtension.getFile(), classLoader, namespace);
    }

    @Override
    public void uninstall(InstalledExtension installedExtension, String namespace, Request request)
        throws UninstallException
    {
        if (installedExtension.isValid(namespace)) {
            NamespaceURLClassLoader classLoader = this.jarExtensionClassLoader.getURLClassLoader(namespace, false);

            // There might be no classloading matching the passed namespace when installing a new JAR at the same time
            // than an upgrade on root
            if (classLoader != null && StringUtils.equals(namespace, classLoader.getNamespace())) {
                // unregister components
                try {
                    unloadComponents(installedExtension.getFile(), classLoader, namespace);
                } catch (Throwable e) {
                    // We failed to unregister some components, we probably failed to register them in the first
                    // place too so let's just ignore it. Better than making impossible to uninstall the extension.
                    // We catch Throwable because most of the time we end up with a LinkageError
                    this.logger.warn("Failed to unregister some components of the JAR extension [{}]",
                        installedExtension.getId(), e);
                }

                // The ClassLoader(s) will be replaced and reloaded at the end of the job
                // @see org.xwiki.extension.jar.internal.handler.JarExtensionJobFinishedListener
            }
        }
    }

    private void loadComponents(LocalExtensionFile jarFile, NamespaceURLClassLoader classLoader, String namespace)
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
            StackingComponentEventManager stackingComponentEventManager = null;
            try {
                if (componentEventManager instanceof StackingComponentEventManager) {
                    stackingComponentEventManager = (StackingComponentEventManager) componentEventManager;
                } else {
                    stackingComponentEventManager = new StackingComponentEventManager();
                    componentManager.setComponentEventManager(stackingComponentEventManager);
                }
                stackingComponentEventManager.shouldStack(true);

                this.jarLoader.initialize(componentManager, classLoader, componentDeclarations);
            } finally {
                if (stackingComponentEventManager != null) {
                    if (componentEventManager != stackingComponentEventManager) {
                        componentManager.setComponentEventManager(componentEventManager);
                    }

                    stackingComponentEventManager.setObservationManager(
                        componentManager.<ObservationManager>getInstance(ObservationManager.class));
                    stackingComponentEventManager.shouldStack(false);
                    stackingComponentEventManager.flushEvents();
                }
            }
        } catch (Exception e) {
            throw new InstallException("Failed to load jar file components", e);
        }
    }

    private List<ComponentDeclaration> getDeclaredComponents(LocalExtensionFile jarFile) throws IOException
    {
        InputStream is = jarFile.openStream();

        try {
            return this.jarLoader.getDeclaredComponentsFromJAR(is);
        } finally {
            is.close();
        }
    }

    private void unloadComponents(LocalExtensionFile jarFile, NamespaceURLClassLoader classLoader, String namespace)
        throws IOException
    {
        List<ComponentDeclaration> componentDeclarations = getDeclaredComponents(jarFile);

        if (componentDeclarations == null) {
            this.logger.debug("[{}] does not contain any component", jarFile.getName());
            return;
        }

        this.jarLoader.unregister(this.componentManagerManager.getComponentManager(namespace, false), classLoader,
            componentDeclarations);
    }
}
