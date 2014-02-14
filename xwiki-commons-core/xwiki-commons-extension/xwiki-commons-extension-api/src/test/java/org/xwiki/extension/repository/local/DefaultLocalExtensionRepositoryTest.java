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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.TestResources;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.test.MockitoRepositoryUtilsRule;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

@AllComponents
public class DefaultLocalExtensionRepositoryTest
{
    private LocalExtensionRepository localExtensionRepository;

    private MockitoComponentManagerRule mocker = new MockitoComponentManagerRule();

    @Rule
    public MockitoRepositoryUtilsRule repositoryUtil = new MockitoRepositoryUtilsRule(this.mocker);

    @Before
    public void before() throws ComponentLookupException
    {
        this.localExtensionRepository = this.mocker.getInstance(LocalExtensionRepository.class);
    }

    @Test
    public void testInit()
    {
        Assert.assertTrue(this.localExtensionRepository.countExtensions() > 0);
    }

    @Test
    public void testResolve() throws ResolveException
    {
        try {
            this.localExtensionRepository.resolve(new ExtensionId("unexistingextension", "version"));

            Assert.fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        try {
            this.localExtensionRepository.resolve(new ExtensionId(TestResources.INSTALLED_ID.getId(), "wrongversion"));

            Assert.fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        Extension extension = this.localExtensionRepository.resolve(TestResources.INSTALLED_ID);

        Assert.assertNotNull(extension);
        Assert.assertEquals(TestResources.INSTALLED_ID, extension.getId());
    }

    @Test
    public void testResolveDependency() throws ResolveException
    {
        try {
            this.localExtensionRepository.resolve(new DefaultExtensionDependency("unexistingextension",
                new DefaultVersionConstraint("version")));

            Assert.fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        try {
            this.localExtensionRepository.resolve(new DefaultExtensionDependency(TestResources.INSTALLED_ID.getId(),
                new DefaultVersionConstraint("wrongversion")));

            Assert.fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        Extension extension =
            this.localExtensionRepository.resolve(new DefaultExtensionDependency(TestResources.INSTALLED_ID.getId(),
                new DefaultVersionConstraint(TestResources.INSTALLED_ID.getVersion().getValue())));

        Assert.assertNotNull(extension);
        Assert.assertEquals(TestResources.INSTALLED_ID, extension.getId());
    }

    @Test
    public void testSearch() throws SearchException
    {
        CollectionIterableResult<Extension> result =
            (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 0, -1);

        Assert.assertEquals(7, result.getTotalHits());
        Assert.assertEquals(7, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search("", 0, -1);

        Assert.assertEquals(7, result.getTotalHits());
        Assert.assertEquals(7, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search("extension", 0, -1);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(3, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search("dependency", 0, -1);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(3, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 0, 0);

        Assert.assertEquals(7, result.getTotalHits());
        Assert.assertEquals(0, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 0, 2);

        Assert.assertEquals(7, result.getTotalHits());
        Assert.assertEquals(2, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 0, 1);

        Assert.assertEquals(7, result.getTotalHits());
        Assert.assertEquals(1, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 1, 2);

        Assert.assertEquals(7, result.getTotalHits());
        Assert.assertEquals(2, result.getSize());
        Assert.assertEquals(1, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, 2, 2);

        Assert.assertEquals(7, result.getTotalHits());
        Assert.assertEquals(2, result.getSize());
        Assert.assertEquals(2, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, -1, 2);

        Assert.assertEquals(7, result.getTotalHits());
        Assert.assertEquals(2, result.getSize());
        Assert.assertEquals(-1, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.localExtensionRepository.search(null, -1, 1);

        Assert.assertEquals(7, result.getTotalHits());
        Assert.assertEquals(1, result.getSize());
        Assert.assertEquals(-1, result.getOffset());
    }

    @Test
    public void testRemove() throws ResolveException, SearchException
    {
        LocalExtension localExtension = this.localExtensionRepository.resolve(TestResources.INSTALLED_ID);

        this.localExtensionRepository.removeExtension(localExtension);

        try {
            this.localExtensionRepository.resolve(TestResources.INSTALLED_ID);
            Assert.fail("Extension [" + TestResources.INSTALLED_ID
                + "] should not exist anymore in the local repository");
        } catch (ResolveException e) {
            // expected
        }

        Assert.assertFalse(this.localExtensionRepository.getLocalExtensionVersions(TestResources.INSTALLED_ID.getId())
            .contains(localExtension));
        Assert.assertFalse(this.localExtensionRepository.getLocalExtensionVersions(
            TestResources.INSTALLED_ID.getId() + "-feature").contains(localExtension));
    }
}
