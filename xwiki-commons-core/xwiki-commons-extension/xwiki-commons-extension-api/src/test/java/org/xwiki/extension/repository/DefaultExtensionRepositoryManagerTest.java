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
package org.xwiki.extension.repository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.internal.DefaultExtensionRepositoryManager;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertSame;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultExtensionRepositoryManager}.
 *
 * @version $Id$
 */
public class DefaultExtensionRepositoryManagerTest
{
    @Rule
    public MockitoComponentMockingRule<ExtensionRepositoryManager> mocker =
        new MockitoComponentMockingRule<ExtensionRepositoryManager>(DefaultExtensionRepositoryManager.class);

    private ExtensionRepository testRepository;

    private ExtensionRepositoryDescriptor descriptor;

    private ExtensionRepositoryDescriptor unsupportedDescriptor;

    private DefaultExtensionDependency dependency;

    private Extension testExtension;

    @Before
    public void before() throws Exception
    {
        final ExtensionRepositoryFactory factory =
            this.mocker.registerMockComponent(ExtensionRepositoryFactory.class, "test");
        this.testRepository = mock(ExtensionRepository.class);

        this.descriptor =
            new DefaultExtensionRepositoryDescriptor("id", "test", new URI("http", "host", "/path", "fragment"));

        this.unsupportedDescriptor =
            new DefaultExtensionRepositoryDescriptor("unsupported", "unsupported", new URI("http", "unsupported",
                "/unsupported", "unsupported"));

        when(factory.createRepository(same(this.descriptor))).thenReturn(this.testRepository);
        when(this.testRepository.getDescriptor()).thenReturn(this.descriptor);

        this.dependency = new DefaultExtensionDependency("id", new DefaultVersionConstraint("version"));

        this.testExtension = mock(Extension.class);
        when(this.testRepository.resolve(this.dependency)).thenReturn(testExtension);
    }

    @Test
    public void addRepository() throws ExtensionRepositoryException, ComponentLookupException
    {
        ExtensionRepository repository = this.mocker.getComponentUnderTest().addRepository(this.descriptor);

        Assert.assertSame(this.testRepository, repository);
    }

    // Tests

    @Test(expected = ExtensionRepositoryException.class)
    public void addRepository_unsuported() throws ComponentLookupException, ExtensionRepositoryException
    {
        this.mocker.getComponentUnderTest().addRepository(this.unsupportedDescriptor);
    }

    @Test
    public void getRepository() throws ExtensionRepositoryException, ComponentLookupException
    {
        this.mocker.getComponentUnderTest().addRepository(this.descriptor);

        ExtensionRepository repository = this.mocker.getComponentUnderTest().getRepository("id");

        Assert.assertSame(this.testRepository, repository);
    }

    @Test
    public void getRepository_doesnotexists() throws ComponentLookupException
    {
        ExtensionRepository repository = this.mocker.getComponentUnderTest().getRepository("id");

        Assert.assertNull(repository);
    }

    @Test
    public void removeRepository() throws ExtensionRepositoryException, ComponentLookupException
    {
        this.mocker.getComponentUnderTest().addRepository(this.descriptor);

        this.mocker.getComponentUnderTest().removeRepository(this.descriptor.getId());

        ExtensionRepository repository = this.mocker.getComponentUnderTest().getRepository("id");

        Assert.assertNull(repository);
    }

    @Test
    public void getRepositories() throws ExtensionRepositoryException, ComponentLookupException
    {
        this.mocker.getComponentUnderTest().addRepository(this.descriptor);

        Collection<ExtensionRepository> repositorties = this.mocker.getComponentUnderTest().getRepositories();

        Assert.assertEquals(Arrays.asList(this.testRepository), new ArrayList<ExtensionRepository>(repositorties));
    }

    @Test(expected = ResolveException.class)
    public void resolveDependencyWithNoRegisteredRepository() throws ResolveException, ComponentLookupException
    {
        this.mocker.getComponentUnderTest().resolve(this.dependency);
    }

    @Test
    public void resolveDependencyWithEmbeddedRepository() throws ResolveException, ComponentLookupException
    {
        this.dependency.addRepository(this.testRepository.getDescriptor());

        assertSame(this.testExtension, this.mocker.getComponentUnderTest().resolve(this.dependency));
    }

    @Test
    public void resolveDependencyWithRegisteredRepository() throws ResolveException, ComponentLookupException
    {
        this.mocker.getComponentUnderTest().addRepository(this.testRepository);

        assertSame(this.testExtension, this.mocker.getComponentUnderTest().resolve(this.dependency));
    }
}
