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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.classloader.ClassLoaderManager;
import org.xwiki.classloader.internal.ClassLoaderResetEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.event.AbstractExtensionEvent;
import org.xwiki.extension.event.ExtensionEvent;
import org.xwiki.extension.event.ExtensionInstalledEvent;
import org.xwiki.extension.event.ExtensionUninstalledEvent;
import org.xwiki.extension.event.ExtensionUpgradedEvent;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.handler.ExtensionInitializer;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.job.event.JobFinishingEvent;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;

/**
 * Listen to job finishing events to properly refresh extension ClassLoader when an uninstall job has been executed.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
@Named("JarExtensionJobFinishingListener")
// Fan out of 21
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class JarExtensionJobFinishingListener extends AbstractEventListener
{
    private static final class ReloadCollection
    {
        private boolean reload;

        private boolean reloadRootNamespace;

        private Set<String> reloadNamespaces = new HashSet<>();

        private Map<String, Set<ExtensionId>> newExtensions = new HashMap<>();

        private void addNamespaceToReload(String namespace)
        {
            this.reload = true;

            // No need to keep adding namespaces if the root namespace is already marked for reload
            if (!this.reloadRootNamespace) {
                this.reloadNamespaces.add(namespace);
                if (namespace != null) {
                    this.reloadNamespaces.add(namespace);
                } else {
                    this.reloadRootNamespace = true;
                }
            }
        }

        private void addNewExtension(String namespace, ExtensionId extensionId)
        {
            this.newExtensions.computeIfAbsent(namespace, n -> new HashSet<>()).add(extensionId);
        }
    }

    /**
     * Jar extension ClassLoader that will be properly refreshed.
     */
    @Inject
    private ClassLoaderManager jarExtensionClassLoader;

    /**
     * The local extension repository from which extension are initialized.
     */
    @Inject
    private InstalledExtensionRepository installedExtensionRepository;

    @Inject
    @Named(JarExtensionHandler.JAR)
    private ExtensionHandler jarHandler;

    @Inject
    private Execution execution;

    /**
     * Extension initializer used to reinstall extensions in a new ClassLoader.
     */
    @Inject
    private Provider<ExtensionInitializer> extensionInitializerProvider;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    @Inject
    private ObservationManager observationManager;

    /**
     * The default constructor.
     */
    public JarExtensionJobFinishingListener()
    {
        super("JarExtensionJobFinishedListener", new ExtensionInstalledEvent(), new ExtensionUninstalledEvent(),
            new ExtensionUpgradedEvent(), new JobStartedEvent(), new JobFinishingEvent());
    }

    private void pushUninstallLevel()
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null) {
            Stack<ReloadCollection> extensions = getUninstalledExtensionCollectionStack(true);

            extensions.push(null);
        }
    }

    private void popUninstallLevel()
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null) {
            Stack<ReloadCollection> extensions = getUninstalledExtensionCollectionStack(false);

            if (extensions != null) {
                extensions.pop();
            }
        }
    }

    private Stack<ReloadCollection> getUninstalledExtensionCollectionStack(boolean create)
    {
        ExecutionContext context = this.execution.getContext();
        final String contextKey = "extension.jar.uninstalledExtensions";

        if (context != null) {
            @SuppressWarnings("unchecked")
            Stack<ReloadCollection> extensions = (Stack<ReloadCollection>) context.getProperty(contextKey);

            if (extensions == null && create) {
                extensions = new Stack<>();
                context.setProperty(contextKey, extensions);
            }

            return extensions;
        }

        return null;
    }

    private ReloadCollection getCurrentJobUninstalledExtensions(boolean create)
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null) {
            Stack<ReloadCollection> extensions = getUninstalledExtensionCollectionStack(false);

            if (extensions != null) {
                ReloadCollection collection = extensions.peek();

                if (collection == null && create) {
                    collection = new ReloadCollection();
                    extensions.set(extensions.size() - 1, collection);
                }

                return collection;
            }
        }

        return null;
    }

    private void addUninstalledExtension(String namespace)
    {
        ReloadCollection collection = getCurrentJobUninstalledExtensions(true);

        if (collection != null) {
            collection.addNamespaceToReload(namespace);
        }
    }

    private void addInstalledExtension(String namespace, ExtensionId extensionId)
    {
        ReloadCollection collection = getCurrentJobUninstalledExtensions(true);

        if (collection != null) {
            collection.addNewExtension(namespace, extensionId);
        }
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof ExtensionUninstalledEvent uninstalledEvent) {
            onExtensionRemovedEvent(uninstalledEvent, (InstalledExtension) source);
        } else if (event instanceof ExtensionUpgradedEvent upgradedEvent) {
            for (InstalledExtension previous : (Collection<InstalledExtension>) data) {
                onExtensionRemovedEvent(upgradedEvent, previous);
            }
            onExtensionAddedEvent(upgradedEvent, (InstalledExtension) source);
        } else if (event instanceof ExtensionInstalledEvent installedEvent) {
            onExtensionAddedEvent(installedEvent, (InstalledExtension) source);
        } else if (event instanceof JobStartedEvent) {
            onJobStartedEvent();
        } else {
            onJobFinishedEvent();
        }
    }

    private void onExtensionAddedEvent(AbstractExtensionEvent event, InstalledExtension extension)
    {
        if (JarExtensionHandler.isSupported(extension.getType())) {
            // Remember extension which are new, and so you should be unloaded at the end of the job
            addInstalledExtension(event.getNamespace(), extension.getId());
        }
    }

    private void onExtensionRemovedEvent(ExtensionEvent event, InstalledExtension extension)
    {
        if (JarExtensionHandler.isSupported(extension.getType())) {
            // Remember namespace which should be reloaded at the end of the job
            addUninstalledExtension(event.getNamespace());
        }
    }

    private void onJobStartedEvent()
    {
        pushUninstallLevel();
    }

    private void initializeExtensions(String namespace)
    {
        ExtensionInitializer initializer = this.extensionInitializerProvider.get();

        // Load JAR extensions
        initializer.initialize(namespace, JarExtensionHandler.JAR);
        // Load WEBJAR extensions
        initializer.initialize(namespace, JarExtensionHandler.WEBJAR);
        // Load WEBJAR Node extensions
        initializer.initialize(namespace, JarExtensionHandler.WEBJAR_NODE);
    }

    private void onJobFinishedEvent()
    {
        ReloadCollection reloadCollection = getCurrentJobUninstalledExtensions(false);

        popUninstallLevel();

        if (reloadCollection != null && reloadCollection.reload) {
            if (reloadCollection.reloadRootNamespace) {
                // Unload extensions
                unloadJARsFromNamespace(null, reloadCollection);

                // Drop class loaders for all namespaces
                this.jarExtensionClassLoader.dropURLClassLoaders();

                // Re-initialize all extensions
                initializeExtensions(null);

                // Notify that all ClassLoaders have been reloaded
                this.observationManager.notify(new ClassLoaderResetEvent(), null);
            } else {
                for (String namespace : reloadCollection.reloadNamespaces) {
                    // Unload extensions
                    unloadJARsFromNamespace(namespace, reloadCollection);

                    // Drop class loader
                    this.jarExtensionClassLoader.dropURLClassLoader(namespace);

                    // Re-initialize extensions from the namespace
                    initializeExtensions(namespace);

                    // Notify that the ClassLoader has been reloaded for the namespace
                    this.observationManager.notify(new ClassLoaderResetEvent(namespace), null);
                }
            }
        }
    }

    private void unloadJARsFromNamespace(String namespace, ReloadCollection reloadCollection)
    {
        // Load extensions from local repository
        Collection<InstalledExtension> installedExtensions;
        if (namespace != null) {
            installedExtensions = this.installedExtensionRepository.getInstalledExtensions(namespace);
        } else {
            installedExtensions = this.installedExtensionRepository.getInstalledExtensions();
        }

        for (InstalledExtension installedExtension : installedExtensions) {
            // Skip non JAR extensions
            if (JarExtensionHandler.isSupported(installedExtension.getType())) {
                if (namespace == null || !installedExtension.isInstalled(null)) {
                    try {
                        unloadJAR(installedExtension, namespace, reloadCollection.newExtensions);
                    } catch (Exception e) {
                        this.logger.error("Failed to unload installed extension [{}]", installedExtension, e);
                    }
                }
            }
        }
    }

    private void unloadJAR(InstalledExtension installedExtension, String namespace,
        Map<String, Set<ExtensionId>> skipExtensions) throws UninstallException
    {
        Set<ExtensionId> skipExtensionsInNamespace = skipExtensions.computeIfAbsent(namespace, n -> new HashSet<>());

        if (skipExtensionsInNamespace.contains(installedExtension.getId())) {
            return;
        }
        if (namespace == null) {
            if (installedExtension.isInstalled(null)) {
                try {
                    Map<String, Collection<InstalledExtension>> bDependencies =
                        this.installedExtensionRepository.getBackwardDependencies(installedExtension.getId());

                    for (Map.Entry<String, Collection<InstalledExtension>> entry : bDependencies.entrySet()) {
                        for (InstalledExtension bDependency : entry.getValue()) {
                            unloadJAR(bDependency, entry.getKey(), skipExtensions);
                        }
                    }
                } catch (ResolveException e) {
                    this.logger.error("Failed to get backward dependencies for installed extension [{}]",
                        installedExtension, e);
                }

                this.jarHandler.uninstall(installedExtension, null, null);

                // Remember that we unloaded the extension
                skipExtensionsInNamespace.add(installedExtension.getId());
            } else {
                for (String namespace2 : installedExtension.getNamespaces()) {
                    unloadJAR(installedExtension, namespace2, skipExtensions);
                }
            }
        } else {
            unloadJARFromNamespace(installedExtension, namespace, skipExtensions, skipExtensionsInNamespace);
        }
    }

    private void unloadJARFromNamespace(InstalledExtension installedExtension, String namespace,
        Map<String, Set<ExtensionId>> skipExtensions, Set<ExtensionId> skipExtensionsInNamespace)
        throws UninstallException
    {
        try {
            Collection<InstalledExtension> bDependencies = this.installedExtensionRepository
                .getBackwardDependencies(installedExtension.getId().getId(), namespace);

            for (InstalledExtension bDependency : bDependencies) {
                unloadJAR(bDependency, namespace, skipExtensions);
            }
        } catch (ResolveException e) {
            this.logger.error("Failed to get backward dependencies for installed extension [{}] on namespace [{}]",
                installedExtension, namespace, e);
        }

        this.jarHandler.uninstall(installedExtension, namespace, null);

        // Remember that we unloaded the extension
        skipExtensionsInNamespace.add(installedExtension.getId());
    }
}
