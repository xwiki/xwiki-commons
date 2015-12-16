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
package org.xwiki.test.mockito;

import java.lang.reflect.Type;

import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.TestComponentManager;

import static org.mockito.Mockito.mock;

/**
 * Helper class to make it extra simple to register Mock Components using Mockito.
 *
 * @version $Id$
 * @since 5.1RC1
 */
public class MockitoComponentManager extends TestComponentManager
{
    /**
     * Registers a Mock component.
     *
     * @param roleType the type of the component role to register
     * @param roleHint the role hint of the component to register
     * @param <T> the mock
     * @return the mock
     * @throws Exception in case of an error during registration
     */
    public <T> T registerMockComponent(Type roleType, String roleHint) throws Exception
    {
        T mock = (T) mock(ReflectionUtils.getTypeClass(roleType));
        registerComponent(roleType, roleHint, mock);
        return mock;
    }

    /**
     * Registers a Mock component (using the default role hint).
     *
     * @param roleType the type of the component role to register
     * @param <T> the mock
     * @return the mock
     * @throws Exception in case of an error during registration
     */
    public <T> T registerMockComponent(Type roleType) throws Exception
    {
        return registerMockComponent(roleType, null);
    }
}
