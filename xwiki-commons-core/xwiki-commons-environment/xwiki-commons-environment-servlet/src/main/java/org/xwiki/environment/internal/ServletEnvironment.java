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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import jakarta.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheControl;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.LRUCacheConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

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
     * The expected path of the file used to test how the Servlet container handles URL encoded characters in resource
     * paths.
     * @since 18.2.0
     * @since 17.10.5
     */
    static final String ENCODED_RESOURCE_PATH = "/WEB-INF/resourcecheck/a%61b";

    /**
     * The expected content of the file aab.
     * @since 18.2.0
     * @since 17.10.5
     */
    static final String DECODED_RESOURCE_CONTENT = "aab";

    /**
     * The expected content of the file a%61b.
     * @since 18.2.0
     * @since 17.10.5
     */
    static final String ENCODED_RESOURCE_CONTENT = "a%61b";

    private static final String LOGGER_INVALID_RESOURCE_PATH =
        "Error getting resource [{}] because of invalid path format. Reason: [{}]";

    private static final String LOGGER_PATH_TRAVERSAL =
        "The path [{}] is trying to access a resource outside of the specified prefix [{}].";

    private static final String LOGGER_PATH_TRAVERSAL_ROOT =
        "The path [{}] is trying to access a resource outside of the resource root.";

    private static final String SLASH = "/";

    private static final String ROOT_PATH = SLASH;

    private static final String VIRTUAL_ROOT_PATH_PREFIX = "/root";

    private static final String VIRTUAL_ROOT_PATH = VIRTUAL_ROOT_PATH_PREFIX + SLASH;

    /**
     * @see #getJakartaServletContext()
     */
    private ServletContext jakartaServletContext;

    /**
     * @see #getServletContext()
     */
    private javax.servlet.ServletContext javaxServletContext;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private CacheControl cacheControl;

    private Cache<ResourceCacheEntry> resourceURLCache;

    private boolean servletDecodeURL;

    private boolean realPathSupported;

    private String rootRealPath;

    static final class ResourceCacheEntry
    {
        private Optional<URL> url;

        private Optional<String> realPath;

        ResourceCacheEntry()
        {
            this.url = null;
            this.realPath = null;
        }

        ResourceCacheEntry(Optional<URL> url, Optional<String> resourceRealPath)
        {
            this.url = url;
            this.realPath = resourceRealPath;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) {
                return true;
            }

            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            ResourceCacheEntry other = (ResourceCacheEntry) obj;
            return Objects.equals(this.url, other.url) && Objects.equals(this.realPath, other.realPath);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.url, this.realPath);
        }

        @Override
        public String toString()
        {
            return "ResourceCacheEntry{url=" + this.url + ", realPath=" + this.realPath + "}";
        }
    }

    /**
     * Initialize the cache for resource URLs. This method is called by {@link ServletEnvironmentCacheInitializer} when
     * the application is started to ensure that the cache is initialized only once the cache builder is available. The
     * cache builder depends on loading resources from this component, so we can't initialize the cache when it is first
     * requested.
     *
     * @since 17.5.0
     * @since 17.4.1
     * @since 16.10.9
     */
    synchronized void initializeCache()
    {
        if (this.resourceURLCache == null) {
            try {
                // The cache manager can't be injected directly as it depends on this component, so it is
                // important to only request it once this component has been initialized.
                CacheManager cacheManager = this.componentManager.getInstance(CacheManager.class);
                this.resourceURLCache = cacheManager
                    .createNewCache(new LRUCacheConfiguration("environment.servlet.resourceURLCache", 10000));
            } catch (Exception e) {
                this.logger.error("Failed to initialize the resource URL cache.", e);
            }
        }
    }

    /**
     * @param servletContext see {@link #getServletContext()}
     */
    public void setServletContext(javax.servlet.ServletContext servletContext)
    {
        this.javaxServletContext = servletContext;

        setServletContext(JakartaServletBridge.toJakarta(servletContext));
    }

    /**
     * @param servletContext see {@link #getServletContext()}
     */
    public void setServletContext(ServletContext servletContext)
    {
        this.jakartaServletContext = servletContext;

        // Various application servers have different behaviors when it comes to resource path interpretation. We are
        // trying to deduce what is the current behavior so that we can add workaround for wrong ones and always have a
        // consistent behavior
        // This only works with the WAR contains a prepared set of file to check it (like it is the case in the XWiki
        // WAR, for example). If not, fallback on the default behavior, which is to assume that the Servlet container
        // does not decode URL encoded characters (as it should, according to specifications).
        String content = getResourceAsStreamContent(ENCODED_RESOURCE_PATH);
        this.servletDecodeURL = content != null && Strings.CS.equals(content, DECODED_RESOURCE_CONTENT);

        // Check once if the real path is supported by the Servlet context so that we know if we can rely on it later to
        // check for path traversal attempts
        this.rootRealPath = withTrailingSlash(getJakartaServletContext().getRealPath(ROOT_PATH));
        this.realPathSupported = this.rootRealPath != null;
    }

    private String getResourceAsStreamContent(String path)
    {
        try (InputStream is = getJakartaServletContext().getResourceAsStream(path)) {
            if (is != null) {
                return IOUtils.toString(is, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            this.logger.warn(
                "Unable to read test resource to determine the servlet URL decoding behavior. Reason: [{}]",
                ExceptionUtils.getRootCauseMessage(e));
        }

        return null;
    }

    /**
     * @return the legacy Javax Servlet Context
     * @deprecated use {@link #getJakartaServletContext()} instead
     */
    @Deprecated(since = "17.0.0RC1")
    public javax.servlet.ServletContext getServletContext()
    {
        if (this.javaxServletContext == null) {
            this.javaxServletContext = JakartaServletBridge.toJavax(getJakartaServletContext());
        }

        return this.javaxServletContext;
    }

    /**
     * @return the Servlet Context
     * @since 17.0.0RC1
     */
    public ServletContext getJakartaServletContext()
    {
        if (this.jakartaServletContext == null) {
            throw new RuntimeException(
                "The Servlet Environment has not been properly initialized (The Servlet Context is not set)");
        }

        return this.jakartaServletContext;
    }

    private String normalizePath(String path)
    {
        // Some application servers decode URL encoded characters when looking for resources, which contradict Servlet
        // specifications
        String protectedPath = path;
        if (this.servletDecodeURL) {
            // The application server decodes URL encoded characters, so we need to double encode them
            protectedPath = path.replace("%", "%25");
        }

        return protectedPath;
    }

    private ResourceCacheEntry getResourceCacheEntry(String resourcePath)
    {
        if (this.resourceURLCache != null && this.cacheControl.isCacheReadAllowed()) {
            return this.resourceURLCache.get(resourcePath);
        }

        return null;
    }

    private void setResourceURLToCache(String resourcePath, URL url)
    {
        if (this.resourceURLCache != null) {
            ResourceCacheEntry cacheEntry = getResourceCacheEntry(resourcePath);

            if (cacheEntry == null) {
                cacheEntry = new ResourceCacheEntry();
            }

            cacheEntry.url = Optional.ofNullable(url);

            this.resourceURLCache.set(resourcePath, cacheEntry);
        }
    }

    private void setResourceRealPathToCache(String resourcePath, String realPath)
    {
        if (this.resourceURLCache != null) {
            ResourceCacheEntry cacheEntry = getResourceCacheEntry(resourcePath);

            if (cacheEntry == null) {
                cacheEntry = new ResourceCacheEntry();
            }

            cacheEntry.realPath = Optional.ofNullable(realPath);

            this.resourceURLCache.set(resourcePath, cacheEntry);
        }
    }

    private String withLeadingSlash(String path)
    {
        if (path != null && !Strings.CS.startsWith(path, SLASH)) {
            return SLASH + path;
        }

        return path;
    }

    private String withoutLeadingSlash(String path)
    {
        return Strings.CS.removeStart(path, SLASH);
    }

    private String withTrailingSlash(String path)
    {
        if (path != null && !Strings.CS.endsWith(path, SLASH)) {
            return path + SLASH;
        }

        return path;
    }

    private String normalizePrefixPath(String prefixPath)
    {
        if (Strings.CS.equals(prefixPath, ROOT_PATH)) {
            return prefixPath;
        }

        return withLeadingSlash(withTrailingSlash(prefixPath));
    }

    private String normalizeResourcePath(String resourcePath)
    {
        return withoutLeadingSlash(resourcePath);
    }

    // As we store Optional<URL> in the cache, we can have null Optional which is unavoidable.
    @SuppressWarnings("java:S2789")
    private String getRealPath(String resourcePath)
    {
        // Check if the resource path is the root path
        if (ROOT_PATH.equals(resourcePath)) {
            return this.rootRealPath;
        }

        // Search in the cache if we already know the real path for this resource
        ResourceCacheEntry cacheEntry = getResourceCacheEntry(resourcePath);
        if (cacheEntry != null && cacheEntry.realPath != null) {
            return cacheEntry.realPath.orElse(null);
        }

        // Get the real path from the Servlet context
        String realPath = null;
        try {
            realPath = getJakartaServletContext().getRealPath(normalizePath(resourcePath));
            if (resourcePath.endsWith(SLASH)) {
                // Make sure the real path reflect the fact that the resource is a directory
                realPath = withTrailingSlash(realPath);
            }
        } catch (IllegalArgumentException e) {
            // Some application servers (like Tomcat) already have a protection for paths trying to go out of the root
            // resource and will throw an IllegalArgumentException in that case. In our case we are just interested in
            // the fact that it's invalid (null) and we want to remember it in the cache.

            this.logger.warn("Failed to get the real path for [{}]", resourcePath, e);
        }

        // Make sure the resource real path is inside the root real path
        if (realPath != null && !Strings.CS.startsWith(realPath, this.rootRealPath)) {
            warnPathTraversalRoot(resourcePath);

            // Make the real path invalid
            realPath = null;
        }

        // Remember the real path
        setResourceRealPathToCache(resourcePath, realPath);

        return realPath;
    }

    private void warnPathTraversal(String normalizedPrefixPath, String normalizedFullPath)
    {
        if (normalizedPrefixPath.equals(ROOT_PATH)) {
            warnPathTraversalRoot(normalizedFullPath);
        } else {
            this.logger.warn(LOGGER_PATH_TRAVERSAL, normalizedFullPath, normalizedPrefixPath);
        }
    }

    private void warnPathTraversalRoot(String normalizedFullPath)
    {
        this.logger.warn(LOGGER_PATH_TRAVERSAL_ROOT, normalizedFullPath);
    }

    private boolean isValidRealPath(String normalizedPrefixPath, String normalizedFullPath)
    {
        // Get prefix real path
        String realPrefixPath = getRealPath(normalizedPrefixPath);
        if (realPrefixPath == null) {
            return false;
        }

        // Get full real path
        String realFullPath = getRealPath(normalizedFullPath);
        if (realFullPath == null) {
            return false;
        }

        if (!Strings.CS.startsWith(realFullPath, realPrefixPath)) {
            // Make full real path is not inside the prefix real path
            warnPathTraversal(normalizedPrefixPath, normalizedFullPath);

            return false;
        }

        return true;
    }

    private boolean isValidPathNormalization(String normalizedPrefixPath, String normalizedFullPath)
    {
        // Use a non empty path as root path (instead of "/") as otherwise it's not possible to properly check for
        // path traversal attempts.
        Path rootPath = Paths.get(withTrailingSlash(VIRTUAL_ROOT_PATH)).toAbsolutePath().normalize();

        // Make sure the prefix path is valid
        Path prefixPath = Paths.get(VIRTUAL_ROOT_PATH_PREFIX + normalizedPrefixPath).normalize();
        if (!prefixPath.startsWith(rootPath)) {
            warnPathTraversalRoot(normalizedPrefixPath);

            return false;
        }

        // Make sure the full path is valid
        Path fullPath = Paths.get(VIRTUAL_ROOT_PATH_PREFIX + normalizedFullPath).normalize();
        if (!fullPath.startsWith(prefixPath)) {
            warnPathTraversal(normalizedPrefixPath, normalizedFullPath);

            return false;
        }

        return true;
    }

    private boolean isValid(String normalizedPrefixPath, String normalizedFullPath)
    {
        // Relies on the real path to check for path traversal attempts first as it's more reliable than trying to do it
        // with Path normalization, but if it's not supported then try with Path normalization and hope for the best.
        // It's not perfect but at least it will cover most cases and it's better than nothing. Real path is slower
        // than Path normalization, but it should be cached so it should not have a big impact on performance.
        if (this.realPathSupported) {
            return isValidRealPath(normalizedPrefixPath, normalizedFullPath);
        }

        // If real path is not supported, try with Path normalization
        return isValidPathNormalization(normalizedPrefixPath, normalizedFullPath);
    }

    @Override
    public URL getResource(String resourcePath)
    {
        return getResource(ROOT_PATH, resourcePath);
    }

    // As we store Optional<URL> in the cache, we can have null Optional which is unavoidable.
    @SuppressWarnings("java:S2789")
    @Override
    public URL getResource(String prefixPath, String resourcePath)
    {
        String normalizedPrefixPath = normalizePrefixPath(prefixPath);
        String normalizedResourcePath = normalizeResourcePath(resourcePath);
        String normalizedFullPath = normalizedPrefixPath + normalizedResourcePath;

        // Check the cache
        ResourceCacheEntry cacheEntry = getResourceCacheEntry(normalizedFullPath);
        if (cacheEntry != null && cacheEntry.url != null) {
            // The resource URL is already in the cache, return it
            return cacheEntry.url.orElse(null);
        }

        URL url = null;

        // Check for path traversal attempts (to access resources outside of the specified prefix or outside the root)
        if (isValid(normalizedPrefixPath, normalizedFullPath)) {
            // Get the resource URL
            url = getResourceInternal(normalizedFullPath);
        }

        // Remember the URL in the cache
        setResourceURLToCache(normalizedFullPath, url);

        return url;
    }

    @Override
    public InputStream getResourceAsStream(String resourcePath)
    {
        return getResourceAsStream(ROOT_PATH, resourcePath);
    }

    @Override
    public InputStream getResourceAsStream(String prefixPath, String resourcePath)
    {
        String normalizedPrefixPath = normalizePrefixPath(prefixPath);
        String normalizedResourcePath = normalizeResourcePath(resourcePath);
        String normalizedFullPath = normalizedPrefixPath + normalizedResourcePath;

        // Check for path traversal attempts to access resources outside of the specified prefix
        if (isValid(normalizedPrefixPath, normalizedFullPath)) {
            // Get the resource
            return getJakartaServletContext().getResourceAsStream(normalizePath(normalizedFullPath));
        }

        return null;
    }

    private URL getResourceInternal(String resourcePath)
    {
        URL url;

        try {
            url = getJakartaServletContext().getResource(normalizePath(resourcePath));

            // ensure to normalize the URI, we don't want relative path.
            if (url != null) {
                url = url.toURI().normalize().toURL();
            }
            // We're catching IllegalArgumentException which might be thrown by Tomcat when trying to resolve path such
            // as `templates/../..`
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            this.logger.warn(LOGGER_INVALID_RESOURCE_PATH, resourcePath, e.getMessage());

            url = null;
        }

        return url;
    }

    @Override
    protected String getTemporaryDirectoryName()
    {
        final String tmpDirectory = super.getTemporaryDirectoryName();

        try {
            if (tmpDirectory == null) {
                File tempDir = (File) getJakartaServletContext().getAttribute(ServletContext.TEMPDIR);
                return tempDir == null ? null : tempDir.getCanonicalPath();
            }
        } catch (IOException e) {
            this.logger.warn("Unable to get Servlet temporary directory due to error [{}], "
                + "falling back on the default System temporary directory.", ExceptionUtils.getMessage(e));
        }

        return tmpDirectory;
    }
}
