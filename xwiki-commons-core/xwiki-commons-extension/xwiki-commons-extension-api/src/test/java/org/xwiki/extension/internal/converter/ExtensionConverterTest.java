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
package org.xwiki.extension.internal.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.internal.maven.MavenUtils;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Validate {@link ExtensionConverter} component.
 *
 * @version $Id$
 */
@AllComponents
public class ExtensionConverterTest
{
    @Rule
    public MockitoComponentMockingRule<ConverterManager> mocker =
        new MockitoComponentMockingRule<ConverterManager>(DefaultConverterManager.class);

    @Test
    public void testConvertFromExtension() throws SecurityException, ComponentLookupException, URISyntaxException
    {
        Model model = new Model();

        model.setGroupId("groupid");
        model.setArtifactId("artifactid");
        model.setVersion("version");
        model.addProperty(MavenUtils.MPKEYPREFIX + MavenUtils.MPNAME_CATEGORY, "category");
        model.addProperty(MavenUtils.MPKEYPREFIX + MavenUtils.MPNAME_NAMESPACES, "namespace1, namespace2");
        Repository repository = new Repository();
        repository.setId("repository-id");
        repository.setUrl("http://url");
        model.addRepository(repository);

        Extension extension = this.mocker.getComponentUnderTest().convert(Extension.class, model);

        assertEquals(model.getGroupId() + ':' + model.getArtifactId(), extension.getId().getId());
        assertEquals(model.getVersion(), extension.getId().getVersion().getValue());
        assertEquals("category", extension.getCategory());
        assertNull(extension.getProperty(MavenUtils.MPKEYPREFIX + MavenUtils.MPNAME_CATEGORY));
        assertEquals(Arrays.asList("namespace1", "namespace2"), new ArrayList<>(extension.getAllowedNamespaces()));
        assertNull(extension.getProperty(MavenUtils.MPKEYPREFIX + MavenUtils.MPNAME_NAMESPACES));
        assertEquals(
            Arrays.asList(new DefaultExtensionRepositoryDescriptor("repository-id", "maven", new URI("http://url"))),
            extension.getRepositories());
    }
}
