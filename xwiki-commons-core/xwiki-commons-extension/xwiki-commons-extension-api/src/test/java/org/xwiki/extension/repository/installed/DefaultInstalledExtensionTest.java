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
package org.xwiki.extension.repository.installed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.repository.internal.installed.DefaultInstalledExtension;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;

public class DefaultInstalledExtensionTest
{
    private DefaultInstalledExtension installedExtension;

    @Before
    public void setUp()
    {
        DefaultLocalExtension localExtension =
            new DefaultLocalExtension(null, new ExtensionId("installed", "version"), "type");
        this.installedExtension = new DefaultInstalledExtension(localExtension, null);
    }

    @Test
    public void testIsInstalled()
    {
        Assert.assertFalse(this.installedExtension.isInstalled());
        Assert.assertFalse(this.installedExtension.isInstalled("namespace"));

        this.installedExtension.setInstalled(true);

        Assert.assertTrue(this.installedExtension.isInstalled());
        Assert.assertTrue(this.installedExtension.isInstalled("namespace"));

        this.installedExtension.setInstalled(true, "namespace");

        Assert.assertTrue(this.installedExtension.isInstalled());
        Assert.assertTrue(this.installedExtension.isInstalled("namespace"));

        this.installedExtension.setInstalled(false);

        Assert.assertFalse(this.installedExtension.isInstalled());
        Assert.assertFalse(this.installedExtension.isInstalled("namespace"));
    }

    @Test
    public void testIsValid()
    {
        Assert.assertTrue(this.installedExtension.isValid(null));
        Assert.assertTrue(this.installedExtension.isValid("namespace"));

        this.installedExtension.setValid(null, false);

        Assert.assertFalse(this.installedExtension.isValid(null));
        Assert.assertTrue(this.installedExtension.isValid("namespace"));
    }

    @Test
    public void testIsDependency()
    {
        Assert.assertFalse(this.installedExtension.isDependency());
        Assert.assertFalse(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setInstalled(true);

        Assert.assertFalse(this.installedExtension.isDependency());
        Assert.assertFalse(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setDependency(true, null);

        Assert.assertTrue(this.installedExtension.isDependency());
        Assert.assertTrue(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setInstalled(true, "namespace");

        Assert.assertFalse(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setDependency(true, "namespace");

        Assert.assertTrue(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setDependency(false, "namespace");

        Assert.assertFalse(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setDependency(false, null);
        this.installedExtension.setDependency(true, "namespace");

        Assert.assertTrue(this.installedExtension.isDependency("namespace"));
    }

    @Test
    public void testGetNamespaces()
    {
        Assert.assertNull(this.installedExtension.getNamespaces());

        this.installedExtension.setInstalled(true, "namespace1");

        Assert
            .assertEquals(Arrays.asList("namespace1"), new ArrayList<String>(this.installedExtension.getNamespaces()));

        this.installedExtension.setInstalled(true, "namespace2");

        Assert.assertEquals(new HashSet<String>(Arrays.asList("namespace1", "namespace2")), new HashSet<String>(
            this.installedExtension.getNamespaces()));

        this.installedExtension.setNamespaces(Arrays.asList("namespace3"));

        Assert
            .assertEquals(Arrays.asList("namespace3"), new ArrayList<String>(this.installedExtension.getNamespaces()));

    }

    @Test
    public void testSetInstallDate()
    {
        Date date = new Date(13);

        this.installedExtension.setInstallDate(date, "foo");
        Assert.assertNull(this.installedExtension.getInstallDate("foo"));

        this.installedExtension.setInstallDate(date, null);
        Assert.assertNull(this.installedExtension.getInstallDate(null));

        this.installedExtension.setInstalled(true, "foo");
        this.installedExtension.setInstallDate(date, "foo");
        Assert.assertEquals(date, this.installedExtension.getInstallDate("foo"));
        Assert.assertNull(this.installedExtension.getInstallDate("bar"));
        Assert.assertNull(this.installedExtension.getInstallDate(null));

        this.installedExtension.setInstalled(false, "foo");
        Assert.assertNull(this.installedExtension.getInstallDate("foo"));

        this.installedExtension.setInstalled(true, null);
        this.installedExtension.setInstallDate(date, null);
        Assert.assertEquals(date, this.installedExtension.getInstallDate(null));
        Assert.assertEquals(date, this.installedExtension.getInstallDate("foo"));

        this.installedExtension.setInstalled(false, null);
        Assert.assertNull(this.installedExtension.getInstallDate(null));
        Assert.assertNull(this.installedExtension.getInstallDate("foo"));

        this.installedExtension.setInstalled(true, "foo");
        this.installedExtension.setInstallDate(new Date(27), "foo");
        this.installedExtension.setInstalled(true, null);
        this.installedExtension.setInstallDate(date, null);
        Assert.assertEquals(date, this.installedExtension.getInstallDate(null));
        Assert.assertEquals(date, this.installedExtension.getInstallDate("foo"));
    }
}
