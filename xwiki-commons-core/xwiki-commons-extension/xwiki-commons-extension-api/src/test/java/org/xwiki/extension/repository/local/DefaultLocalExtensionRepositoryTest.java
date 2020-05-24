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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.TestResources;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.ExtensionQuery.COMPARISON;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.test.MockitoRepositoryUtilsExtension;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ComponentTest
@ExtendWith(MockitoRepositoryUtilsExtension.class)
@AllComponents
public class DefaultLocalExtensionRepositoryTest
{
    private LocalExtensionRepository localExtensionRepository;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @BeforeEach
    public void before() throws ComponentLookupException
    {
        this.localExtensionRepository = this.componentManager.getInstance(LocalExtensionRepository.class);
    }

    @Test
    void testInit()
    {
        assertTrue(this.localExtensionRepository.countExtensions() > 0);
    }

    @Test
    void testResolve() throws ResolveException
    {
        assertThrows(ResolveException.class, () -> {
            this.localExtensionRepository.resolve(new ExtensionId("unexistingextension", "version"));
        });

        assertThrows(ResolveException.class, () -> {
            this.localExtensionRepository.resolve(new ExtensionId(TestResources.INSTALLED_ID.getId(), "wrongversion"));
        });

        Extension extension = this.localExtensionRepository.resolve(TestResources.INSTALLED_ID);

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ID, extension.getId());
    }

    @Test
    void testResolveExistingDependency() throws ResolveException
    {
        Extension extension =
            this.localExtensionRepository.resolve(new DefaultExtensionDependency(TestResources.INSTALLED_ID.getId(),
                new DefaultVersionConstraint(TestResources.INSTALLED_ID.getVersion().getValue())));

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ID, extension.getId());
    }

    @Test
    void testResolveUnexistingDependencyId()
    {
        assertThrows(ResolveException.class, () -> {
            this.localExtensionRepository.resolve(
                new DefaultExtensionDependency("unexistingextension", new DefaultVersionConstraint("version")));
        });
    }

    @Test
    void testResolveIncompatibleDependencyVersion()
    {
        assertThrows(ResolveException.class, () -> {
            this.localExtensionRepository.resolve(new DefaultExtensionDependency(TestResources.INSTALLED_ID.getId(),
                new DefaultVersionConstraint("wrongversion")));
        });
    }

    @Test
    void testResolveUnexistingButFeatureCompatibleDependency()
    {
        assertThrows(ResolveException.class, () -> {
            this.localExtensionRepository.resolve(new DefaultExtensionDependency("installedextension-feature",
                new DefaultVersionConstraint(TestResources.INSTALLED_ID.getVersion().getValue())));
        });
    }

    @Test
    void testResolveUnexistingButSmalerVersionDependency()
    {
        assertThrows(ResolveException.class, () -> {
            this.localExtensionRepository.resolve(
                new DefaultExtensionDependency(TestResources.INSTALLED_ID.getId(),
                    new DefaultVersionConstraint("0.9")));
        });
    }

    @Test
    void testSearch() throws SearchException
    {
        CollectionIterableResult<Extension> result =
            (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 0, -1);

        assertEquals(18, result.getTotalHits());
        assertEquals(18, result.getSize());
        assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search("", 0, -1);

        assertEquals(18, result.getTotalHits());
        assertEquals(18, result.getSize());
        assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search("extension", 0, -1);

        assertEquals(5, result.getTotalHits());
        assertEquals(5, result.getSize());
        assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search("dependency", 0, -1);

        assertEquals(10, result.getTotalHits());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 0, 0);

        assertEquals(18, result.getTotalHits());
        assertEquals(0, result.getSize());
        assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 0, 2);

        assertEquals(18, result.getTotalHits());
        assertEquals(2, result.getSize());
        assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 0, 1);

        assertEquals(18, result.getTotalHits());
        assertEquals(1, result.getSize());
        assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 1, 2);

        assertEquals(18, result.getTotalHits());
        assertEquals(2, result.getSize());
        assertEquals(1, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 2, 2);

        assertEquals(18, result.getTotalHits());
        assertEquals(2, result.getSize());
        assertEquals(2, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, -1, 2);

        assertEquals(18, result.getTotalHits());
        assertEquals(2, result.getSize());
        assertEquals(-1, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, -1, 1);

        assertEquals(18, result.getTotalHits());
        assertEquals(1, result.getSize());
        assertEquals(-1, result.getOffset());
    }

    @Test
    void testSearchWithQueryEQUAL() throws SearchException
    {
        ExtensionQuery query = new ExtensionQuery();

        query.addFilter(Extension.FIELD_ID, TestResources.INSTALLED_ID.getId(), COMPARISON.EQUAL);

        IterableResult<Extension> result = this.localExtensionRepository.search(query);

        assertEquals(1, result.getTotalHits());
        assertEquals(1, result.getSize());
        assertEquals(0, result.getOffset());
        assertEquals(TestResources.INSTALLED_ID, result.iterator().next().getId());
    }

    @Test
    void testSearchWithQueryMATCH() throws SearchException
    {
        ExtensionQuery query = new ExtensionQuery();

        query.addFilter(Extension.FIELD_ID, TestResources.INSTALLED_ID.getId(), COMPARISON.MATCH);

        IterableResult<Extension> result = this.localExtensionRepository.search(query);

        assertEquals(3, result.getTotalHits());
        assertEquals(3, result.getSize());
        assertEquals(0, result.getOffset());
    }

    @Test
    void testSearchWithQueryWithNullValue() throws SearchException
    {
        ExtensionQuery query = new ExtensionQuery();

        query.addFilter(Extension.FIELD_CATEGORY, "category", COMPARISON.EQUAL);

        IterableResult<Extension> result = this.localExtensionRepository.search(query);

        assertEquals(0, result.getTotalHits());
        assertEquals(0, result.getSize());
        assertEquals(0, result.getOffset());
    }

    @Test
    void testRemove() throws ResolveException
    {
        LocalExtension localExtension = this.localExtensionRepository.resolve(TestResources.INSTALLED_ID);

        this.localExtensionRepository.removeExtension(localExtension);

        assertThrows(ResolveException.class, () -> {
            this.localExtensionRepository.resolve(TestResources.INSTALLED_ID);
        });

        assertFalse(this.localExtensionRepository.getLocalExtensionVersions(TestResources.INSTALLED_ID.getId())
            .contains(localExtension));
        assertFalse(this.localExtensionRepository
            .getLocalExtensionVersions(TestResources.INSTALLED_ID.getId() + "-feature").contains(localExtension));
    }

    @Test
    void resolveVersions() throws ResolveException
    {
        IterableResult<Version> versions =
            this.localExtensionRepository.resolveVersions(TestResources.INSTALLED_ONNAMESPACE_ID.getId(), 0, -1);

        assertEquals(1, versions.getTotalHits());
        assertEquals(1, versions.getSize());
        assertEquals(TestResources.INSTALLED_ONNAMESPACE_ID.getVersion(), versions.iterator().next());
    }
}
