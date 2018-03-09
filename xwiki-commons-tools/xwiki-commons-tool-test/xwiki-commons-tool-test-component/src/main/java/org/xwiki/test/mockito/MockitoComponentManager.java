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

import org.xwiki.component.descriptor.ComponentDescriptor;
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
        return registerMockComponent(roleType, roleHint, true);
    }

    /**
     * Registers a Mock component.
     * <p>
     * If <code>force</code> is false the method will do nothing if there is already a mock and will return it.
     *
     * @param roleType the type of the component role to register
     * @param roleHint the role hint of the component to register
     * @param force force registering a new mock even if there is already one
     * @param <T> the mock
     * @return the mock
     * @throws Exception in case of an error during registration
     * @since 10.2RC1
     */
    public <T> T registerMockComponent(Type roleType, String roleHint, boolean force) throws Exception
    {
        // Check if the component is already mocked
        if (!force) {
            ComponentDescriptor<?> descriptor = getComponentDescriptor(roleType, roleHint);

            if (descriptor != null && descriptor.getImplementation() == null) {
                return getInstance(roleType, roleHint);
            }
        }

        // Mock the component and register it
        @SuppressWarnings("unchecked")
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

    /**
     * Registers a Mock component (using the default role hint).
     * <p>
     * If <code>force</code> is false the method will do nothing if there is already a mock and will return it.
     *
     * @param roleType the type of the component role to register
     * @param force force registering a new mock even if there is already one
     * @param <T> the mock
     * @return the mock
     * @throws Exception in case of an error during registration
     */
    public <T> T registerMockComponent(Type roleType, boolean force) throws Exception
    {
        return registerMockComponent(roleType, null, force);
    }
}
