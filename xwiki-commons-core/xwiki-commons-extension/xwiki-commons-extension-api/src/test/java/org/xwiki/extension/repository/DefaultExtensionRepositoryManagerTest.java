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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.internal.DefaultExtensionRepositoryManager;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultExtensionRepositoryManager}.
 *
 * @version $Id$
 */
@ComponentTest
public class DefaultExtensionRepositoryManagerTest
{
    @InjectMockComponents
    private DefaultExtensionRepositoryManager manager;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    private ExtensionRepository testRepository;

    private ExtensionRepositoryDescriptor descriptor;

    private ExtensionRepositoryDescriptor unsupportedDescriptor;

    private DefaultExtensionDependency dependency;

    private Extension testExtension;

    private ExtensionRepositoryFactory factory;

    @BeforeEach
    public void before() throws Exception
    {
        this.factory = this.componentManager.registerMockComponent(ExtensionRepositoryFactory.class, "test");
        this.testRepository = mock(ExtensionRepository.class);

        this.descriptor =
            new DefaultExtensionRepositoryDescriptor("id", "test", new URI("http", "host", "/path", "fragment"));

        this.unsupportedDescriptor = new DefaultExtensionRepositoryDescriptor("unsupported", "unsupported",
            new URI("http", "unsupported", "/unsupported", "unsupported"));

        when(factory.createRepository(same(this.descriptor))).thenReturn(this.testRepository);
        when(this.testRepository.getDescriptor()).thenReturn(this.descriptor);

        this.dependency = new DefaultExtensionDependency("id", new DefaultVersionConstraint("version"));

        this.testExtension = mock(Extension.class);
        when(this.testRepository.resolve(this.dependency)).thenReturn(testExtension);
    }

    // Tests

    @Test
    public void addRepository() throws ExtensionRepositoryException
    {
        ExtensionRepository repository = this.manager.addRepository(this.descriptor);

        assertSame(this.testRepository, repository);
    }

    @Test
    public void addRepository_unsuported()
    {
        assertThrows(ExtensionRepositoryException.class, () -> {
            this.manager.addRepository(this.unsupportedDescriptor);
        });
    }

    @Test
    public void getRepository() throws ExtensionRepositoryException, ComponentLookupException
    {
        this.manager.addRepository(this.descriptor);

        ExtensionRepository repository = this.manager.getRepository("id");

        assertSame(this.testRepository, repository);
    }

    @Test
    public void getRepository_doesnotexists()
    {
        ExtensionRepository repository = this.manager.getRepository("id");

        assertNull(repository);
    }

    @Test
    public void removeRepository() throws ExtensionRepositoryException
    {
        this.manager.addRepository(this.descriptor);

        this.manager.removeRepository(this.descriptor.getId());

        ExtensionRepository repository = this.manager.getRepository("id");

        assertNull(repository);
    }

    @Test
    public void getRepositories() throws ExtensionRepositoryException
    {
        this.manager.addRepository(this.descriptor);

        Collection<ExtensionRepository> repositorties = this.manager.getRepositories();

        assertEquals(Arrays.asList(this.testRepository), new ArrayList<ExtensionRepository>(repositorties));
    }

    @Test
    public void resolveDependencyWithNoRegisteredRepository()
    {
        assertThrows(ResolveException.class, () -> {
            this.manager.resolve(this.dependency);
        });
    }

    @Test
    public void resolveDependencyWithEmbeddedRepository() throws ResolveException
    {
        this.dependency.addRepository(this.testRepository.getDescriptor());

        assertSame(this.testExtension, this.manager.resolve(this.dependency));
    }

    @Test
    public void resolveDependencyWithRegisteredRepository() throws ResolveException
    {
        this.manager.addRepository(this.testRepository);

        assertSame(this.testExtension, this.manager.resolve(this.dependency));
    }

    @Test
    public void repositoryOrder() throws URISyntaxException, ExtensionRepositoryException
    {
        ExtensionRepositoryDescriptor repDescriptor0 =
            new DefaultExtensionRepositoryDescriptor("id0", "test", new URI("http", "host", "/path", "fragment"));
        ExtensionRepository rep0 = mock(ExtensionRepository.class, "repo0");
        when(this.factory.createRepository(same(repDescriptor0))).thenReturn(rep0);
        when(rep0.getDescriptor()).thenReturn(repDescriptor0);
        ExtensionRepositoryDescriptor repDescriptor1 =
            new DefaultExtensionRepositoryDescriptor("id1", "test", new URI("http", "host", "/path", "fragment"));
        ExtensionRepository rep1 = mock(ExtensionRepository.class, "repo1");
        when(this.factory.createRepository(same(repDescriptor1))).thenReturn(rep1);
        when(rep1.getDescriptor()).thenReturn(repDescriptor1);
        ExtensionRepositoryDescriptor repDescriptor2 =
            new DefaultExtensionRepositoryDescriptor("id2", "test", new URI("http", "host", "/path", "fragment"));
        ExtensionRepository rep2 = mock(ExtensionRepository.class, "repo2");
        when(this.factory.createRepository(same(repDescriptor2))).thenReturn(rep2);
        when(rep2.getDescriptor()).thenReturn(repDescriptor2);
        ExtensionRepositoryDescriptor repDescriptor3 =
            new DefaultExtensionRepositoryDescriptor("id3", "test", new URI("http", "host", "/path", "fragment"));
        ExtensionRepository rep3 = mock(ExtensionRepository.class, "repo3");
        when(this.factory.createRepository(same(repDescriptor3))).thenReturn(rep3);
        when(rep3.getDescriptor()).thenReturn(repDescriptor3);

        this.manager.addRepository(repDescriptor3, 3);
        this.manager.addRepository(repDescriptor2, 2);
        this.manager.addRepository(repDescriptor1, 1);
        this.manager.addRepository(repDescriptor0, 0);

        Collection<ExtensionRepository> repositories = this.manager.getRepositories();
        Iterator<ExtensionRepository> it = repositories.iterator();

        assertSame(rep0, it.next());
        assertSame(rep1, it.next());
        assertSame(rep2, it.next());
        assertSame(rep3, it.next());
    }
}
