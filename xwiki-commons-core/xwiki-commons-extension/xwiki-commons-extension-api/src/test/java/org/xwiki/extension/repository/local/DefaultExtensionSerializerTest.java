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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.DefaultExtensionIssueManagement;
import org.xwiki.extension.DefaultExtensionScm;
import org.xwiki.extension.DefaultExtensionScmConnection;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionLicense;
import org.xwiki.extension.InvalidExtensionException;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.repository.internal.DefaultExtensionSerializer;
import org.xwiki.extension.repository.internal.ExtensionSerializer;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

@ComponentList(ExtensionFactory.class)
public class DefaultExtensionSerializerTest
{
    @Rule
    public final MockitoComponentMockingRule<ExtensionSerializer> componentManager =
        new MockitoComponentMockingRule<ExtensionSerializer>(DefaultExtensionSerializer.class);

    private DefaultLocalExtension serializeAndUnserialize(DefaultLocalExtension extension)
        throws ParserConfigurationException, TransformerException, InvalidExtensionException, ComponentLookupException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        this.componentManager.getComponentUnderTest().saveExtensionDescriptor(extension, os);

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        DefaultLocalExtension unserializedExtension =
            this.componentManager.getComponentUnderTest().loadLocalExtensionDescriptor(null, is);

        Assert.assertEquals(extension, unserializedExtension);
        Assert.assertEquals(extension.getDescription(), unserializedExtension.getDescription());
        Assert.assertEquals(extension.getName(), unserializedExtension.getName());
        Assert.assertEquals(extension.getCategory(), unserializedExtension.getCategory());
        Assert.assertEquals(extension.getSummary(), unserializedExtension.getSummary());
        Assert.assertEquals(extension.getWebSite(), unserializedExtension.getWebSite());
        Assert.assertEquals(extension.getAuthors(), unserializedExtension.getAuthors());
        Assert.assertEquals(new ArrayList<ExtensionId>(extension.getExtensionFeatures()),
            new ArrayList<ExtensionId>(unserializedExtension.getExtensionFeatures()));
        Assert.assertEquals(new ArrayList<String>(extension.getFeatures()),
            new ArrayList<String>(unserializedExtension.getFeatures()));
        Assert.assertEquals(new ArrayList<String>(extension.getAllowedNamespaces()),
            new ArrayList<String>(unserializedExtension.getAllowedNamespaces()));
        Assert.assertEquals(new ArrayList<ExtensionLicense>(extension.getLicenses()),
            new ArrayList<ExtensionLicense>(unserializedExtension.getLicenses()));
        Assert.assertEquals(extension.getScm(), unserializedExtension.getScm());
        Assert.assertEquals(extension.getIssueManagement(), unserializedExtension.getIssueManagement());
        Assert.assertEquals(extension.getProperties(), unserializedExtension.getProperties());

        for (int i = 0; i < extension.getDependencies().size(); ++i) {
            Assert.assertEquals(extension.getDependencies().get(i), unserializedExtension.getDependencies().get(i));
            Assert.assertEquals(extension.getDependencies().get(i).getProperties(),
                unserializedExtension.getDependencies().get(i).getProperties());
        }

        for (int i = 0; i < extension.getManagedDependencies().size(); ++i) {
            Assert.assertEquals(extension.getManagedDependencies().get(i),
                unserializedExtension.getManagedDependencies().get(i));
            Assert.assertEquals(extension.getManagedDependencies().get(i).getProperties(),
                unserializedExtension.getManagedDependencies().get(i).getProperties());
        }

        return unserializedExtension;
    }

    // Tests

    @Test
    public void testSerialize() throws ParserConfigurationException, TransformerException, InvalidExtensionException,
        MalformedURLException, ComponentLookupException
    {
        DefaultLocalExtension extension =
            new DefaultLocalExtension(null, new ExtensionId("extensionid", "extensionversion"), "type");

        // Minimum extension
        // serializeAndUnserialize(extension);

        DefaultExtensionDependency dependency =
            new DefaultExtensionDependency("dependencyid", new DefaultVersionConstraint("dependencyversion"));
        extension.addDependency(dependency);

        // Minimum extension with minimum dependency
        // serializeAndUnserialize(extension);

        extension.setDescription("description");
        extension.setSummary("summary");
        extension.setWebsite("website");
        extension.setName("name");

        extension.setCategory("category");

        extension.putProperty("key1", "value1");
        extension.putProperty("key2", true);
        extension.putProperty("key3", 42);
        extension.putProperty("key4", Arrays.asList("list1", "list2"));
        extension.putProperty("key5", new HashSet<String>(Arrays.asList("list1", "list2")));
        extension.putProperty("key6", Collections.<String, Object>singletonMap("key", "value"));
        extension.putProperty("key7", Collections.<String, Object>singletonMap("key",
            Collections.<String, Object>singletonMap("subkey", "subvalue")));

        extension.addAuthor(new DefaultExtensionAuthor("authorname", "http://authorurl"));
        extension.addFeature("feature1");
        extension.addExtensionFeature(new ExtensionId("feature2", "version"));
        extension.addAllowedNamespace("namespae1");
        extension.addLicense(new ExtensionLicense("licensename", Arrays.asList("license content")));
        dependency.setProperties(Collections.<String, Object>singletonMap("dependencykey", "dependencyvalue"));

        extension.setScm(new DefaultExtensionScm("url", new DefaultExtensionScmConnection("system", "path"),
            new DefaultExtensionScmConnection("system2", "path2")));
        extension.setIssueManagement(new DefaultExtensionIssueManagement("system", "url"));

        // Complete extension
        serializeAndUnserialize(extension);
    }
}
