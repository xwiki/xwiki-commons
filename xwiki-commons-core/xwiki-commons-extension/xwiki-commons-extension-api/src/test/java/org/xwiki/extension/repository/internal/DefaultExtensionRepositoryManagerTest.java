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
package org.xwiki.extension.repository.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.repository.search.Searchable;
import org.xwiki.extension.test.ExtensionUtils;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.google.common.collect.Lists;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Test {@link DefaultExtensionRepositoryManager}.
 *
 * @version $Id$
 */
public class DefaultExtensionRepositoryManagerTest
{
    @Rule
    public MockitoComponentMockingRule<ExtensionRepositoryManager> mock =
        new MockitoComponentMockingRule<ExtensionRepositoryManager>(DefaultExtensionRepositoryManager.class);

    private ExtensionRepository mockRepository1;

    private Searchable mockSearchableRepository1;

    private ExtensionRepository mockRepository2;

    private Searchable mockSearchableRepository2;

    private Extension extension1;

    private Extension extension2;

    private Extension extension3;

    private Extension extension4;

    @Before
    public void configure() throws Exception
    {
        this.mockRepository1 =
            mock(ExtensionRepository.class, withSettings().extraInterfaces(Searchable.class).name("repository1"));
        this.mockSearchableRepository1 = (Searchable) this.mockRepository1;
        when(this.mockRepository1.getDescriptor())
            .thenReturn(new DefaultExtensionRepositoryDescriptor("repository1", "type", new URI("uri:uri")));
        this.mock.getComponentUnderTest().addRepository(this.mockRepository1);

        this.mockRepository2 =
            mock(ExtensionRepository.class, withSettings().extraInterfaces(Searchable.class).name("repository2"));
        this.mockSearchableRepository2 = (Searchable) this.mockRepository2;
        when(this.mockRepository2.getDescriptor())
            .thenReturn(new DefaultExtensionRepositoryDescriptor("repository2", "type", new URI("uri:uri")));
        this.mock.getComponentUnderTest().addRepository(this.mockRepository2);

        this.extension1 = ExtensionUtils.mockExtension("id1", "version1");
        this.extension2 = ExtensionUtils.mockExtension("id2", "version2");
        this.extension3 = ExtensionUtils.mockExtension("id3", "version3");
        this.extension4 = ExtensionUtils.mockExtension("id4", "version4");
    }

    private List<Version> toVersionList(String... versions)
    {
        List<Version> versionList = new ArrayList<Version>(versions.length);

        for (String version : versions) {
            versionList.add(new DefaultVersion(version));
        }

        return versionList;
    }

    private IterableResult<Version> toIterableVersions(String... versions)
    {
        List<Version> versionList = toVersionList(versions);

        return new CollectionIterableResult<Version>(versionList.size(), 0, versionList);
    }

    private IterableResult<Extension> toIterableExtensions(Extension... extensions)
    {
        return new CollectionIterableResult<Extension>(extensions.length, 0, Arrays.asList(extensions));
    }

    private void assertSameElements(Iterable<?> it1, Iterable<?> it2)
    {
        Iterator<?> i1 = it1.iterator();
        Iterator<?> i2 = it2.iterator();

        while (i1.hasNext() && i2.hasNext()) {
            Object element1 = i1.next();
            Object element2 = i2.next();
            if (element1 != element2) {
                Assert.fail("[" + element1 + "] is not the same as [" + element2 + "]");
            }
        }
    }

    private void assertResolveVersions(String id, int offset, int nb, String... versions)
        throws ResolveException, ComponentLookupException
    {
        Assert.assertEquals(toVersionList(versions),
            Lists.newArrayList(this.mock.getComponentUnderTest().resolveVersions("id", offset, nb)));
    }

    private void assertSearch(String id, int offset, int nb, Extension... extensions)
        throws ComponentLookupException, SearchException
    {
        assertSameElements(Arrays.asList(extensions), this.mock.getComponentUnderTest().search("id", offset, nb));
    }

    // Tests

    @Test
    public void resolveVersions() throws ResolveException, ComponentLookupException
    {
        when(this.mockRepository1.resolveVersions("id", 0, -1)).thenReturn(toIterableVersions("1.0", "2.0"));
        when(this.mockRepository2.resolveVersions("id", 0, -1)).thenReturn(toIterableVersions("3.0", "2.0", "4.0"));

        assertResolveVersions("id", 0, -1, "1.0", "2.0", "3.0", "4.0");
        assertResolveVersions("id", 0, 1, "1.0");
        assertResolveVersions("id", 1, -1, "2.0", "3.0", "4.0");
    }

    @Test
    public void search() throws SearchException, ComponentLookupException
    {
        when(this.mockSearchableRepository1.search("pattern", 0, -1))
            .thenReturn(toIterableExtensions(this.extension1, this.extension2));
        when(this.mockSearchableRepository2.search("pattern", 0, -1))
            .thenReturn(toIterableExtensions(this.extension3, this.extension4));

        assertSearch("pattern", 0, -1, this.extension1, this.extension2, this.extension3, this.extension4);
        assertSearch("pattern", 0, 1, this.extension1);
        assertSearch("pattern", 1, -1, this.extension2, this.extension3, this.extension4);

        when(this.mockSearchableRepository1.search("pattern", 0, -1)).thenReturn(toIterableExtensions());
        when(this.mockSearchableRepository2.search("pattern", 0, -1))
            .thenReturn(toIterableExtensions(this.extension3, this.extension4));

        assertSearch("pattern", 0, -1, this.extension3, this.extension4);
        assertSearch("pattern", 0, 1, this.extension3);
        assertSearch("pattern", 1, -1, this.extension4);

        when(this.mockSearchableRepository1.search("pattern", 0, -1))
            .thenReturn(toIterableExtensions(this.extension1, this.extension2));
        when(this.mockSearchableRepository2.search("pattern", 0, -1)).thenReturn(toIterableExtensions());

        assertSearch("pattern", 0, -1, this.extension1, this.extension2, this.extension3, this.extension4);
        assertSearch("pattern", 0, 1, this.extension1);
        assertSearch("pattern", 1, -1, this.extension2, this.extension3, this.extension4);
    }
}
