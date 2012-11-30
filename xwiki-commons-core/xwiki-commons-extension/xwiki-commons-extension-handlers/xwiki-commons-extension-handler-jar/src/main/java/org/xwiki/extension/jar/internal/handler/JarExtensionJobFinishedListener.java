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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.classloader.ClassLoaderManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.event.ExtensionUninstalledEvent;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.handler.ExtensionInitializer;
import org.xwiki.extension.job.internal.UninstallJob;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.job.event.JobFinishedEvent;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * Listen to job finished events to properly refresh extension ClassLoader when an uninstall job has been executed.
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
@Named("JarExtensionJobFinishedListener")
public class JarExtensionJobFinishedListener implements EventListener
{
    private static final class UninstalledExtensionCollection
    {
        boolean rootNamespace = false;

        public Set<String> namespaces;

        public void add(String namespace)
        {
            if (!this.rootNamespace) {
                if (namespace != null) {
                    if (this.namespaces == null) {
                        this.namespaces = new HashSet<String>();
                    }
                    this.namespaces.add(namespace);
                } else {
                    this.rootNamespace = true;
                }
            }
        }
    }

    /**
     * The list of events observed.
     */
    private static final List<Event> EVENTS = Arrays.asList(new ExtensionUninstalledEvent(), new JobStartedEvent(
        UninstallJob.JOBTYPE), new JobFinishedEvent(UninstallJob.JOBTYPE));

    /**
     * Jar extension ClassLoader that will be properly refreshed.
     */
    @Inject
    private ClassLoaderManager jarExtensionClassLoader;

    /**
     * Extension initializer used to reinstall extensions in a new ClassLoader
     */
    @Inject
    private ExtensionInitializer extensionInitializer;

    /**
     * The local extension repository from which extension are initialized.
     */
    @Inject
    private InstalledExtensionRepository installedExtensionRepository;

    @Inject
    @Named("jar")
    private ExtensionHandler jarHandler;

    @Inject
    private Execution execution;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    @Override
    public String getName()
    {
        return "JarExtensionJobFinishedListener";
    }

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    private void pushUninstallLevel()
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null) {
            Stack<UninstalledExtensionCollection> extensions = getUninstalledExtensionCollectionStack(true);

            extensions.push(null);
        }
    }

    private void popUninstallLevel()
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null) {
            Stack<UninstalledExtensionCollection> extensions = getUninstalledExtensionCollectionStack(false);

            if (extensions != null) {
                extensions.pop();
            }
        }
    }

    private Stack<UninstalledExtensionCollection> getUninstalledExtensionCollectionStack(boolean create)
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null) {
            Stack<UninstalledExtensionCollection> extensions =
                (Stack<UninstalledExtensionCollection>) context.getProperty("extension.jar.uninstalledExtensions");

            if (extensions == null && create) {
                extensions = new Stack<UninstalledExtensionCollection>();
                context.setProperty("extension.jar.uninstalledExtensions", extensions);
            }

            return extensions;
        }

        return null;
    }

    private UninstalledExtensionCollection getCurrentJobUninstalledExtensions(boolean create)
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null) {
            Stack<UninstalledExtensionCollection> extensions = getUninstalledExtensionCollectionStack(false);

            if (extensions != null) {
                UninstalledExtensionCollection collection = extensions.peek();

                if (collection == null && create) {
                    collection = new UninstalledExtensionCollection();
                    extensions.set(extensions.size() - 1, collection);
                }

                return collection;
            }
        }

        return null;
    }

    private void addUninstalledExtension(ExtensionId id, String namespace)
    {
        UninstalledExtensionCollection collection = getCurrentJobUninstalledExtensions(true);

        if (collection != null) {
            collection.add(namespace);
        }
    }

    @Override
    public void onEvent(Event event, Object o, Object o1)
    {
        if (event instanceof ExtensionUninstalledEvent) {
            onExtensionUninstalledEvent(event);
        } else if (event instanceof JobStartedEvent) {
            onJobStartedEvent(event);
        } else {
            onJobFinishedEvent(event);
        }
    }

    private void onExtensionUninstalledEvent(Event event)
    {
        ExtensionUninstalledEvent uninstallEvent = (ExtensionUninstalledEvent) event;

        addUninstalledExtension(uninstallEvent.getExtensionId(), uninstallEvent.getNamespace());
    }

    private void onJobStartedEvent(Event event)
    {
        pushUninstallLevel();
    }

    private void onJobFinishedEvent(Event event)
    {
        UninstalledExtensionCollection collection = getCurrentJobUninstalledExtensions(false);

        popUninstallLevel();

        if (collection != null) {
            if (collection.rootNamespace) {
                // Unload extensions
                unloadJARsFromNamespace(null, null);

                // Drop class loaders
                this.jarExtensionClassLoader.dropURLClassLoaders();

                // Load extensions
                this.extensionInitializer.initialize(null, "jar");
            } else if (collection.namespaces != null) {
                for (String namespace : collection.namespaces) {
                    // Unload extensions
                    unloadJARsFromNamespace(namespace, null);

                    // Drop class loader
                    this.jarExtensionClassLoader.dropURLClassLoader(namespace);

                    // Load extensions
                    this.extensionInitializer.initialize(namespace, "jar");
                }
            }
        }
    }

    private void unloadJARsFromNamespace(String namespace, Map<String, Set<InstalledExtension>> unloadedExtensions)
    {
        if (unloadedExtensions == null) {
            unloadedExtensions = new HashMap<String, Set<InstalledExtension>>();
        }

        // Load extensions from local repository
        Collection<InstalledExtension> installedExtensions;
        if (namespace != null) {
            installedExtensions = this.installedExtensionRepository.getInstalledExtensions(namespace);
        } else {
            installedExtensions = this.installedExtensionRepository.getInstalledExtensions();
        }

        for (InstalledExtension installedExtension : installedExtensions) {
            if ("jar".equals(installedExtension.getType())) {
                if (namespace == null || !installedExtension.isInstalled(null)) {
                    try {
                        unloadJAR(installedExtension, namespace, unloadedExtensions);
                    } catch (Exception e) {
                        this.logger.error("Failed to unload installed extension [{}]", installedExtension, e);
                    }
                }
            }
        }
    }

    private void unloadJAR(InstalledExtension installedExtension, String namespace,
        Map<String, Set<InstalledExtension>> unloadedExtensions) throws UninstallException
    {
        Set<InstalledExtension> unloadedExtensionsInNamespace = unloadedExtensions.get(namespace);

        if (unloadedExtensionsInNamespace == null) {
            unloadedExtensionsInNamespace = new HashSet<InstalledExtension>();
            unloadedExtensions.put(namespace, unloadedExtensionsInNamespace);
        }

        if (!unloadedExtensionsInNamespace.contains(installedExtension)) {
            if (namespace == null) {
                if (installedExtension.isInstalled(null)) {
                    try {
                        Map<String, Collection<InstalledExtension>> bDependencies =
                            this.installedExtensionRepository.getBackwardDependencies(installedExtension.getId());

                        for (Map.Entry<String, Collection<InstalledExtension>> entry : bDependencies.entrySet()) {
                            for (InstalledExtension bDependency : entry.getValue()) {
                                unloadJAR(bDependency, entry.getKey(), unloadedExtensions);
                            }
                        }
                    } catch (ResolveException e) {
                        this.logger.error("Failed to get backward dependencies for installed extension [{}]",
                            installedExtension, e);
                    }

                    this.jarHandler.uninstall(installedExtension, null, null);
                    unloadedExtensionsInNamespace.add(installedExtension);
                } else {
                    for (String namespace2 : installedExtension.getNamespaces()) {
                        unloadJAR(installedExtension, namespace2, unloadedExtensions);
                    }
                }
            } else {
                try {
                    Collection<InstalledExtension> bDependencies =
                        this.installedExtensionRepository.getBackwardDependencies(installedExtension.getId().getId(),
                            namespace);

                    for (InstalledExtension bDependency : bDependencies) {
                        unloadJAR(bDependency, namespace, unloadedExtensions);
                    }
                } catch (ResolveException e) {
                    this.logger.error(
                        "Failed to get backward dependencies for installed extension [{}] on namespace [{}]",
                        installedExtension, namespace, e);
                }

                this.jarHandler.uninstall(installedExtension, namespace, null);
                unloadedExtensionsInNamespace.add(installedExtension);
            }
        }
    }
}
