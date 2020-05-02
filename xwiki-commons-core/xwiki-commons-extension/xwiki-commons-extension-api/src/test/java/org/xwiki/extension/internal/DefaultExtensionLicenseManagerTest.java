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
package org.xwiki.extension.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.ExtensionLicense;
import org.xwiki.extension.ExtensionLicenseManager;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultExtensionLicenseManager}.
 *
 * @version $Id$
 */
@ComponentTest
// @formatter:off
@ComponentList({
    DefaultExtensionLicenseManager.class
})
// @formatter:on
public class DefaultExtensionLicenseManagerTest
{
    @InjectComponentManager
    private ComponentManager componentManager;

    private ExtensionLicenseManager licenseManager;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.licenseManager = this.componentManager.getInstance(ExtensionLicenseManager.class);
    }

    @Test
    void getLicenses()
    {
        assertTrue(this.licenseManager.getLicenses().size() > 0);
    }

    @Test
    void getLicense() throws IOException
    {
        ExtensionLicense license = this.licenseManager.getLicense("Apache License 2.0");

        List<String> content = IOUtils.readLines(
            getClass().getResourceAsStream("/extension/licenses/Apache License 2.0.license"), StandardCharsets.UTF_8);
        content = content.subList(8, content.size());

        assertNotNull(license);
        assertEquals("Apache License 2.0", license.getName());
        assertEquals(content, license.getContent());

        license = this.licenseManager.getLicense("ASL");

        assertNotNull(license);
        assertEquals("Apache License 2.0", license.getName());
        assertEquals(content, license.getContent());
    }
}
