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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import jakarta.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheControl;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.internal.MapCache;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.environment.internal.ServletEnvironment.ResourceCacheEntry;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServletEnvironment}.
 *
 * @version $Id$
 * @since 3.5M1
 */
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "checkstyle:MultipleStringLiterals"})
@ComponentTest
@ComponentList(ServletEnvironmentConfiguration.class)
class ServletEnvironmentTest
{
    private File servletTmpDir;

    private File systemTmpDir;

    @MockComponent
    private CacheManager cacheManager;

    @Mock
    private Cache<ResourceCacheEntry> cache;

    @MockComponent
    @Named("restricted")
    private ConfigurationSource configurationSource;

    @MockComponent
    private CacheControl cacheControl;

    @InjectMockComponents
    private ServletEnvironment environment;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension();

    @BeforeEach
    void beforeEach() throws Exception
    {
        this.servletTmpDir = new File(System.getProperty("java.io.tmpdir"), "ServletEnvironmentTest-tmpDir");
        this.systemTmpDir = new File(System.getProperty("java.io.tmpdir"), "xwiki-temp");

        doReturn(this.cache).when(this.cacheManager).createNewCache(any());
        when(this.configurationSource.getProperty("environment.servlet.allowedRealPaths", List.of("/etc/xwiki/")))
            .thenReturn(List.of("/etc/xwiki/"));
    }

    @AfterEach
    void afterEach()
    {
        FileUtils.deleteQuietly(this.servletTmpDir);
    }

    @Test
    void getResourceWhenServletContextNotSet()
    {
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            this.environment.getResource("/whatever");
        });
        assertEquals("The Servlet Environment has not been properly initialized (The Servlet Context is not set)",
            exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getResourceOk(boolean initializeCache) throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        this.environment.setServletContext(servletContext);
        String resourceName = "/test";
        when(servletContext.getResource(resourceName)).thenReturn(new URI("file:/path/../test").toURL());
        if (initializeCache) {
            this.environment.initializeCache();
        }

        URL expectedURL = new URI("file:/test").toURL();
        assertEquals(expectedURL, this.environment.getResource(resourceName));
        verify(servletContext).getResource(resourceName);
        if (initializeCache) {
            verify(this.cache).set(resourceName, new ResourceCacheEntry(Optional.of(expectedURL), null));
            // As cache control returns false, the cache shouldn't be read.
            verify(this.cache, never()).get(any());
        } else {
            verifyNoInteractions(this.cache);
        }
    }

    @Test
    void getResourceWithURLEncodedCharactersDecodingApplicationServer() throws MalformedURLException
    {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getResourceAsStream(ServletEnvironment.ENCODED_RESOURCE_PATH))
            .thenReturn(IOUtils.toInputStream(ServletEnvironment.DECODED_RESOURCE_CONTENT, StandardCharsets.UTF_8));
        this.environment.setServletContext(servletContext);

        this.environment.getResource("a%61b");

        verify(servletContext).getResource("/a%2561b");
    }

    @Test
    void getResourceWithURLEncodedCharactersWhenSpecsApplicationServer() throws MalformedURLException
    {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getResourceAsStream(ServletEnvironment.ENCODED_RESOURCE_PATH))
            .thenReturn(IOUtils.toInputStream(ServletEnvironment.ENCODED_RESOURCE_CONTENT, StandardCharsets.UTF_8));
        this.environment.setServletContext(servletContext);

        this.environment.getResource("a%61b");

        verify(servletContext).getResource("/a%61b");
    }

    @Test
    void getResourceWithURLEncodedCharactersWhenUnknownApplicationServer() throws MalformedURLException
    {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getResourceAsStream(ServletEnvironment.ENCODED_RESOURCE_PATH)).thenReturn(null);
        this.environment.setServletContext(servletContext);

        this.environment.getResource("a%61b");

        verify(servletContext).getResource("/a%61b");
    }

    @Test
    void getResourceAsStreamOk()
    {
        ServletContext servletContext = mock(ServletContext.class);
        this.environment.setServletContext(servletContext);
        this.environment.getResourceAsStream("/test");

        verify(servletContext).getResourceAsStream("/test");
        // No cache for streams.
        verifyNoInteractions(this.cache);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getResourceNotExisting(boolean initializeCache)
    {
        ServletContext servletContext = mock();
        this.environment.setServletContext(servletContext);
        String resourceName = "/unknown resource";
        if (initializeCache) {
            this.environment.initializeCache();
        }

        assertNull(this.environment.getResource(resourceName));
        if (initializeCache) {
            verify(this.cache).set(resourceName, new ResourceCacheEntry(Optional.empty(), null));
            // As cache control returns false, the cache shouldn't be read.
            verify(this.cache, never()).get(any());
        } else {
            verifyNoInteractions(this.cache);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getResourceWhenMalformedURLException(boolean initializeCache) throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        String resourceName = "/bad resource";
        when(servletContext.getResource(resourceName)).thenThrow(new MalformedURLException("invalid url"));
        this.environment.setServletContext(servletContext);
        if (initializeCache) {
            this.environment.initializeCache();
        }
        assertNull(this.environment.getResource(resourceName));
        assertEquals("Error getting resource [/bad resource] because of invalid path format. Reason: [invalid url]",
            logCapture.getMessage(0));
        if (initializeCache) {
            verify(this.cache).set(resourceName, new ResourceCacheEntry(Optional.empty(), null));
            // As cache control returns false, the cache shouldn't be read.
            verify(this.cache, never()).get(any());
        } else {
            verifyNoInteractions(this.cache);
        }
    }

    @Test
    void getResourceNotExistingFromCache()
    {
        String resourceName = "/unknown resource";
        this.environment.initializeCache();
        when(this.cacheControl.isCacheReadAllowed()).thenReturn(true);
        when(this.cache.get(resourceName)).thenReturn(new ResourceCacheEntry(Optional.empty(), null));
        assertNull(this.environment.getResource(resourceName));
        verify(this.cache).get(resourceName);
        verify(this.cache, never()).set(any(), any());
    }

    @Test
    void getResourceExistingFromCache()
    {
        String resourceName = "/known resource";
        URL expectedURL = mock(URL.class);
        this.environment.initializeCache();
        when(this.cacheControl.isCacheReadAllowed()).thenReturn(true);
        when(this.cache.get(resourceName)).thenReturn(new ResourceCacheEntry(Optional.of(expectedURL), null));
        assertEquals(expectedURL, this.environment.getResource(resourceName));
        verify(this.cache).get(resourceName);
        verify(this.cache, never()).set(any(), any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getResourceWithRecursiveCallInCacheInitialization(boolean cached) throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        String resourceName = "/resource";
        when(servletContext.getResource(resourceName)).thenReturn(new URI("file:/path/../resource").toURL());
        this.environment.setServletContext(servletContext);

        when(this.cacheControl.isCacheReadAllowed()).thenReturn(true);

        URL cachedURL = mock();
        if (cached) {
            when(this.cache.get(resourceName)).thenReturn(new ResourceCacheEntry(Optional.of(cachedURL), null));
        }

        // Two futures to synchronize the background thread that creates the cache.
        CompletableFuture<Void> arrivedInCreateCacheFuture = new CompletableFuture<>();
        CompletableFuture<Void> blockCreateCacheFuture = new CompletableFuture<>();

        Mutable<URL> recursiveURL = new MutableObject<>();

        doAnswer(invocationOnMock -> {
            // Signal that the background thread arrived in the cache creation call.
            arrivedInCreateCacheFuture.complete(null);
            recursiveURL.setValue(this.environment.getResource(resourceName));
            // Wait until the main thread unblocks the cache creation (but don't wait forever just to be safe).
            blockCreateCacheFuture.get(20, TimeUnit.SECONDS);
            return this.cache;
        }).when(this.cacheManager).createNewCache(any());

        // Launch a background thread so we can test blocking in the creation of the cache.
        ExecutorService executor = Executors.newFixedThreadPool(1);

        try {
            CompletableFuture<Void> initializeCall = CompletableFuture.supplyAsync(() -> {
                this.environment.initializeCache();
                return null;
            }, executor);

            // Wait for the background thread to arrive in the cache creation call (but don't wait forever just to be
            // safe).
            arrivedInCreateCacheFuture.get(20, TimeUnit.SECONDS);

            URL expectedURL = new URI("file:/resource").toURL();
            // Ensure that the cache creation doesn't block getting the resource URL.
            assertEquals(expectedURL, this.environment.getResource(resourceName));

            // Unblock the cache creation call.
            blockCreateCacheFuture.complete(null);

            // Ensure that the blocked call now completed. Again, don't wait forever just in case.
            initializeCall.get(20, TimeUnit.SECONDS);

            // Assert that the recursive call got the URL.
            assertEquals(expectedURL, recursiveURL.get());

            // Assert that we now get the cached URL.
            assertEquals(cached ? cachedURL : expectedURL, this.environment.getResource(resourceName));

            // Only the last call should have accessed the cache. If the cache didn't return the value, it should
            // have been stored. Plus setting the cache also check if an entry has been added in the meantime.
            if (cached) {
                verify(this.cache).get(resourceName);
                verify(this.cache, never()).set(any(), any());
            } else {
                verify(this.cache, times(2)).get(resourceName);
                verify(this.cache).set(resourceName, new ResourceCacheEntry(Optional.of(expectedURL), null));
            }
            verify(servletContext, times(cached ? 2 : 3)).getResource(resourceName);
        } finally {
            executor.shutdownNow();
        }
    }

    private Object getResource(String resourcePath, boolean isGetResource)
    {
        return isGetResource ? this.environment.getResource(resourcePath)
            : this.environment.getResourceAsStream(resourcePath);
    }

    private Object getResource(String prefixPath, String resourcePath, boolean isGetResource)
    {
        return isGetResource ? this.environment.getResource(prefixPath, resourcePath)
            : this.environment.getResourceAsStream(prefixPath, resourcePath);
    }

    @Test
    void getNotExistingResource()
    {
        ServletContext servletContext = mock(ServletContext.class);
        this.environment.setServletContext(servletContext);

        assertNull(this.environment.getResource("prefix", "resource"));
        assertNull(this.environment.getResource("prefix", "resource2"));
    }

    @Test
    void getNotExistingResourceWithRealPathEnabledAndCache() throws CacheException
    {
        MapCache<ResourceCacheEntry> testCache = new MapCache<>();
        when(this.cacheManager.<ResourceCacheEntry>createNewCache(any())).thenReturn(testCache);
        this.environment.initializeCache();
        when(this.cacheControl.isCacheReadAllowed()).thenReturn(true);

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/")).thenReturn("/real/path");
        this.environment.setServletContext(servletContext);

        assertNull(this.environment.getResource("prefix", "resource"));
        assertNull(this.environment.getResource("prefix", "resource2"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getResourceWithPathTraversalWithoutRealPathSupport(boolean isGetResource) throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        this.environment.setServletContext(servletContext);
        verify(servletContext, times(1)).getResourceAsStream(any());

        assertNull(getResource("../resource", isGetResource));
        assertEquals("The resource path [/../resource] is trying to access a resource outside of the resource root.",
            this.logCapture.getMessage(0));

        assertNull(getResource("../prefix", "resource", isGetResource));
        assertEquals("The resource path [/../prefix/] is trying to access a resource outside of the resource root.",
            this.logCapture.getMessage(1));

        assertNull(getResource("/prefix/", "../resource", isGetResource));
        assertEquals(
            "The resource path [/prefix/../resource] is trying to access a resource outside of the specified prefix [/prefix/].",
            this.logCapture.getMessage(2));

        // Make sure we stopped before asking for the actual resource (getResourceAsStream is called once in the init)
        verify(servletContext, never()).getResource(any());
        verify(servletContext, times(1)).getResourceAsStream(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getResourceWithPathTraversalWithRealPathSupport(boolean isGetResource) throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/")).thenReturn("/real/path");
        this.environment.setServletContext(servletContext);
        this.environment.initializeCache();
        verify(servletContext, times(1)).getResourceAsStream(any());

        assertNull(getResource("invalidpath", isGetResource));

        when(servletContext.getRealPath("/../resource")).thenReturn("/real/resource");
        assertNull(getResource("../resource", isGetResource));
        assertEquals(
            "The resource path [/../resource] is trying to access a resource outside of the resource root. It's expected to be located inside one of the allowed real locations [/real/path/, /etc/xwiki/], but its real location is [/real/resource]. If this should actually be an allowed location, you can add it to the property 'environment.servlet.allowedRealPaths' in the configuration file 'xwiki.properties'.",
            this.logCapture.getMessage(0));
        verify(this.cache).set("/../resource", new ResourceCacheEntry(null, Optional.empty()));

        when(servletContext.getRealPath("/../prefix/")).thenReturn("/real/prefix");
        assertNull(getResource("../prefix", "resource", isGetResource));
        assertEquals(
            "The resource path [/../prefix/] is trying to access a resource outside of the resource root. It's expected to be located inside one of the allowed real locations [/real/path/, /etc/xwiki/], but its real location is [/real/prefix/]. If this should actually be an allowed location, you can add it to the property 'environment.servlet.allowedRealPaths' in the configuration file 'xwiki.properties'.",
            this.logCapture.getMessage(1));
        verify(this.cache).set("/../prefix/", new ResourceCacheEntry(null, Optional.empty()));

        when(servletContext.getRealPath("/prefix/")).thenReturn("/real/path/prefix");
        when(servletContext.getRealPath("/prefix/../resource")).thenReturn("/real/path/resource");
        assertNull(getResource("/prefix/", "../resource", isGetResource));
        assertEquals(
            "The resource path [/prefix/../resource] is trying to access a resource outside of the specified prefix [/prefix/]. It's expected to be inside the prefix real location [/real/path/prefix/], but its real location is [/real/path/resource].",
            this.logCapture.getMessage(2));
        verify(this.cache).set("/prefix/", new ResourceCacheEntry(null, Optional.of("/real/path/prefix/")));
        verify(this.cache).set("/prefix/../resource", new ResourceCacheEntry(null, Optional.of("/real/path/resource")));

        // Make sure we don't cache the root real path
        verify(this.cache, never()).set(eq("/"), any());
        // Make sure we stopped before asking for the actual resource
        verify(servletContext, never()).getResource(any());
        verify(servletContext, times(1)).getResourceAsStream(any());
    }

    @Test
    void getResourceWithPathTraversalWithRealPathSupport() throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/")).thenReturn("/real/path");
        this.environment.setServletContext(servletContext);
        this.environment.initializeCache();
        verify(servletContext, times(1)).getResourceAsStream(any());

        when(servletContext.getResource("/resource")).thenReturn(new URI("file:/real/path/resource").toURL());

        when(servletContext.getRealPath("/resource")).thenReturn("/real/path/resource");
        assertEquals("file:/real/path/resource", this.environment.getResource("/resource").toString());

        when(servletContext.getRealPath("/resource")).thenReturn("/etc/xwiki/resource");
        assertEquals("file:/real/path/resource", this.environment.getResource("/resource").toString());
    }

    @Test
    void getPermanentDirectoryWhenSetWithAPI() throws Exception
    {
        File permanentDirectory = new File("/permanent");
        this.environment.setPermanentDirectory(permanentDirectory);

        assertEquals(permanentDirectory.getCanonicalFile(),
            this.environment.getPermanentDirectory().getCanonicalFile());
    }

    @Test
    void getPermanentDirectoryWhenSetWithSystemProperty() throws Exception
    {
        Logger logger = mock(Logger.class);
        ReflectionUtils.setFieldValue(this.environment, "logger", logger);

        File expectedPermanentDirectory = new File(System.getProperty("java.io.tmpdir"), "permanent");
        System.setProperty("xwiki.data.dir", expectedPermanentDirectory.toString());

        try {
            this.environment.setServletContext(mock(ServletContext.class));

            assertEquals(expectedPermanentDirectory.getCanonicalFile(),
                this.environment.getPermanentDirectory().getCanonicalFile());
        } finally {
            System.clearProperty("xwiki.data.dir");
        }

        verify(logger).info("Using permanent directory [{}]", expectedPermanentDirectory);
    }

    @Test
    void getPermanentDirectoryWhenNotSet() throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getAttribute("jakarta.servlet.context.tempdir")).thenReturn(this.servletTmpDir);
        this.environment.setServletContext(servletContext);

        Logger logger = mock(Logger.class);
        ReflectionUtils.setFieldValue(this.environment, "logger", logger);

        assertEquals(this.servletTmpDir.getCanonicalFile(),
            this.environment.getPermanentDirectory().getCanonicalFile());

        // Also verify that we log a warning!
        verify(logger).warn("No permanent directory configured, fallbacking to temporary directory. You should set "
            + "the \"environment.permanentDirectory\" configuration property in the xwiki.properties file.");
        verify(logger).info("Using permanent directory [{}]", this.servletTmpDir.getCanonicalFile());
    }

    @Test
    void getTemporaryDirectory() throws Exception
    {
        File tmpDir = new File("tmpdir");
        this.environment.setTemporaryDirectory(tmpDir);
        assertEquals(tmpDir.getCanonicalFile(), this.environment.getTemporaryDirectory().getCanonicalFile());
    }

    @Test
    void getTemporaryDirectoryWhenNotSet() throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getAttribute("jakarta.servlet.context.tempdir")).thenReturn(this.servletTmpDir);
        this.environment.setServletContext(servletContext);

        File tmpDir = this.environment.getTemporaryDirectory();

        // Make sure it is the "xwiki-temp" dir which is under the main temp dir.
        assertEquals(this.servletTmpDir.listFiles()[0].getCanonicalFile(), tmpDir.getCanonicalFile());
    }

    /**
     * Verify we default to the system tmp dir if the Servlet tmp dir is not set.
     */
    @Test
    void getTemporaryDirectoryWhenServletTempDirNotSet() throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        this.environment.setServletContext(servletContext);

        assertEquals(this.systemTmpDir.getCanonicalFile(), this.environment.getTemporaryDirectory().getCanonicalFile());

        // Verify that servletContext.getAttribute was called (and that we returned null - this happens because we
        // didn't set any stubbing on servletContext and null is the default returned by Mockito).
        verify(servletContext).getAttribute("jakarta.servlet.context.tempdir");
    }
}
