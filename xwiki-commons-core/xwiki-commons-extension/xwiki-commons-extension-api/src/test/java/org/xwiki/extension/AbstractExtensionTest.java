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
package org.xwiki.extension;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * @version $Id$
 */
public class AbstractExtensionTest
{
    public static final ExtensionRepositoryDescriptor DESCRIPTOR1 = new DefaultExtensionRepositoryDescriptor("id1");

    public static final ExtensionRepositoryDescriptor DESCRIPTOR2 = new DefaultExtensionRepositoryDescriptor("id2");

    public static final DefaultExtensionAuthor AUTHOR1 = new DefaultExtensionAuthor("name1", "http://url1");

    public static final DefaultExtensionAuthor AUTHOR2 = new DefaultExtensionAuthor("name1", "http://url2");

    public static final ExtensionLicense LICENSE1 = new ExtensionLicense("license1", Arrays.asList("content1"));

    public static final ExtensionLicense LICENSE2 = new ExtensionLicense("license2", Arrays.asList("content2"));

    public static final DefaultExtensionDependency DEPENDENCY1 =
        new DefaultExtensionDependency("id1", new DefaultVersionConstraint("version1"));

    public static final DefaultExtensionDependency DEPENDENCY2 =
        new DefaultExtensionDependency("id2", new DefaultVersionConstraint("version2"));

    private AbstractExtension extension;

    private ExtensionRepository repository;

    private ExtensionId id;

    private String type;

    public static class TestExtension extends AbstractExtension
    {
        public TestExtension(ExtensionId id, String type, ExtensionId... features)
        {
            super(null, id, type);

            for (ExtensionId feature : features) {
                addExtensionFeature(feature);
            }
        }

        public TestExtension(Extension extension)
        {
            super(null, extension);
        }
    }

    private AbstractExtension toExtension(String id, String version, ExtensionId... features)
    {
        return new TestExtension(new ExtensionId(id, version), "test", features);
    }

    private void assertCompareTo(int comparizon, Extension e1, Extension e2)
    {
        assertEquals(comparizon, e1.compareTo(e2));
    }

    @BeforeEach
    public void beforeEach()
    {
        this.repository = mock(ExtensionRepository.class);
        this.id = new ExtensionId("extesionid", "extensionversion");
        this.type = "extensiontype";

        this.extension = new AbstractExtension(this.repository, this.id, this.type)
        {
        };
    }

    @Test
    void get()
    {
        assertSame(this.repository, this.extension.get("repository"));
        assertEquals(this.id.getId(), this.extension.get("id"));
        assertEquals(this.id.getVersion(), this.extension.get("version"));
        assertEquals(this.type, this.extension.get("type"));
    }

    @Test
    void compareTo()
    {
        assertCompareTo(0, toExtension("id", "2.0"), toExtension("id", "2.0"));
        assertCompareTo(-1, toExtension("id", "2.0"), toExtension("id", "3.0"));
        assertCompareTo(1, toExtension("id", "2.0"), toExtension("id", "1.0"));

        assertCompareTo(0, toExtension("feature", "2.0"), toExtension("id", "2.0", new ExtensionId("feature", "2.0")));
        assertCompareTo(-1, toExtension("feature", "2.0"), toExtension("id", "2.0", new ExtensionId("feature", "3.0")));
        assertCompareTo(1, toExtension("feature", "2.0"), toExtension("id", "2.0", new ExtensionId("feature", "1.0")));

        assertCompareTo(0, toExtension("id", "2.0", new ExtensionId("feature", "2.0")), toExtension("feature", "2.0"));
        assertCompareTo(-1, toExtension("id", "2.0", new ExtensionId("feature", "2.0")), toExtension("feature", "3.0"));
        assertCompareTo(1, toExtension("id", "2.0", new ExtensionId("feature", "2.0")), toExtension("feature", "1.0"));

        assertCompareTo(-1, toExtension("id", "1.0"), toExtension("id2", "1.0"));

        assertCompareTo(-1, toExtension("id", "1.0"), null);
    }

    @Test
    void set()
    {
        AbstractExtension extension = toExtension("id", "version", new ExtensionId("feature", "featureversion"));

        extension.addAuthor(AUTHOR1);
        extension.addAuthor(AUTHOR2);

        extension.addRepository(DESCRIPTOR1);
        extension.addRepository(DESCRIPTOR2);

        extension.addLicense(LICENSE1);
        extension.addLicense(LICENSE2);

        extension.addDependency(DEPENDENCY1);
        extension.addDependency(DEPENDENCY2);

        extension.addManagedDependency(DEPENDENCY1);
        extension.addManagedDependency(DEPENDENCY2);

        extension.addAllowedNamespace("namespace1");
        extension.addAllowedNamespace("namespace2");

        AbstractExtension cloneExtension = new TestExtension(extension);

        assertEquals(extension, cloneExtension);
        assertEquals(extension.getId(), cloneExtension.getId());
        assertEquals(new ArrayList<>(extension.getExtensionFeatures()),
            new ArrayList<>(cloneExtension.getExtensionFeatures()));
        assertEquals(Arrays.asList(DESCRIPTOR1, DESCRIPTOR2), cloneExtension.getRepositories());
        assertEquals(Arrays.asList(AUTHOR1, AUTHOR2), cloneExtension.getAuthors());
        assertEquals(Arrays.asList(LICENSE1, LICENSE2), cloneExtension.getLicenses());
        assertEquals(Arrays.asList(DEPENDENCY1, DEPENDENCY2), cloneExtension.getDependencies());
        assertEquals(Arrays.asList(DEPENDENCY1, DEPENDENCY2), cloneExtension.getManagedDependencies());
        assertEquals(Arrays.asList("namespace1", "namespace2"), new ArrayList<>(cloneExtension.getAllowedNamespaces()));
    }

    @Test
    void equals()
    {
        AbstractExtension extension = toExtension("id", "version");
        AbstractExtension cloneExtension = new TestExtension(extension);

        assertEquals(extension, cloneExtension);

        cloneExtension.setCategory("othercategory");

        assertEquals(extension, cloneExtension);

        AbstractExtension extensionWithDifferentId = toExtension("otherid", "version");

        assertNotEquals(extension, extensionWithDifferentId);

        AbstractExtension extensionWithDifferentVersion = toExtension("id", "otherversion");

        assertNotEquals(extension, extensionWithDifferentVersion);
    }
}
