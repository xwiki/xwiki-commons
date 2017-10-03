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
package org.xwiki.extension.repository.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.environment.Environment;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.internal.PathUtils;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.internal.ExtensionSerializer;
import org.xwiki.extension.version.Version;

/**
 * Scan jars to find core extensions.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultCoreExtensionScanner implements CoreExtensionScanner, Disposable
{
    /**
     * Logger to use to log shutdown information (opposite of initialization).
     */
    private static final Logger SHUTDOWN_LOGGER = LoggerFactory.getLogger("org.xwiki.shutdown");

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * Used to resolve found core extensions.
     */
    @Inject
    private Provider<ExtensionRepositoryManager> repositoryManagerProvider;

    @Inject
    private Environment environment;

    @Inject
    private CoreExtensionCache cache;

    @Inject
    private ExtensionSerializer parser;

    @Inject
    private List<ExtensionScanner> scanners;

    private boolean shouldStop;

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        this.shouldStop = true;
    }

    @Override
    public void updateExtensions(Collection<DefaultCoreExtension> extensions)
    {
        ExtensionRepositoryManager repositoryManager = this.repositoryManagerProvider.get();

        for (DefaultCoreExtension extension : extensions) {
            // If XWiki is stopping before this is finished then we need to exit.
            if (this.shouldStop) {
                SHUTDOWN_LOGGER.debug("Aborting Extension Update as XWiki is stopping");
                break;
            }

            if (!extension.isComplete()) {
                try {
                    Extension remoteExtension = repositoryManager.resolve(extension.getId());

                    extension.set(remoteExtension);
                    extension.setComplete(true);

                    // Cache it
                    if (extension.getDescriptorURL() != null) {
                        this.cache.store(extension);
                    }
                } catch (ResolveException e) {
                    this.logger.debug("Can't find remote extension with id [{}]", extension.getId(), e);
                } catch (Exception e) {
                    this.logger.warn("Failed to update core extension [{}]: [{}]", extension.getId(),
                        ExceptionUtils.getRootCauseMessage(e), e);
                }
            }
        }
    }

    @Override
    public Map<String, DefaultCoreExtension> loadExtensions(DefaultCoreExtensionRepository repository)
    {
        Map<String, DefaultCoreExtension> extensions = new HashMap<>();

        loadExtensionsFromClassloaders(extensions, repository);

        return extensions;
    }

    @Override
    public DefaultCoreExtension loadEnvironmentExtension(DefaultCoreExtensionRepository repository)
    {
        //////////
        // XED

        URL xedURL = this.environment.getResource("/META-INF/extension.xed");
        if (xedURL != null) {
            try (InputStream xedStream = this.environment.getResourceAsStream("/META-INF/extension.xed")) {
                return this.parser.loadCoreExtensionDescriptor(repository, null, xedStream);
            } catch (Exception e) {
                this.logger.error("Failed to load [{}] descriptor file", xedURL, e);
            }
        }

        //////////
        // Others

        for (ExtensionScanner scanner : this.scanners) {
            DefaultCoreExtension environmentExtension = scanner.scanEnvironment(repository);

            if (environmentExtension != null) {
                return environmentExtension;
            }
        }

        //////////
        // Could not find any valid descriptor

        this.logger.debug("No declared environmennt extension");

        return null;
    }

    private Collection<URL> getJARs()
    {
        Set<URL> urls = new HashSet<>();
        // ClasspathHelper.forClassLoader() get even the JARs that are made not reachable by the application server
        // So the trick is to get all resources in which we can access a META-INF folder
        urls.addAll(ClasspathHelper.forPackage("META-INF"));
        // It's possible in a (bad JAR) to declare the entry META-INF/MANIFEST.MF without the entry META-INF/
        urls.addAll(ClasspathHelper.forResource("META-INF/MANIFEST.MF"));
        // Workaround javax.inject 1 JAR which is incredibly hacky and does not even contain any META-INF folder so we
        // have to do
        // something special for it
        urls.addAll(ClasspathHelper.forPackage("javax"));

        Collection<URL> jarURLs = new ArrayList<>(urls.size());

        for (URL url : urls) {
            try {
                jarURLs.add(PathUtils.getExtensionURL(url));
            } catch (IOException e) {
                this.logger.error("Failed to convert to extension URL", e);
            }
        }

        return jarURLs;
    }

    private void addCoreExtension(Map<String, DefaultCoreExtension> extensions, DefaultCoreExtension coreExtension)
    {
        addCoreExtension(extensions, coreExtension, this.logger);
    }

    static public void addCoreExtension(Map<String, DefaultCoreExtension> extensions,
        DefaultCoreExtension coreExtension, Logger logger)
    {
        DefaultCoreExtension existingCoreExtension = extensions.get(coreExtension.getId().getId());

        if (existingCoreExtension == null) {
            extensions.put(coreExtension.getId().getId(), coreExtension);
        } else {
            Version existingVersion = existingCoreExtension.getId().getVersion();
            Version version = coreExtension.getId().getVersion();

            int comparizon = version.compareTo(existingVersion);

            // Ignore collision between same versions
            if (comparizon != 0) {
                logger.warn("Collision between core extension [{} ({})] and [{} ({})]", coreExtension.getId(),
                    coreExtension.getDescriptorURL(), existingCoreExtension.getId(),
                    existingCoreExtension.getDescriptorURL());

                DefaultCoreExtension selectedExtension;
                if (comparizon > 0) {
                    extensions.put(coreExtension.getId().getId(), coreExtension);

                    selectedExtension = coreExtension;
                } else {
                    selectedExtension = existingCoreExtension;
                }
                logger.warn("[{} ({})] is selected", selectedExtension.getId(), selectedExtension.getDescriptorURL());
            }
        }
    }

    private DefaultCoreExtension loadCoreExtensionFromXED(URL jarURL, DefaultCoreExtensionRepository repository)
    {
        this.logger.debug("  Loading XED [{}]...", jarURL);

        try {
            String jarString = jarURL.toExternalForm();

            int extIndex = jarString.lastIndexOf('.');
            if (extIndex > 0) {
                // Find XED file URL
                URL xedURL;
                try {
                    xedURL = new URL(jarString.substring(0, extIndex) + ".xed");
                } catch (MalformedURLException e) {
                    // Cannot really happen
                    return null;
                }

                // Load XED stream
                InputStream xedStream;
                try {
                    xedStream = xedURL.openStream();
                } catch (IOException e) {
                    // We assume it means the xed does not exist so we just ignore it
                    this.logger.debug("Failed to load [{}]", xedURL, e);
                    return null;
                }

                // Load XED file
                try {
                    this.logger.debug("    Parsing XED [{}]...", xedURL);

                    DefaultCoreExtension coreExtension =
                        this.parser.loadCoreExtensionDescriptor(repository, jarURL, xedStream);
                    coreExtension.setDescriptorURL(xedURL);
                    return coreExtension;
                } catch (Exception e) {
                    this.logger.error("Failed to load [{}]", xedURL, e);
                } finally {
                    IOUtils.closeQuietly(xedStream);

                    this.logger.debug("    Done parsing XED [{}]...", xedURL);
                }
            }
        } finally {
            this.logger.debug("  Done loading XED [{}]...", jarURL);
        }

        return null;
    }

    private void loadExtensionsFromClassloaders(Map<String, DefaultCoreExtension> extensions,
        DefaultCoreExtensionRepository repository)
    {
        ////////////////////
        // Get all jar files

        this.logger.debug("Searching for JARs...");

        Collection<URL> jars = getJARs();

        this.logger.debug("Found the following JARs: ", jars);

        ////////////////////
        // Try to find associated xed files

        this.logger.debug("Loading JARs with associated XED files...");

        fromXED(extensions, jars, repository);

        this.logger.debug("Done loading JARs with associated XED files");

        ////////////////////
        // Try with other scanners (for example find associated Maven files)

        this.logger.debug("Loading remaining JARs with registered scanners...");

        for (ExtensionScanner scanner : this.scanners) {
            scanner.scanJARs(extensions, jars, repository);
        }

        this.logger.debug("Done loading JARs with registered scanners");

        ////////////////////
        // Work some magic to guess the rest of the jar files

        this.logger.debug("Try to guess the id of some remaning JARs which don't have any know descriptor...");

        for (ExtensionScanner scanner : this.scanners) {
            scanner.guess(extensions, jars, repository);
        }

        this.logger.debug("Done guessing the id of remaning JARs which don't have any know descriptor");
    }

    private void fromXED(Map<String, DefaultCoreExtension> extensions, Collection<URL> jars,
        DefaultCoreExtensionRepository repository)
    {
        for (Iterator<URL> it = jars.iterator(); it.hasNext();) {
            URL jarURL = it.next();

            DefaultCoreExtension coreExtension = loadCoreExtensionFromXED(jarURL, repository);

            if (coreExtension != null) {
                // Add the core extension
                addCoreExtension(extensions, coreExtension);

                // Remove the jar from the list
                it.remove();
            }
        }
    }
}
