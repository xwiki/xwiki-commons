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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.junit.jupiter.api.Test;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.maven.internal.MavenUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link MavenUtils}.
 * 
 * @version $Id$
 */
class MavenUtilsTest
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

    @Test
    void resolveVersion()
    {
        Model model = new Model();
        model.setVersion("1.0");
        assertEquals("1.0", MavenUtils.resolveVersion(model));

        // The version is inherited from the parent
        model = new Model();
        Parent parent = new Parent();
        parent.setVersion("2.0");
        model.setParent(parent);
        assertEquals("2.0", MavenUtils.resolveVersion(model));

        // The version comes from a property
        model = new Model();
        model.setVersion("${myversion}");
        model.getProperties().setProperty("myversion", "3.0");
        assertEquals("3.0", MavenUtils.resolveVersion(model));

        // The version of the model references itself: it can only come from the parent
        model = new Model();
        model.setVersion("${project.version}");
        parent = new Parent();
        parent.setVersion("4.0");
        model.setParent(parent);
        assertEquals("4.0", MavenUtils.resolveVersion(model));

        // The version of the model references itself and there is no parent
        model = new Model();
        model.setVersion("${project.version}");
        assertEquals(MavenUtils.UNKNOWN, MavenUtils.resolveVersion(model));

        // A dependency referencing the version of the project
        model = new Model();
        model.setVersion("5.0");
        assertEquals("5.0", MavenUtils.resolveVersion("${project.version}", model, true));

        // Not a property reference
        model = new Model();
        model.setVersion("$");
        assertEquals("$", MavenUtils.resolveVersion(model));
    }

    @Test
    void resolveGroupId()
    {
        Model model = new Model();
        model.setGroupId("groupid");
        assertEquals("groupid", MavenUtils.resolveGroupId(model));

        // The group id is inherited from the parent
        model = new Model();
        Parent parent = new Parent();
        parent.setGroupId("parentgroupid");
        model.setParent(parent);
        assertEquals("parentgroupid", MavenUtils.resolveGroupId(model));

        // The group id comes from a property
        model = new Model();
        model.setGroupId("${mygroupid}");
        model.getProperties().setProperty("mygroupid", "propertygroupid");
        assertEquals("propertygroupid", MavenUtils.resolveGroupId(model));

        // Not a property reference
        model = new Model();
        model.setGroupId("$");
        assertEquals("$", MavenUtils.resolveGroupId(model));
    }

    @Test
    void isUnresolved()
    {
        assertFalse(MavenUtils.isUnresolved("artifactId"));
        assertTrue(MavenUtils.isUnresolved("${project.artifactId}"));
        assertTrue(MavenUtils.isUnresolved(null));
    }

    @Test
    void parseDescriptorPath()
    {
        assertArrayEquals(new String[] {"groupId", "artifactId"},
            MavenUtils.parseDescriptorPath("META-INF/maven/groupId/artifactId/pom.xml"));
        assertArrayEquals(new String[] {"io.netty", "netty-tcnative-boringssl-static"}, MavenUtils.parseDescriptorPath(
            "jar:file:/lib/netty-tcnative-boringssl-static-2.0.81.Final-linux-x86_64.jar"
                + "!/META-INF/maven/io.netty/netty-tcnative-boringssl-static/pom.xml"));

        assertArrayEquals(ArrayUtils.EMPTY_STRING_ARRAY, MavenUtils.parseDescriptorPath(null));
        assertArrayEquals(ArrayUtils.EMPTY_STRING_ARRAY,
            MavenUtils.parseDescriptorPath("META-INF/maven/groupId/artifactId/pom.properties"));
        assertArrayEquals(ArrayUtils.EMPTY_STRING_ARRAY,
            MavenUtils.parseDescriptorPath("META-INF/maven/artifactId/pom.xml"));
        assertArrayEquals(ArrayUtils.EMPTY_STRING_ARRAY, MavenUtils.parseDescriptorPath("pom.xml"));
    }
}
