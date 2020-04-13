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
package org.xwiki.extension.repository.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.environment.Environment;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtensionRepository;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

@AllComponents
public class DefaultCoreExtensionRepositoryTest
{
    @Rule
    public MockitoComponentMockingRule<CoreExtensionRepository> mocker =
        new MockitoComponentMockingRule<>(DefaultCoreExtensionRepository.class);

    private CoreExtensionRepository coreExtensionRepository;

    @Before
    public void before() throws Exception
    {
        this.mocker.registerMockComponent(Environment.class);

        this.coreExtensionRepository = this.mocker.getInstance(CoreExtensionRepository.class);
    }

    /**
     * Validate core extension loading and others initializations.
     */
    @Test
    public void init()
    {
        Assert.assertTrue(this.coreExtensionRepository.countExtensions() > 0);
    }
}
