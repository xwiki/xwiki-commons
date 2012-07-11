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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import junit.framework.Assert;

import org.junit.Test;
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionLicense;
import org.xwiki.extension.InvalidExtensionException;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;
import org.xwiki.extension.repository.internal.local.ExtensionSerializer;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;
import org.xwiki.test.AbstractComponentTestCase;

public class DefaultExtensionSerializerTest extends AbstractComponentTestCase
{
    private ExtensionSerializer serializer;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.serializer = getComponentManager().getInstance(ExtensionSerializer.class);
    }

    private DefaultLocalExtension serializeAndUnserialize(DefaultLocalExtension extension)
        throws ParserConfigurationException, TransformerException, InvalidExtensionException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        this.serializer.saveDescriptor(extension, os);

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        DefaultLocalExtension unserializedExtension = this.serializer.loadDescriptor(null, is);

        Assert.assertEquals(extension, unserializedExtension);
        Assert.assertEquals(extension.getDescription(), unserializedExtension.getDescription());
        Assert.assertEquals(extension.getName(), unserializedExtension.getName());
        Assert.assertEquals(extension.getSummary(), unserializedExtension.getSummary());
        Assert.assertEquals(extension.getWebSite(), unserializedExtension.getWebSite());
        Assert.assertEquals(extension.getAuthors(), unserializedExtension.getAuthors());
        Assert.assertEquals(extension.getFeatures(), unserializedExtension.getFeatures());
        Assert.assertEquals(extension.getLicenses(), unserializedExtension.getLicenses());
        Assert.assertEquals(extension.getProperties(), unserializedExtension.getProperties());

        for (int i = 0; i < extension.getDependencies().size(); ++i) {
            Assert.assertEquals(extension.getDependencies().get(i), unserializedExtension.getDependencies().get(i));
            Assert.assertEquals(extension.getDependencies().get(i).getProperties(), unserializedExtension
                .getDependencies().get(i).getProperties());
        }

        return unserializedExtension;
    }

    // Tests

    @Test
    public void testSerialize() throws ParserConfigurationException, TransformerException, InvalidExtensionException,
        MalformedURLException
    {
        DefaultLocalExtension extension =
            new DefaultLocalExtension(null, new ExtensionId("extensionid", "extensionversion"), "type");

        // Minimum extension
        //serializeAndUnserialize(extension);

        DefaultExtensionDependency dependency =
            new DefaultExtensionDependency("dependencyid", new DefaultVersionConstraint("dependencyversion"));
        extension.addDependency(dependency);

        // Minimum extension with minimum dependency
        //serializeAndUnserialize(extension);

        extension.setDescription("description");
        extension.setSummary("summary");
        extension.setWebsite("website");
        extension.setName("name");

        extension.putProperty("key1", "value1");
        extension.putProperty("key2", true);
        extension.putProperty("key3", 42);
        extension.putProperty("key4", Arrays.asList("list1", "list2"));
        extension.putProperty("key5", new HashSet<String>(Arrays.asList("list1", "list2")));

        extension.addAuthor(new DefaultExtensionAuthor("authorname", new URL("http://authorurl")));
        extension.addFeature("feature1");
        extension.addLicense(new ExtensionLicense("licensename", Arrays.asList("license content")));
        dependency.setProperties(Collections.<String, Object> singletonMap("dependencykey", "dependencyvalue"));

        // Complete extension
        serializeAndUnserialize(extension);
    }
}
