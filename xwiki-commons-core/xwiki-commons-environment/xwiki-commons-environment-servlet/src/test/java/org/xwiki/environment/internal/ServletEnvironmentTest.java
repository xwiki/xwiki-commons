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
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
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
import org.xwiki.cache.CacheManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
@SuppressWarnings({ "checkstyle:ClassFanOutComplexity", "checkstyle:MultipleStringLiterals" })
@ComponentTest
class ServletEnvironmentTest
{
    private File servletTmpDir;

    private File systemTmpDir;

    @MockComponent
    private CacheManager cacheManager;

    @Mock
    private Cache<Optional<URL>> cache;

    @MockComponent
    private CacheControl cacheControl;

    @InjectMockComponents
    private ServletEnvironment environment;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension();

    @BeforeEach
    public void setUp() throws Exception
    {
        this.servletTmpDir = new File(System.getProperty("java.io.tmpdir"), "ServletEnvironmentTest-tmpDir");
        this.systemTmpDir = new File(System.getProperty("java.io.tmpdir"), "xwiki-temp");

        doReturn(this.cache).when(this.cacheManager).createNewCache(any());
    }

    @AfterEach
    public void tearDown() throws Exception
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

    @Test
    void getResourceOk() throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        this.environment.setServletContext(servletContext);
        String resourceName = "/test";
        when(servletContext.getResource(resourceName)).thenReturn(new URL("file:/path/../test"));

        URL expectedURL = new URL("file:/test");
        assertEquals(expectedURL, this.environment.getResource(resourceName));
        verify(servletContext).getResource(resourceName);
        verify(this.cache).set(resourceName, Optional.of(expectedURL));
        // As cache control returns false, the cache shouldn't be read.
        verify(this.cache, never()).get(any());
    }

    @Test
    void getResourceAsStreamOk() throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        this.environment.setServletContext(servletContext);
        this.environment.getResourceAsStream("/test");

        verify(servletContext).getResourceAsStream("/test");
        // No cache for streams.
        verifyNoInteractions(this.cache);
    }

    @Test
    void getResourceNotExisting() throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        this.environment.setServletContext(servletContext);
        String resourceName = "unknown resource";
        assertNull(this.environment.getResource(resourceName));
        verify(this.cache).set(resourceName, Optional.empty());
        // As cache control returns false, the cache shouldn't be read.
        verify(this.cache, never()).get(any());
    }

    @Test
    void getResourceWhenMalformedURLException() throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        String resourceName = "bad resource";
        when(servletContext.getResource(resourceName)).thenThrow(new MalformedURLException("invalid url"));
        this.environment.setServletContext(servletContext);
        assertNull(this.environment.getResource(resourceName));
        assertEquals("Error getting resource [bad resource] because of invalid path format. Reason: [invalid url]",
            logCapture.getMessage(0));
        verify(this.cache).set(resourceName, Optional.empty());
        // As cache control returns false, the cache shouldn't be read.
        verify(this.cache, never()).get(any());
    }

    @Test
    void getResourceNotExistingFromCache()
    {
        String resourceName = "unknown resource";
        when(this.cacheControl.isCacheReadAllowed()).thenReturn(true);
        when(this.cache.get(resourceName)).thenReturn(Optional.empty());
        assertNull(this.environment.getResource(resourceName));
        verify(this.cache).get(resourceName);
        verify(this.cache, never()).set(any(), any());
    }

    @Test
    void getResourceExistingFromCache()
    {
        String resourceName = "known resource";
        URL expectedURL = mock(URL.class);
        when(this.cacheControl.isCacheReadAllowed()).thenReturn(true);
        when(this.cache.get(resourceName)).thenReturn(Optional.of(expectedURL));
        assertEquals(expectedURL, this.environment.getResource(resourceName));
        verify(this.cache).get(resourceName);
        verify(this.cache, never()).set(any(), any());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void getResourceWithRecursiveCallInCacheInitialization(boolean cached) throws Exception
    {
        ServletContext servletContext = mock(ServletContext.class);
        String resourceName = "/resource";
        when(servletContext.getResource(resourceName)).thenReturn(new URL("file:/path/../resource"));
        this.environment.setServletContext(servletContext);

        when(this.cacheControl.isCacheReadAllowed()).thenReturn(true);

        URL cachedURL = mock();
        if (cached) {
            when(this.cache.get(resourceName)).thenReturn(Optional.of(cachedURL));
        }

        CompletableFuture<Void> arrivedInCreateCacheFuture = new CompletableFuture<>();
        CompletableFuture<Void> blockCreateCacheFuture = new CompletableFuture<>();

        Mutable<URL> recursiveURL = new MutableObject<>();

        doAnswer(invocationOnMock -> {
            arrivedInCreateCacheFuture.complete(null);
            recursiveURL.setValue(this.environment.getResource(resourceName));
            blockCreateCacheFuture.get(20, TimeUnit.SECONDS);
            return this.cache;
        }).when(this.cacheManager).createNewCache(any());

        // Launch a background thread so we can test blocking in the creation of the cache.
        ExecutorService executor = Executors.newFixedThreadPool(1);

        try {
            CompletableFuture<URL> outerCall =
                CompletableFuture.supplyAsync(() -> this.environment.getResource(resourceName), executor);

            arrivedInCreateCacheFuture.get(20, TimeUnit.SECONDS);

            URL expectedURL = new URL("file:/resource");
            // Ensure that the cache creation doesn't block getting the resource URL.
            assertEquals(expectedURL, this.environment.getResource(resourceName));

            blockCreateCacheFuture.complete(null);

            // Ensure that the blocked call now got the value, too.
            URL actualOuterURL = outerCall.get(20, TimeUnit.SECONDS);
            if (cached) {
                assertEquals(cachedURL, actualOuterURL);
            } else {
                assertEquals(expectedURL, actualOuterURL);
            }

            // Assert that the recursive call also got the URL.
            assertEquals(expectedURL, recursiveURL.getValue());

            // Only the outer call should have accessed the cache. If the cache didn't return the value, it should
            // have been stored.
            verify(this.cache).get(resourceName);
            if (cached) {
                verify(this.cache, never()).set(any(), any());
            } else {
                verify(this.cache).set(resourceName, Optional.of(expectedURL));
            }
            verify(servletContext, times(cached ? 2 : 3)).getResource(resourceName);
        } finally {
            executor.shutdownNow();
        }
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

        assertEquals(this.systemTmpDir.getCanonicalFile(),
            this.environment.getTemporaryDirectory().getCanonicalFile());

        // Verify that servletContext.getAttribute was called (and that we returned null - this happens because we
        // didn't set any stubbing on servletContext and null is the default returned by Mockito).
        verify(servletContext).getAttribute("jakarta.servlet.context.tempdir");
    }
}
