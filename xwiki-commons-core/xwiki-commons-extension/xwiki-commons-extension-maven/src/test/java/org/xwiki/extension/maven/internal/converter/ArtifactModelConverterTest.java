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

import org.apache.maven.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.extension.Extension;
import org.xwiki.extension.maven.ArtifactModel;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate {@link ModelConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class ArtifactModelConverterTest
{
    @InjectMockComponents
    private DefaultConverterManager converter;

    private ArtifactModel artifactModel;

    @BeforeEach
    void beforeEach()
    {
        Model model = new Model();

        model.setGroupId("groupid");
        model.setArtifactId("artifactid");
        model.setVersion("version");
        model.setPackaging("jar");

        this.artifactModel = new ArtifactModel(model);
    }

    @Test
    void testConvertWithClassifier() throws SecurityException
    {
        this.artifactModel.setClassifier("classifier");

        Extension extension = this.converter.convert(Extension.class, artifactModel);

        assertEquals("groupid:artifactid:classifier", extension.getId().getId());
        assertEquals("jar", extension.getType());
    }

    @Test
    void testConvertPom() throws SecurityException
    {
        this.artifactModel.setType("pom");

        Extension extension = this.converter.convert(Extension.class, artifactModel);

        assertNull(extension.getType());
    }

    @Test
    void testConvertWithType() throws SecurityException
    {
        this.artifactModel.setType("type");

        Extension extension = this.converter.convert(Extension.class, artifactModel);

        assertEquals("groupid:artifactid", extension.getId().getId());
        assertEquals("type", extension.getType());
    }

    @Test
    void testConvertWithClassifierAndType() throws SecurityException
    {
        this.artifactModel.setClassifier("classifier");
        this.artifactModel.setType("type");

        Extension extension = this.converter.convert(Extension.class, artifactModel);

        assertEquals("groupid:artifactid:classifier", extension.getId().getId());
        assertEquals("type", extension.getType());
    }
}
