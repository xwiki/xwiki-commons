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
package org.xwiki.extension.maven.internal.converter;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.maven.internal.MavenUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validate {@link MavenUtils}.
 * 
 * @version $Id$
 */
public class MavenUtilsTest
{
    @Test
    void toXWikiExtensionIdentifier()
    {
        assertEquals("groupId:artifactId:classifier:mavenType",
            MavenUtils.toXWikiExtensionIdentifier("groupId", "artifactId", "classifier", "mavenType"));
        assertEquals("groupId:artifactId:classifier", MavenUtils.toExtensionId("groupId", "artifactId", "classifier"));
        assertEquals("groupId:artifactId", MavenUtils.toXWikiExtensionIdentifier("groupId", "artifactId", null, null));
    }

    @Test
    void toExtensionId()
    {
        assertEquals(new ExtensionId("groupId:artifactId:classifier", "version"),
            MavenUtils.toExtensionId("groupId", "artifactId", "classifier", "version"));
    }
}
