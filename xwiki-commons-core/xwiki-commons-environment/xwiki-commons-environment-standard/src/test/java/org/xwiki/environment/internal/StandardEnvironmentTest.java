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

import javax.inject.Provider;
import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.environment.Environment;

/**
 * Unit tests for {@link StandardEnvironment}.
 *
 * @version $Id$
 * @since 3.5M1
 */
@RunWith(JMock.class)
public class StandardEnvironmentTest
{
    private static final File TMPDIR = new File(System.getProperty("java.io.tmpdir"), "xwiki-temp");

    private StandardEnvironment environment;

    private Mockery mockery = new JUnit4Mockery();

    public Mockery getMockery()
    {
        return this.mockery;
    }

    @Before
    public void setUp() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.initialize(getClass().getClassLoader());
        this.environment = (StandardEnvironment) ecm.getInstance(Environment.class);
        ReflectionUtils.setFieldValue(this.environment, "isTesting", true);
        if (TMPDIR.exists()) {
            FileUtils.forceDelete(TMPDIR);
        }
    }

    @After
    public void tearDown() throws Exception
    {
        if (TMPDIR.exists()) {
            FileUtils.forceDelete(TMPDIR);
        }
    }

    @Test
    public void testGetResourceWhenResourceDirNotSet()
    {
        Assert.assertNull(this.environment.getResource("doesntexist"));
    }

    @Test
    public void testGetResourceOk()
    {
        // Make sure our resource really exists on the file system...
        // TODO: find a better way...
        File resourceFile = new File("target/testGetResourceOk");
        resourceFile.mkdirs();

        this.environment.setResourceDirectory(new File("target"));
        URL resource = this.environment.getResource("testGetResourceOk");
        Assert.assertNotNull(resource);
    }

    @Test
    public void testGetResourceWhenResourceDirNotSetButResourceAvailableInDefaultClassLoader()
    {
        URL resource = this.environment.getResource("test");
        Assert.assertNotNull(resource);
    }

    @Test
    public void testGetResourceWhenResourceDirSetButResourceAvailableInDefaultClassLoader()
    {
        this.environment.setResourceDirectory(new File("/resource"));
        URL resource = this.environment.getResource("test");
        Assert.assertNotNull(resource);
    }

    @Test
    public void testGetPermanentDirectory()
    {
        File permanentDirectory = new File("/permanent");
        this.environment.setPermanentDirectory(permanentDirectory);
        Assert.assertEquals(permanentDirectory, this.environment.getPermanentDirectory());
    }

    @Test
    public void testGetPermanentDirectoryWhenNotSet()
    {
        // Also verify that we log a warning!
        final Logger logger = getMockery().mock(Logger.class);
        getMockery().checking(new Expectations() {{
            oneOf(logger).warn("No permanent directory configured. Using a temporary directory [{}]",
                               System.getProperty("java.io.tmpdir"));
        }});

        ReflectionUtils.setFieldValue(this.environment, "logger", logger);

        Assert.assertEquals(new File(System.getProperty("java.io.tmpdir")),
                            this.environment.getPermanentDirectory());
    }

    @Test
    public void testGetTemporaryDirectory()
    {
        File tmpDir = new File("tmpdir");
        this.environment.setTemporaryDirectory(tmpDir);
        Assert.assertEquals(tmpDir, this.environment.getTemporaryDirectory());
    }

    @Test
    public void testGetTemporaryDirectoryWhenNotSet()
    {
        Assert.assertEquals(TMPDIR, this.environment.getTemporaryDirectory());
    }

    @Test(expected = RuntimeException.class)
    public void testGetTemporaryDirectoryWhenNotADirectory() throws Exception
    {
        FileUtils.write(TMPDIR, "test");

        final String[] params = new String[] {
            "temporary",
            TMPDIR.getAbsolutePath(),
            "not a directory"
        };
        final Logger logger = getMockery().mock(Logger.class);
        getMockery().checking(new Expectations() {{
            oneOf(logger).error("Configured {} directory [{}] is {}.", params);
        }});
        ReflectionUtils.setFieldValue(this.environment, "logger", logger);

        this.environment.getTemporaryDirectory();
    }

    @Test
    public void testGetTemporaryDirectoryFailOver() throws Exception
    {
        FileUtils.forceMkdir(TMPDIR);
        final File txtFile = new File(TMPDIR, "test.txt");
        FileUtils.write(txtFile, "test");

        final Provider<EnvironmentConfiguration> prov = new Provider<EnvironmentConfiguration>() {
            public EnvironmentConfiguration get()
            {
                return new EnvironmentConfiguration() {
                    public String getPermanentDirectoryPath()
                    {
                        return txtFile.getAbsolutePath();
                    }
                };
            }
        };
        ReflectionUtils.setFieldValue(this.environment, "configurationProvider", prov);


        final Logger logger = getMockery().mock(Logger.class);
        getMockery().checking(new Expectations() {{
            allowing(logger).error(with(any(String.class)), with(any(String[].class)));
            // Getting the tmp dir causes the permenant dir to be checked.
            oneOf(logger).warn("Falling back on [{}] for {} directory.",
                               System.getProperty("java.io.tmpdir"),
                               "permanent");
        }});
        ReflectionUtils.setFieldValue(this.environment, "logger", logger);

        Assert.assertEquals(TMPDIR, this.environment.getTemporaryDirectory());

        // Check that the directory was cleared.
        Assert.assertEquals(0, TMPDIR.listFiles().length);
    }
}
