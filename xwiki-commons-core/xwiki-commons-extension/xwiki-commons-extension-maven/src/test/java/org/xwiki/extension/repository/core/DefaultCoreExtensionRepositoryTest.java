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

import org.junit.jupiter.api.Test;
import org.xwiki.environment.Environment;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtensionRepository;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultCoreExtensionRepository}.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
public class DefaultCoreExtensionRepositoryTest
{
    @InjectMockComponents
    private DefaultCoreExtensionRepository coreExtensionRepository;

    @MockComponent
    private Environment environment;

    /**
     * Validate core extension loading and others initializations.
     */
    @Test
    void init()
    {
        assertTrue(this.coreExtensionRepository.countExtensions() > 0);
    }
}
