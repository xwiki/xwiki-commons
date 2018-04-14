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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.annotation.ComponentDescriptorFactory;
import org.xwiki.component.annotation.Role;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.util.ReflectionUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * See javadoc for {@link MockitoComponentMockingRule}.
 *
 * @param <T> the component role type that we're mocking
 * @version $Id$
 * @since 10.3RC1
 */
public class MockitoComponentMocker<T>
{
    /**
     * Used to create Component Descriptors based on annotations.
     */
    private ComponentDescriptorFactory factory = new ComponentDescriptorFactory();

    /**
     * Used to discover and register components using annotations.
     */
    private ComponentAnnotationLoader loader = new ComponentAnnotationLoader();

    private MockitoComponentManager componentManager;

    /**
     * The class of the component implementation to mock.
     */
    private Class<?> componentImplementationClass;

    /**
     * The role Type if the component implementation implements several roles.
     */
    private Type componentRoleType;

    /**
     * The role Hint if the component implementation implements several roles.
     */
    private String componentRoleHint;

    /**
     * The list of component Roles that shouldn't be mocked.
     */
    private List<Class<?>> excludedComponentRoleDependencies = new ArrayList<Class<?>>();

    /**
     * @param componentManager the mockito component manager in which to register mock components
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     */
    public MockitoComponentMocker(MockitoComponentManager componentManager,
        Class<? extends T> componentImplementationClass)
    {
        this(componentManager, componentImplementationClass, (Type) null);
    }

    /**
     * @param componentManager the mockito component manager in which to register mock components
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     */
    public MockitoComponentMocker(MockitoComponentManager componentManager,
        Class<? extends T> componentImplementationClass, Type componentRoleType)
    {
        this(componentManager, componentImplementationClass, componentRoleType, null);
    }

    /**
     * @param componentManager the mockito component manager in which to register mock components
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param componentRoleHint the role hint of the component implementation (when it has several), for disambiguation
     */
    public MockitoComponentMocker(MockitoComponentManager componentManager, Class<?> componentImplementationClass,
        Type componentRoleType, String componentRoleHint)
    {
        this(componentManager, componentImplementationClass, componentRoleType, componentRoleHint,
            Collections.emptyList());
    }

    /**
     * @param componentManager the mockito component manager in which to register mock components
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param componentRoleHint the role hint of the component implementation (when it has several), for disambiguation
     * @param excludedComponentRoleDependencies list of component dependency role classes that we don't want mocked
     */
    public MockitoComponentMocker(MockitoComponentManager componentManager, Class<?> componentImplementationClass,
        Type componentRoleType, String componentRoleHint, List<? extends Class<?>> excludedComponentRoleDependencies)
    {
        this.componentManager = componentManager;
        this.componentImplementationClass = componentImplementationClass;
        this.componentRoleType = componentRoleType;
        this.componentRoleHint = componentRoleHint;
        this.excludedComponentRoleDependencies.addAll(excludedComponentRoleDependencies);
    }

    /**
     * @param componentManager the mockito component manager in which to register mock components
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param excludedComponentRoleDependencies list of component dependency role classes that we don't want mocked
     */
    public MockitoComponentMocker(MockitoComponentManager componentManager,
        Class<? extends T> componentImplementationClass, List<? extends Class<?>> excludedComponentRoleDependencies)
    {
        this(componentManager, componentImplementationClass, null, null, excludedComponentRoleDependencies);
    }

    /**
     * Mock the injected components for the specified component implementation.
     *
     * @param testInstance the test instance
     * @return the mocked component role/hint or null if invalid
     * @throws Exception in case of an error while mocking
     */
    public RoleHint<T> mockComponent(final Object testInstance) throws Exception
    {
        // Handle component fields
        for (ComponentDescriptor<T> descriptor : this.factory
            .createComponentDescriptors(this.componentImplementationClass, findComponentRoleType())) {
            // Only use the descriptor for the specified hint
            if ((this.componentRoleHint != null && this.componentRoleHint.equals(descriptor.getRoleHint()))
                || this.componentRoleHint == null) {
                registerMockDependencies(descriptor);
                this.componentManager.registerComponent(descriptor);
                return new RoleHint<T>(descriptor.getRoleType(), descriptor.getRoleHint());
            }
        }
        return null;
    }

    /**
     * @return the list of component dependency role classes that we don't want mocked
     */
    public List<Class<?>> getExcludedComponentRoleDependencies()
    {
        return this.excludedComponentRoleDependencies;
    }

    /**
     * @return the component implementation for which we wish to have its injection mocked
     */
    public Class<?> getComponentImplementationClass()
    {
        return this.componentImplementationClass;
    }

    /**
     * @return the Component role type extracted from the the component implementation class
     */
    private Type findComponentRoleType()
    {
        Type type;

        Set<Type> componentRoleTypes = this.loader.findComponentRoleTypes(this.componentImplementationClass);
        if (this.componentRoleType != null) {
            if (!componentRoleTypes.contains(this.componentRoleType)) {
                throw new RuntimeException(
                    "Specified Component Role ([" + this.componentRoleType + "]) not found in component");
            } else {
                type = this.componentRoleType;
            }
        } else {
            if (componentRoleTypes.isEmpty()) {
                throw new RuntimeException(
                    String.format("Couldn't find roles for component [%s]", this.componentRoleType));
            } else if (componentRoleTypes.size() > 1) {
                throw new RuntimeException("Components with several roles must explicitly specify which role to use.");
            } else {
                type = componentRoleTypes.iterator().next();
            }
        }
        return type;
    }

    /**
     * Create mocks of injected dependencies and registers them against the Component Manager.
     *
     * @param descriptor the descriptor of the component under test
     * @throws Exception if an error happened during registration
     */
    private void registerMockDependencies(ComponentDescriptor<T> descriptor) throws Exception
    {
        Collection<ComponentDependency<?>> dependencyDescriptors = descriptor.getComponentDependencies();
        for (ComponentDependency<?> dependencyDescriptor : dependencyDescriptors) {
            Class<?> roleTypeClass = ReflectionUtils.getTypeClass(dependencyDescriptor.getRoleType());
            // Only register a mock if it isn't:
            // - Already registered
            // - An explicit exception specified by the user
            // - A logger
            // - A collection of components, we want to keep them as Java collections. Those collections are later
            // filled by the component manager with available components. Developers can register mocked components
            // in an override of #setupDependencies().
            // TODO: Handle multiple roles/hints.
            if (!this.excludedComponentRoleDependencies.contains(roleTypeClass) && Logger.class != roleTypeClass
                && !roleTypeClass.isAssignableFrom(List.class) && !roleTypeClass.isAssignableFrom(Map.class)
                && !this.componentManager.hasComponent(dependencyDescriptor.getRoleType(),
                    dependencyDescriptor.getRoleHint()))
            {
                DefaultComponentDescriptor cd = new DefaultComponentDescriptor<>();

                cd.setRoleType(dependencyDescriptor.getRoleType());
                cd.setRoleHint(dependencyDescriptor.getRoleHint());

                Object dependencyMock = mock(roleTypeClass, dependencyDescriptor.getName());

                if (Provider.class == roleTypeClass) {
                    Type providedType = ReflectionUtils.getLastTypeGenericArgument(dependencyDescriptor.getRoleType());
                    Class providedClass = ReflectionUtils.getTypeClass(providedType);

                    // If the target is registered or if a list or a map are asked don't mock anything
                    if (this.componentManager.hasComponent(providedType, dependencyDescriptor.getRoleHint())
                        || providedClass.isAssignableFrom(List.class) || providedClass.isAssignableFrom(Map.class)) {
                        continue;
                    }

                    if (providedClass.getAnnotation(Role.class) != null) {
                        // If the dependency is a Provider for a @Role mock the @Role instead of the Provider
                        cd.setRoleType(providedType);
                        dependencyMock = mock(providedClass, dependencyDescriptor.getName());
                    } else {
                        // If the dependency is a Provider not targeting a @Role register a mock Provider which provide
                        // a mock
                        Provider provider = (Provider) dependencyMock;
                        when(provider.get()).thenReturn(mock(providedClass, providedType.toString()));
                    }
                }

                this.componentManager.registerComponent(cd, dependencyMock);
            }
        }
    }
}
