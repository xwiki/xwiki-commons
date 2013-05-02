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
        installedExtension = new DefaultInstalledExtension(localExtension, null);
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
}
