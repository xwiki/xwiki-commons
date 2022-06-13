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
package org.xwiki.test.jmock;

import java.lang.reflect.Type;

import org.jmock.Mockery;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Adds method to {@link EmbeddableComponentManager} to easily register mock components when writing tests.
 *
 * @version $Id$
 * @since 4.2M1
 * @deprecated use {@link org.xwiki.test.junit5.mockito.ComponentTest} instead
 */
@Deprecated(since = "4.3.1")
public class MockingComponentManager extends EmbeddableComponentManager
{
    /**
     * Register a mock component.
     *
     * @param mockery the JMock mockery instance to create the mock component
     * @param role the role of the component to register
     * @param hint the hint of the component to register
     * @param mockId the JMock id (this is needed when registering several mocks for the same interface)
     * @param <T> the component role class
     * @return the registered mock component instance
     * @throws Exception if the registration failed
     */
    public <T> T registerMockComponent(Mockery mockery, Type role, String hint, String mockId) throws Exception
    {
        DefaultComponentDescriptor<T> descriptor = createComponentDescriptor(role);
        descriptor.setRoleHint(hint);
        return registerMockComponent(mockery, descriptor, mockId);
    }

    /**
     * Register a mock component.
     *
     * @param mockery the JMock mockery instance to create the mock component
     * @param role the role of the component to register
     * @param hint the hint of the component to register
     * @param <T> the component role class
     * @return the registered mock component instance
     * @throws Exception if the registration failed
     */
    public <T> T registerMockComponent(Mockery mockery, Type role, String hint) throws Exception
    {
        return registerMockComponent(mockery, role, hint, null);
    }

    /**
     * Register a mock component.
     *
     * @param mockery the JMock mockery instance to create the mock component
     * @param role the role of the component to register
     * @param <T> the component role class
     * @return the registered mock component instance
     * @throws Exception if the registration failed
     */
    public <T> T registerMockComponent(Mockery mockery, Type role) throws Exception
    {
        return registerMockComponent(mockery, this.<T>createComponentDescriptor(role));
    }

    /**
     * Register a mock component.
     *
     * @param mockery the JMock mockery instance to create the mock component
     * @param role the role of the component to register
     * @param mockId the JMock id (this is needed when registering several mocks for the same interface)
     * @param <T> the component role class
     * @return the registered mock component instance
     * @throws Exception if the registration failed
     */
    public <T> T registerMockComponentWithId(Mockery mockery, Type role, String mockId) throws Exception
    {
        return registerMockComponent(mockery, this.<T>createComponentDescriptor(role), mockId);
    }

    /**
     * Register a mock component.
     *
     * @param mockery the JMock mockery instance to create the mock component
     * @param descriptor the component descriptor for the component to register
     * @param <T> the component role class
     * @return the registered mock component instance
     * @throws Exception if the registration failed
     */
    private <T> T registerMockComponent(Mockery mockery, ComponentDescriptor<T> descriptor) throws Exception
    {
        return registerMockComponent(mockery, descriptor, null);
    }

    /**
     * Register a mock component.
     *
     * @param mockery the JMock mockery instance to create the mock component
     * @param descriptor the component descriptor for the component to register
     * @param mockId the JMock id (this is needed when registering several mocks for the same interface)
     * @param <T> the component role class
     * @return the registered mock component instance
     * @throws Exception if the registration failed
     */
    @SuppressWarnings("unchecked")
    private <T> T registerMockComponent(Mockery mockery, ComponentDescriptor<T> descriptor, String mockId)
        throws Exception
    {
        T instance;
        if (mockId != null) {
            instance = mockery.mock((Class<T>) ReflectionUtils.getTypeClass(descriptor.getRoleType()), mockId);
        } else {
            instance =
                mockery.mock((Class<T>) ReflectionUtils.getTypeClass(descriptor.getRoleType()), descriptor
                    .getRoleType().toString());
        }

        registerComponent(descriptor, instance);

        return instance;
    }

    /**
     * Create a Component Descriptor.
     *
     * @param role the role of the component to register
     * @param <T> the component role class
     * @return the component descriptor
     */
    private <T> DefaultComponentDescriptor<T> createComponentDescriptor(Type role)
    {
        DefaultComponentDescriptor<T> descriptor = new DefaultComponentDescriptor<>();
        descriptor.setRoleType(role);

        return descriptor;
    }
}
