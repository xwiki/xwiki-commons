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
package org.xwiki.extension.repository.local;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.AbstractExtensionTest;
import org.xwiki.extension.DefaultExtensionComponent;
import org.xwiki.extension.DefaultExtensionIssueManagement;
import org.xwiki.extension.DefaultExtensionPattern;
import org.xwiki.extension.DefaultExtensionScm;
import org.xwiki.extension.DefaultExtensionScmConnection;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InvalidExtensionException;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.repository.internal.DefaultExtensionSerializer;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ComponentTest
@ComponentList(ExtensionFactory.class)
class DefaultExtensionSerializerTest
{
    @InjectMockComponents
    private DefaultExtensionSerializer serializer;

    private DefaultLocalExtension serializeAndUnserialize(DefaultLocalExtension extension)
        throws ParserConfigurationException, TransformerException, InvalidExtensionException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        this.serializer.saveExtensionDescriptor(extension, os);

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        DefaultLocalExtension unserializedExtension = this.serializer.loadLocalExtensionDescriptor(null, is);

        assertEquals(extension, unserializedExtension);
        assertEquals(extension.getDescription(), unserializedExtension.getDescription());
        assertEquals(extension.getName(), unserializedExtension.getName());
        assertEquals(extension.getCategory(), unserializedExtension.getCategory());
        assertEquals(extension.getSummary(), unserializedExtension.getSummary());
        assertEquals(extension.getWebSite(), unserializedExtension.getWebSite());
        assertEquals(extension.getAuthors(), unserializedExtension.getAuthors());
        assertEquals(new ArrayList<>(extension.getExtensionFeatures()),
            new ArrayList<>(unserializedExtension.getExtensionFeatures()));
        assertEquals(new ArrayList<>(extension.getFeatures()), new ArrayList<>(unserializedExtension.getFeatures()));
        if (extension.getAllowedNamespaces() != null) {
            assertEquals(new ArrayList<>(extension.getAllowedNamespaces()),
                new ArrayList<>(unserializedExtension.getAllowedNamespaces()));
        } else {
            assertNull(unserializedExtension.getAllowedNamespaces());
        }
        assertEquals(new ArrayList<>(extension.getLicenses()), new ArrayList<>(unserializedExtension.getLicenses()));
        assertEquals(extension.getComponents(), unserializedExtension.getComponents());
        assertEquals(extension.getScm(), unserializedExtension.getScm());
        assertEquals(extension.getIssueManagement(), unserializedExtension.getIssueManagement());
        assertEquals(extension.getProperties(), unserializedExtension.getProperties());

        assertEquals(extension.getDependencies().size(), unserializedExtension.getDependencies().size());
        for (int i = 0; i < extension.getDependencies().size(); ++i) {
            assertEquals(extension.getDependencies().get(i), unserializedExtension.getDependencies().get(i));
            assertEquals(extension.getDependencies().get(i).getProperties(),
                unserializedExtension.getDependencies().get(i).getProperties());
        }

        assertEquals(extension.getManagedDependencies().size(), unserializedExtension.getManagedDependencies().size());
        for (int i = 0; i < extension.getManagedDependencies().size(); ++i) {
            assertEquals(extension.getManagedDependencies().get(i),
                unserializedExtension.getManagedDependencies().get(i));
            assertEquals(extension.getManagedDependencies().get(i).getProperties(),
                unserializedExtension.getManagedDependencies().get(i).getProperties());
        }

        return unserializedExtension;
    }

    @Test
    void testSerializeAndUnserialize()
        throws ParserConfigurationException, TransformerException, InvalidExtensionException, ComponentLookupException
    {
        DefaultLocalExtension extension =
            new DefaultLocalExtension(null, new ExtensionId("extensionid", "extensionversion"), "type");

        // Minimum extension
        serializeAndUnserialize(extension);

        extension.addDependency(AbstractExtensionTest.DEPENDENCY1);

        // Minimum extension with minimum dependency
        serializeAndUnserialize(extension);

        AbstractExtensionTest.DEPENDENCY2.addExclusion(new DefaultExtensionPattern("pattern"));

        extension.addDependency(AbstractExtensionTest.DEPENDENCY2);

        AbstractExtensionTest.DEPENDENCY1
            .setProperties(Collections.<String, Object>singletonMap("dependencykey", "dependencyvalue"));

        extension.addManagedDependency(AbstractExtensionTest.DEPENDENCY1);
        extension.addManagedDependency(AbstractExtensionTest.DEPENDENCY2);

        extension.setDescription("description");
        extension.setSummary("summary");
        extension.setWebsite("website");
        extension.setName("name");

        extension.setCategory("category");

        extension.putProperty("key1", "value1");
        extension.putProperty("key2", true);
        extension.putProperty("key3", false);
        extension.putProperty("key4", 42);
        extension.putProperty("key5", Arrays.asList("list1", "list2"));
        extension.putProperty("key6", new HashSet<>(Arrays.asList("list1", "list2")));
        extension.putProperty("key7", Collections.singletonMap("key", "value"));
        extension.putProperty("key8", Collections.singletonMap("key", Collections.singletonMap("subkey", "subvalue")));
        extension.putProperty("key9", new Date(0));

        extension.addAuthor(AbstractExtensionTest.AUTHOR1);
        extension.addAuthor(AbstractExtensionTest.AUTHOR2);
        extension.addFeature("feature1");
        extension.addExtensionFeature(new ExtensionId("feature2", "version"));
        extension.addAllowedNamespace("namespae1");
        extension.addLicense(AbstractExtensionTest.LICENSE1);
        extension.addLicense(AbstractExtensionTest.LICENSE2);

        extension.addComponent(new DefaultExtensionComponent("roletype1", "rolehint1"));
        extension.addComponent(new DefaultExtensionComponent("roletype2", "rolehint2"));

        extension.setScm(new DefaultExtensionScm("url", new DefaultExtensionScmConnection("system", "path"),
            new DefaultExtensionScmConnection("system2", "path2")));
        extension.setIssueManagement(new DefaultExtensionIssueManagement("system", "url"));

        // Complete extension
        serializeAndUnserialize(extension);
    }
}
