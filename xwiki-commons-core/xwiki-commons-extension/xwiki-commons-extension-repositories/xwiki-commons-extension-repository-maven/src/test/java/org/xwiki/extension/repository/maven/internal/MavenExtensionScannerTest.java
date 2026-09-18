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
package org.xwiki.extension.repository.maven.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtension;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtensionRepository;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Validate {@link MavenExtensionScanner}.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class MavenExtensionScannerTest
{
    @InjectMockComponents
    private MavenExtensionScanner scanner;

    @Test
    void scanJARsWithRawPOM()
    {
        // A pom.xml which was not interpolated, so its artifact id is still a Maven property
        URL folder = getClass().getClassLoader().getResource("rawpom/");
        assertNotNull(folder);

        Map<String, DefaultCoreExtension> extensions = new HashMap<>();
        this.scanner.scanJARs(extensions, new ArrayList<>(List.of(folder)), mock(DefaultCoreExtensionRepository.class));

        // The identifier is taken from the location of the descriptor
        assertEquals(1, extensions.size());
        DefaultCoreExtension extension = extensions.get("rawgroupid:rawartifactid");
        assertNotNull(extension, "Found " + extensions.keySet());
        assertEquals("1.0", extension.getId().getVersion().getValue());
    }
}
