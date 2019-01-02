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
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StandardEnvironment}.
 *
 * @version $Id$
 * @since 3.5M1
 */
@ComponentTest
public class StandardEnvironmentTest
{
    private static final File TMPDIR = new File(System.getProperty("java.io.tmpdir"), "xwiki-temp");

    @InjectMockComponents
    private StandardEnvironment environment;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.INFO);

    @BeforeEach
    public void setUp() throws Exception
    {
        if (TMPDIR.exists()) {
            FileUtils.forceDelete(TMPDIR);
        }
    }

    @AfterEach
    public void tearDown() throws Exception
    {
        if (TMPDIR.exists()) {
            FileUtils.forceDelete(TMPDIR);
        }
    }

    @Test
    public void getResourceWhenResourceDirNotSet()
    {
        assertNull(this.environment.getResource("doesntexist"));
    }

    @Test
    public void getResourceOk()
    {
        // Make sure our resource really exists on the file system...
        // TODO: find a better way...
        File resourceFile = new File("target/getResourceOk");
        resourceFile.mkdirs();

        this.environment.setResourceDirectory(new File("target"));
        URL resource = this.environment.getResource("getResourceOk");
        assertNotNull(resource);
    }

    @Test
    public void getResourceWhenResourceDirNotSetButResourceAvailableInDefaultClassLoader()
    {
        URL resource = this.environment.getResource("test");
        assertNotNull(resource);
    }

    @Test
    public void getResourceWhenResourceDirSetButResourceAvailableInDefaultClassLoader()
    {
        this.environment.setResourceDirectory(new File("/resource"));
        URL resource = this.environment.getResource("test");
        assertNotNull(resource);
    }

    @Test
    public void getPermanentDirectory()
    {
        File permanentDirectory = new File("/permanent");
        this.environment.setPermanentDirectory(permanentDirectory);
        assertEquals(permanentDirectory, this.environment.getPermanentDirectory());
    }

    @Test
    public void getConfiguredPermanentDirectory(MockitoComponentManager componentManager) throws Exception
    {
        File persistentDir =
            new File(System.getProperty("java.io.tmpdir"), "xwiki-test-persistentDir").getAbsoluteFile();
        EnvironmentConfiguration configuration = componentManager.getInstance(EnvironmentConfiguration.class);
        when(configuration.getPermanentDirectoryPath()).thenReturn(persistentDir.getAbsolutePath());

        assertEquals(persistentDir, this.environment.getPermanentDirectory());

        assertEquals(1, this.logCapture.size());
        assertEquals(String.format("Using permanent directory [%s]", persistentDir), this.logCapture.getMessage(0));
    }

    @Test
    public void getPermanentDirectoryWhenNotSet()
    {
        assertEquals(new File(System.getProperty("java.io.tmpdir")), this.environment.getPermanentDirectory());

        assertEquals(2, this.logCapture.size());
        assertEquals(Level.WARN, this.logCapture.getLogEvent(0).getLevel());
        assertEquals("No permanent directory configured, fallbacking to temporary directory. You should set the "
                + "\"environment.permanentDirectory\" configuration property in the xwiki.properties file.",
            this.logCapture.getMessage(0));
        assertEquals(Level.INFO, this.logCapture.getLogEvent(1).getLevel());
        assertEquals(String.format("Using permanent directory [%s]",
            new File(System.getProperty("java.io.tmpdir"))), this.logCapture.getMessage(1));
    }

    @Test
    public void getTemporaryDirectory()
    {
        File tmpDir = new File("tmpdir");
        this.environment.setTemporaryDirectory(tmpDir);
        assertEquals(tmpDir, this.environment.getTemporaryDirectory());
    }

    @Test
    public void getTemporaryDirectoryWhenNotSet()
    {
        assertEquals(TMPDIR, this.environment.getTemporaryDirectory());
    }

    @Test
    public void getTemporaryDirectoryWhenNotADirectory() throws Exception
    {
        FileUtils.write(TMPDIR, "test", "UTF-8");

        Throwable exception = assertThrows(RuntimeException.class, () -> {
            this.environment.getTemporaryDirectory();
        });
        assertEquals("Could not find a writable temporary directory. Check the server logs for more information.",
            exception.getMessage());

        assertEquals(1, this.logCapture.size());
        assertEquals(String.format("Configured temporary directory [%s] is not a directory.",
            TMPDIR.getAbsolutePath()), this.logCapture.getMessage(0));
    }

    @Test
    public void getTemporaryDirectoryFailOver(MockitoComponentManager componentManager) throws Exception
    {
        FileUtils.forceMkdir(TMPDIR);
        File txtFile = new File(TMPDIR, "test.txt");
        FileUtils.write(txtFile, "test", "UTF-8");

        EnvironmentConfiguration configuration = componentManager.getInstance(EnvironmentConfiguration.class);
        when(configuration.getPermanentDirectoryPath()).thenReturn(txtFile.getAbsolutePath());

        assertEquals(TMPDIR, this.environment.getTemporaryDirectory());

        // Check that the directory was cleared.
        assertEquals(0, TMPDIR.listFiles().length);
    }
}
