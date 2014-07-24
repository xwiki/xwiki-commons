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

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.repository.internal.DefaultExtensionRepositoryManager;
import org.xwiki.test.jmock.AbstractMockingComponentTestCase;
import org.xwiki.test.jmock.annotation.MockingRequirement;

/**
 * Unit tests for {@link DefaultExtensionRepositoryManager}.
 *
 * @version $Id$
 */
@MockingRequirement(value = DefaultExtensionRepositoryManager.class, exceptions = { ComponentManager.class })
public class DefaultExtensionRepositoryManagerTest extends AbstractMockingComponentTestCase<ExtensionRepositoryManager>
{
    private ExtensionRepository testRepository;

    private ExtensionRepositoryDescriptor descriptor;

    private ExtensionRepositoryDescriptor unsupportedDescriptor;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        final ExtensionRepositoryFactory factory = registerMockComponent(ExtensionRepositoryFactory.class, "test");
        this.testRepository = getMockery().mock(ExtensionRepository.class);

        this.descriptor =
            new DefaultExtensionRepositoryDescriptor("id", "test", new URI("http", "host", "/path", "fragment"));

        this.unsupportedDescriptor =
            new DefaultExtensionRepositoryDescriptor("unsupported", "unsupported", new URI("http", "unsupported",
                "/unsupported", "unsupported"));

        getMockery().checking(new Expectations()
        {
            {
                allowing(factory).createRepository(with(same(DefaultExtensionRepositoryManagerTest.this.descriptor)));
                will(returnValue(DefaultExtensionRepositoryManagerTest.this.testRepository));

                allowing(DefaultExtensionRepositoryManagerTest.this.testRepository).getDescriptor();
                will(returnValue(DefaultExtensionRepositoryManagerTest.this.descriptor));
            }
        });
    }

    @Test
    public void addRepository() throws ExtensionRepositoryException, ComponentLookupException
    {
        ExtensionRepository repository = getMockedComponent().addRepository(this.descriptor);

        Assert.assertSame(this.testRepository, repository);
    }

    // Tests

    @Test(expected = ExtensionRepositoryException.class)
    public void addRepository_unsuported() throws ComponentLookupException, ExtensionRepositoryException
    {
        getMockedComponent().addRepository(this.unsupportedDescriptor);
    }

    @Test
    public void getRepository() throws ExtensionRepositoryException, ComponentLookupException
    {
        getMockedComponent().addRepository(this.descriptor);

        ExtensionRepository repository = getMockedComponent().getRepository("id");

        Assert.assertSame(this.testRepository, repository);
    }

    @Test
    public void getRepository_doesnotexists() throws ComponentLookupException
    {
        ExtensionRepository repository = getMockedComponent().getRepository("id");

        Assert.assertNull(repository);
    }

    @Test
    public void removeRepository() throws ExtensionRepositoryException, ComponentLookupException
    {
        getMockedComponent().addRepository(this.descriptor);

        getMockedComponent().removeRepository(this.descriptor.getId());

        ExtensionRepository repository = getMockedComponent().getRepository("id");

        Assert.assertNull(repository);
    }

    @Test
    public void getRepositories() throws ExtensionRepositoryException, ComponentLookupException
    {
        getMockedComponent().addRepository(this.descriptor);

        Collection<ExtensionRepository> repositorties = getMockedComponent().getRepositories();

        Assert.assertEquals(Arrays.asList(this.testRepository), new ArrayList<ExtensionRepository>(repositorties));
    }
}
