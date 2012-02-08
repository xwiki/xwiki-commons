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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
public class StandardEnvironmentTest
{
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
        this.environment = (StandardEnvironment) ecm.lookup(Environment.class);
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
        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        
        // Also verify that we log a warning!
        final Logger logger = getMockery().mock(Logger.class);
        getMockery().checking(new Expectations() {{
            oneOf(logger).warn("No permanent directory configured. Using a temporary directory [{}]", tmpDir);
        }});

        ReflectionUtils.setFieldValue(this.environment, "logger", logger);

        Assert.assertEquals(tmpDir, this.environment.getPermanentDirectory());
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
        Assert.assertEquals(new File(System.getProperty("java.io.tmpdir")), this.environment.getTemporaryDirectory());
    }
}
