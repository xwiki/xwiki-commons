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
package org.xwiki.environment.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheControl;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.LRUCacheConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;

/**
 * Defines what an Environment means in a Servlet environment.
 *
 * @version $Id$
 * @since 3.5M1
 */
@Component
@Singleton
public class ServletEnvironment extends AbstractEnvironment
{
    /**
     * @see #getServletContext()
     */
    private ServletContext servletContext;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private CacheControl cacheControl;

    private Cache<Optional<URL>> resourceURLCache;

    private final AtomicBoolean cacheInitializing = new AtomicBoolean();

    // SonarQube has no idea about concurrent modifications that make the repeated check for resourceURLCache == null
    // necessary.
    // See also the comment at the duplicate check that explains why we need to check again inside the "lock".
    @SuppressWarnings("java:S2589")
    private Cache<Optional<URL>> getResourceURLCache()
    {
        // Don't block on cache initialization to avoid loops.
        if (this.resourceURLCache == null && this.cacheInitializing.compareAndSet(false, true)) {
            try {
                // Compare again inside the "lock" to avoid race conditions. Only after seeing the false value we can
                // be sure that we also see the resourceURLCache variable write from the thread that wrote it -
                // writing atomics with "set" guarantees sequential consistency.
                if (this.resourceURLCache == null) {
                    // The cache manager can't be injected directly as it depends on this component, so it is
                    // important to only request it once this component has been initialized.
                    CacheManager cacheManager = this.componentManager.getInstance(CacheManager.class);
                    this.resourceURLCache = cacheManager.createNewCache(
                        new LRUCacheConfiguration("environment.servlet.resourceURLCache", 10000));
                }
            } catch (Exception e) {
                this.logger.error("Failed to initialize resource URL cache.", e);
            } finally {
                this.cacheInitializing.set(false);
            }
        }

        return this.resourceURLCache;
    }

    /**
     * @param servletContext see {@link #getServletContext()}
     */
    public void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    /**
     * @return the Servlet Context
     */
    public ServletContext getServletContext()
    {
        if (this.servletContext == null) {
            throw new RuntimeException("The Servlet Environment has not been properly initialized "
                + "(The Servlet Context is not set)");
        }
        return this.servletContext;
    }

    @Override
    public InputStream getResourceAsStream(String resourceName)
    {
        return getServletContext().getResourceAsStream(resourceName);
    }

    @Override
    // As we store Optional<URL> in the cache, the cache can return null for the Optional which is unavoidable.
    @SuppressWarnings("java:S2789")
    public URL getResource(String resourceName)
    {
        Cache<Optional<URL>> cache = getResourceURLCache();

        if (cache != null && this.cacheControl.isCacheReadAllowed()) {
            Optional<URL> cachedURL = cache.get(resourceName);

            if (cachedURL != null) {
                return cachedURL.orElse(null);
            }
        }

        URL url = getResourceInternal(resourceName);

        if (cache != null) {
            cache.set(resourceName, Optional.ofNullable(url));
        }

        return url;
    }

    private URL getResourceInternal(String resourceName)
    {
        URL url;
        try {
            url = getServletContext().getResource(resourceName);

            // ensure to normalize the URI, we don't want relative path.
            if (url != null) {
                url = url.toURI().normalize().toURL();
            }
        // We're catching IllegalArgumentException which might be thrown by Tomcat when trying to resolve path such as
        // `templates/../..`
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            url = null;
            this.logger.warn("Error getting resource [{}] because of invalid path format. Reason: [{}]",
                resourceName, e.getMessage());
        }
        return url;
    }

    @Override
    protected String getTemporaryDirectoryName()
    {
        final String tmpDirectory = super.getTemporaryDirectoryName();
        try {
            if (tmpDirectory == null) {
                File tempDir = (File) this.getServletContext().getAttribute(ServletContext.TEMPDIR);
                return tempDir == null ? null : tempDir.getCanonicalPath();
            }
        } catch (IOException e) {
            this.logger.warn("Unable to get Servlet temporary directory due to error [{}], "
                + "falling back on the default System temporary directory.", ExceptionUtils.getMessage(e));
        }
        return tmpDirectory;
    }
}
