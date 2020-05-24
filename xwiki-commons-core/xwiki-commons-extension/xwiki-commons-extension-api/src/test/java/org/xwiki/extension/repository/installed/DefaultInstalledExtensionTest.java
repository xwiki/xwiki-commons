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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.repository.internal.installed.DefaultInstalledExtension;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link DefaultInstalledExtension}.
 *
 * @version $Id$
 */
public class DefaultInstalledExtensionTest
{
    private DefaultInstalledExtension installedExtension;

    @BeforeEach
    public void beforeEach()
    {
        DefaultLocalExtension localExtension =
            new DefaultLocalExtension(null, new ExtensionId("installed", "version"), "type");
        this.installedExtension = new DefaultInstalledExtension(localExtension, null);
    }

    @Test
    void isInstalled()
    {
        assertFalse(this.installedExtension.isInstalled());
        assertFalse(this.installedExtension.isInstalled("namespace"));

        this.installedExtension.setInstalled(true);

        assertTrue(this.installedExtension.isInstalled());
        assertTrue(this.installedExtension.isInstalled("namespace"));

        this.installedExtension.setInstalled(true, "namespace");

        assertTrue(this.installedExtension.isInstalled());
        assertTrue(this.installedExtension.isInstalled("namespace"));

        this.installedExtension.setInstalled(false);

        assertFalse(this.installedExtension.isInstalled());
        assertFalse(this.installedExtension.isInstalled("namespace"));
    }

    @Test
    void isValid()
    {
        assertTrue(this.installedExtension.isValid(null));
        assertTrue(this.installedExtension.isValid("namespace"));

        this.installedExtension.setValid(null, false);

        assertFalse(this.installedExtension.isValid(null));
        assertTrue(this.installedExtension.isValid("namespace"));

        this.installedExtension.setValid("namespace", false);

        assertFalse(this.installedExtension.isValid(null));
        assertFalse(this.installedExtension.isValid("namespace"));

        this.installedExtension.setInstalled(true, "namespace");

        assertFalse(this.installedExtension.isValid(null));
        assertTrue(this.installedExtension.isValid("namespace"));

        this.installedExtension.setValid("namespace", false);

        assertFalse(this.installedExtension.isValid(null));
        assertFalse(this.installedExtension.isValid("namespace"));

        this.installedExtension.setInstalled(false, "namespace");

        assertFalse(this.installedExtension.isValid(null));
        assertTrue(this.installedExtension.isValid("namespace"));
    }

    @Test
    void isDependency()
    {
        assertFalse(this.installedExtension.isDependency());
        assertFalse(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setInstalled(true);

        assertFalse(this.installedExtension.isDependency());
        assertFalse(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setDependency(true, null);

        assertTrue(this.installedExtension.isDependency());
        assertTrue(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setInstalled(true, "namespace");

        assertFalse(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setDependency(true, "namespace");

        assertTrue(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setDependency(false, "namespace");

        assertFalse(this.installedExtension.isDependency("namespace"));

        this.installedExtension.setDependency(false, null);
        this.installedExtension.setDependency(true, "namespace");

        assertTrue(this.installedExtension.isDependency("namespace"));
    }

    @Test
    void getNamespaces()
    {
        assertNull(this.installedExtension.getNamespaces());

        this.installedExtension.setInstalled(true, "namespace1");

        assertEquals(Arrays.asList("namespace1"), new ArrayList<>(this.installedExtension.getNamespaces()));

        this.installedExtension.setInstalled(true, "namespace2");

        assertEquals(new HashSet<>(Arrays.asList("namespace1", "namespace2")),
            new HashSet<>(this.installedExtension.getNamespaces()));

        this.installedExtension.setNamespaces(Arrays.asList("namespace3"));

        assertEquals(Arrays.asList("namespace3"), new ArrayList<>(this.installedExtension.getNamespaces()));
    }

    @Test
    void setInstallDate()
    {
        Date date = new Date(13);

        this.installedExtension.setInstallDate(date, "foo");
        assertNull(this.installedExtension.getInstallDate("foo"));

        this.installedExtension.setInstallDate(date, null);
        assertNull(this.installedExtension.getInstallDate(null));

        this.installedExtension.setInstalled(true, "foo");
        this.installedExtension.setInstallDate(date, "foo");
        assertEquals(date, this.installedExtension.getInstallDate("foo"));
        assertNull(this.installedExtension.getInstallDate("bar"));
        assertNull(this.installedExtension.getInstallDate(null));

        this.installedExtension.setInstalled(false, "foo");
        assertNull(this.installedExtension.getInstallDate("foo"));

        this.installedExtension.setInstalled(true, null);
        this.installedExtension.setInstallDate(date, null);
        assertEquals(date, this.installedExtension.getInstallDate(null));
        assertEquals(date, this.installedExtension.getInstallDate("foo"));

        this.installedExtension.setInstalled(false, null);
        assertNull(this.installedExtension.getInstallDate(null));
        assertNull(this.installedExtension.getInstallDate("foo"));

        this.installedExtension.setInstalled(true, "foo");
        this.installedExtension.setInstallDate(new Date(27), "foo");
        this.installedExtension.setInstalled(true, null);
        this.installedExtension.setInstallDate(date, null);
        assertEquals(date, this.installedExtension.getInstallDate(null));
        assertEquals(date, this.installedExtension.getInstallDate("foo"));
    }

    @Test
    void isValidated()
    {
        assertFalse(this.installedExtension.isValidated("namespace1"));
        assertFalse(this.installedExtension.isValidated("namespace2"));

        this.installedExtension.setValid("namespace1", true);

        assertTrue(this.installedExtension.isValidated("namespace1"));
        assertFalse(this.installedExtension.isValidated("namespace2"));

        this.installedExtension.setValid("namespace2", false);

        assertTrue(this.installedExtension.isValidated("namespace1"));
        assertTrue(this.installedExtension.isValidated("namespace2"));
    }
}
