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

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.environment.Environment;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for {@link ServletEnvironment}.
 *
 * @version $Id$
 * @since 3.5M1
 */
public class ServletEnvironmentTest
{
    private File servletTmpDir;

    private File systemTmpDir;

    private ServletEnvironment environment;

    private Mockery mockery = new JUnit4Mockery();

    public Mockery getMockery()
    {
        return this.mockery;
    }

    @Before
    public void setUp() throws Exception
    {
        this.servletTmpDir = new File(System.getProperty("java.io.tmpdir"), "ServletEnvironmentTest-tmpDir");
        this.systemTmpDir = new File(System.getProperty("java.io.tmpdir"), "xwiki-temp");

        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.initialize(getClass().getClassLoader());
        this.environment = (ServletEnvironment) ecm.getInstance(Environment.class);
    }

    @After
    public void tearDown() throws Exception
    {
        FileUtils.deleteQuietly(this.servletTmpDir);
    }

    @Test
    public void testGetResourceWhenServletContextNotSet()
    {
        try {
            this.environment.getResource("/whatever");
            Assert.fail();
        } catch (RuntimeException expected) {
            Assert.assertEquals("The Servlet Environment has not been properly initialized "
                + "(The Servlet Context is not set)", expected.getMessage());
        }
    }
    
    @Test
    public void testGetResourceOk() throws Exception
    {
        final ServletContext servletContext = getMockery().mock(ServletContext.class);
        getMockery().checking(new Expectations() {{
            // This is the test!
            oneOf(servletContext).getResource("/test");
        }});
        this.environment.setServletContext(servletContext);

        this.environment.getResource("/test");
    }

    @Test
    public void testGetResourceAsStreamOk() throws Exception
    {
        final ServletContext servletContext = getMockery().mock(ServletContext.class);
        getMockery().checking(new Expectations() {{
            // This is the test!
            oneOf(servletContext).getResourceAsStream("/test");
        }});
        this.environment.setServletContext(servletContext);

        this.environment.getResourceAsStream("/test");
    }


    @Test
    public void testGetPermanentDirectoryWhenSetWithAPI() throws Exception
    {
        File permanentDirectory = new File("/permanent");
        this.environment.setPermanentDirectory(permanentDirectory);
        Assert.assertEquals(permanentDirectory.getCanonicalFile(),
            this.environment.getPermanentDirectory().getCanonicalFile());
    }

    @Test
    public void testGetPermanentDirectoryWhenSetWithSystemProperty() throws Exception
    {
        File expectedPermanentDirectory = new File(System.getProperty("java.io.tmpdir"), "permanent");
        System.setProperty("xwiki.data.dir", expectedPermanentDirectory.toString());

        try {
            final ServletContext servletContext = getMockery().mock(ServletContext.class);
            getMockery().checking(new Expectations() {{
                oneOf(servletContext).getAttribute("javax.servlet.context.tempdir");
                will(returnValue(servletTmpDir));
            }});
            this.environment.setServletContext(servletContext);

            Assert.assertEquals(expectedPermanentDirectory.getCanonicalFile(),
                this.environment.getPermanentDirectory().getCanonicalFile());
        } finally {
            System.clearProperty("xwiki.data.dir");
        }
    }

    @Test
    public void testGetPermanentDirectoryWhenNotSet() throws Exception
    {
        final ServletContext servletContext = getMockery().mock(ServletContext.class);
        getMockery().checking(new Expectations() {{
            oneOf(servletContext).getAttribute("javax.servlet.context.tempdir");
            will(returnValue(servletTmpDir));
        }});
        this.environment.setServletContext(servletContext);

        // Also verify that we log a warning!
        final Logger logger = getMockery().mock(Logger.class);
        getMockery().checking(new Expectations() {{
            oneOf(logger).warn(with(startsWith("No permanent directory configured. Using temporary directory")),
                with(any(String.class)));
        }});
        ReflectionUtils.setFieldValue(this.environment, "logger", logger);

        Assert.assertEquals(this.servletTmpDir.getCanonicalFile(),
            this.environment.getPermanentDirectory().getCanonicalFile());
    }

    @Test
    public void testGetTemporaryDirectory() throws Exception
    {
        File tmpDir = new File("tmpdir");
        this.environment.setTemporaryDirectory(tmpDir);
        Assert.assertEquals(tmpDir.getCanonicalFile(), this.environment.getTemporaryDirectory().getCanonicalFile());
    }

    @Test
    public void testGetTemporaryDirectoryWhenNotSet() throws Exception
    {
        final ServletContext servletContext = getMockery().mock(ServletContext.class);
        getMockery().checking(new Expectations() {{
            atLeast(1).of(servletContext).getAttribute("javax.servlet.context.tempdir");
            will(returnValue(servletTmpDir));
        }});

        this.environment.setServletContext(servletContext);

        final File tmpDir = this.environment.getTemporaryDirectory();

        // Make sure it is the "xwiki-temp" dir which is under the main temp dir.
        Assert.assertEquals(this.servletTmpDir.listFiles()[0].getCanonicalFile(), tmpDir.getCanonicalFile());
    }

    /**
     * Verify we default to the system tmp dir if the Servlet tmp dir is not set.
     */
    @Test
    public void testGetTemporaryDirectoryWhenServletTempDirNotSet() throws Exception
    {
        final ServletContext servletContext = getMockery().mock(ServletContext.class);
        getMockery().checking(new Expectations() {{
            allowing(servletContext).getAttribute("javax.servlet.context.tempdir");
            will(returnValue(null));
        }});
        this.environment.setServletContext(servletContext);

        Assert.assertEquals(this.systemTmpDir.getCanonicalFile(),
            this.environment.getTemporaryDirectory().getCanonicalFile());
    }

}
