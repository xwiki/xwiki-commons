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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.annotation.ComponentDescriptorFactory;
import org.xwiki.component.annotation.Role;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.ReflectionUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Components should extend this class instead of using {@link MockitoComponentManagerRule} or
 * {@link org.xwiki.test.ComponentManagerRule} which should only be used for integration tests.
 * <p>
 * To use this class, define a JUnit {@code @Rule} and pass the component implementation class that you wish to have
 * mocked for you. Then in your test code, do a lookup of your component under test and you'll get a component instance
 * which has all its injected dependencies mocked automatically.
 * <p>
 * For example:
 * 
 * <pre>
 * {@code
 * public class MyComponentTest
 * {
 *     &#64;Rule
 *     public final MockitoComponentMockingRule&lt;MyComponent&gt; mocker =
 *         new MockitoComponentMockingRule(MyImplementation.class);
 * 
 *     &#64;Test
 *     public void someTest() throws Exception
 *     {
 *         MyComponent myComponent = mocker.getComponentUnderTest();
 *         ...
 *     }
 * ...
 * }
 * }
 * </pre>
 *
 * Note that by default there are no component registered against the component manager except those mocked
 * automatically by the Rule (except for the MockitoComponentMockingRule itself, which means that if your component
 * under test is injected a default ComponentManager, it'll be the MockitoComponentMockingRule which will get injected.
 * See more below). This has 2 advantages:
 * <ul>
 * <li>This is the spirit of this Rule since it's for unit testing and this testing your component in isolation from the
 * rest</li>
 * <li>It makes the tests up to 10 times faster</li>
 * </ul>
 * If you really need to register some components, use the {@link org.xwiki.test.annotation.ComponentList} annotation
 * and if you really really need to register all components (it takes time) then use
 * {@link org.xwiki.test.annotation.AllComponents}.
 * <p>
 * In addition, you can perform some action before any component is registered in the Component Manager by having one or
 * several methods annotated with {@link org.xwiki.test.annotation.BeforeComponent}. Similarly, you can perform an
 * action after all components have been registered in the Component Manager by having one or several methods annotated
 * with {@link org.xwiki.test.annotation.AfterComponent}.
 * <p>
 * This can be useful (for example) in the case you wish to register a mock ComponentManager in your component under
 * test. You would write:
 * 
 * <pre>
 * {@code
 * &#64;Rule
 * public final MockitoComponentManagerRule mocker = new MockitoComponentManagerRule();
 * 
 * &#64;AfterComponent
 * public void overrideComponents() throws Exception
 * {
 *     this.mocker.registerMockComponent(ComponentManager.class);
 * }
 * }
 * </pre>
 *
 * @param <T> the component role type, used to provide a typed instance when calling {@link #getComponentUnderTest()}
 * @version $Id$
 * @since 4.3.1
 */
public class MockitoComponentMockingRule<T> extends MockitoComponentManagerRule
{
    /**
     * Used to discover and register components using annotations.
     */
    private ComponentAnnotationLoader loader = new ComponentAnnotationLoader();

    /**
     * Used to create Component Descriptors based on annotations.
     */
    private ComponentDescriptorFactory factory = new ComponentDescriptorFactory();

    /**
     * The mocked logger if any.
     */
    private Logger mockLogger;

    /**
     * The Role Type and Hint of the mocked component.
     */
    private RoleHint<T> mockedComponentHint;

    /**
     * The class of the component implementation to mock.
     */
    private Class<?> componentImplementationClass;

    /**
     * The list of component Roles that shouldn't be mocked.
     */
    private List<Class<?>> excludedComponentRoleDependencies = new ArrayList<Class<?>>();

    /**
     * The role Type if the component implementation implements several roles.
     */
    private Type componentRoleType;

    /**
     * The role Hint if the component implementation implements several roles.
     */
    private String componentRoleHint;

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass)
    {
        this.componentImplementationClass = componentImplementationClass;
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param excludedComponentRoleDependencies list of component dependency role classes that we don't want mocked
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass,
        List<? extends Class<?>> excludedComponentRoleDependencies)
    {
        this(componentImplementationClass);
        this.excludedComponentRoleDependencies.addAll(excludedComponentRoleDependencies);
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param componentRoleHint the role hint of the component implementation (when it has several), for disambiguation
     * @param excludedComponentRoleDependencies list of component dependency role classes that we don't want mocked
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass, Type componentRoleType,
        String componentRoleHint, List<? extends Class<?>> excludedComponentRoleDependencies)
    {
        this(componentImplementationClass, excludedComponentRoleDependencies);
        this.componentRoleType = componentRoleType;
        this.componentRoleHint = componentRoleHint;
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param excludedComponentRoleDependencies list of component dependency role classes that we don't want mocked
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass, Type componentRoleType,
        List<? extends Class<?>> excludedComponentRoleDependencies)
    {
        this(componentImplementationClass, componentRoleType, null, excludedComponentRoleDependencies);
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param componentRoleHint the role hint of the component implementation (when it has several), for disambiguation
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass, Type componentRoleType,
        String componentRoleHint)
    {
        this(componentImplementationClass);
        this.componentRoleType = componentRoleType;
        this.componentRoleHint = componentRoleHint;
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass, Type componentRoleType)
    {
        this(componentImplementationClass, componentRoleType, (String) null);
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                before(base, method, target);
                try {
                    base.evaluate();
                } finally {
                    after(base, method, target);
                }
            }
        };
    }

    /**
     * Called before the test.
     *
     * @param base The {@link Statement} to be modified
     * @param method The method to be run
     * @param target The object on with the method will be run.
     * @throws Throwable if anything goes wrong
     * @since 5.1M1
     */
    @Override
    protected void before(final Statement base, final FrameworkMethod method, final Object target) throws Throwable
    {
        super.before(base, method, target);

        mockComponent(target);
    }

    /**
     * Mock the injected components for the specified component implementation.
     *
     * @param testInstance the test instance
     * @throws Exception in case of an error while mocking
     */
    private void mockComponent(final Object testInstance) throws Exception
    {
        // Handle component fields
        for (ComponentDescriptor<T> descriptor : this.factory
            .createComponentDescriptors(this.componentImplementationClass, findComponentRoleType())) {
            // Only use the descriptor for the specified hint
            if ((this.componentRoleHint != null && this.componentRoleHint.equals(descriptor.getRoleHint()))
                || this.componentRoleHint == null) {
                registerMockDependencies(descriptor);
                registerComponent(descriptor);

                // Save the mocked component information so that the test can get an instance of this component
                // easily by calling getComponentUnderTest(...)
                this.mockedComponentHint = new RoleHint<T>(descriptor.getRoleType(), descriptor.getRoleHint());
                break;
            }
        }
    }

    /**
     * Overrides EmbeddableComponentManager in order to mock Loggers since they're handled specially and are not
     * components.
     *
     * @param instanceClass the injected class
     * @return the logger
     */
    @Override
    protected Object createLogger(Class<?> instanceClass)
    {
        Object logger;

        if (!this.excludedComponentRoleDependencies.contains(Logger.class)
            && this.componentImplementationClass == instanceClass) {
            logger = mock(Logger.class, instanceClass.getName());
            this.mockLogger = (Logger) logger;
        } else {
            logger = super.createLogger(instanceClass);
        }
        return logger;
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
                && !hasComponent(dependencyDescriptor.getRoleType(), dependencyDescriptor.getRoleHint())) {
                DefaultComponentDescriptor cd = new DefaultComponentDescriptor<>();

                cd.setRoleType(dependencyDescriptor.getRoleType());
                cd.setRoleHint(dependencyDescriptor.getRoleHint());

                Object dependencyMock = mock(roleTypeClass, dependencyDescriptor.getName());

                if (Provider.class == roleTypeClass) {
                    Type providedType = ReflectionUtils.getLastTypeGenericArgument(dependencyDescriptor.getRoleType());
                    Class providedClass = ReflectionUtils.getTypeClass(providedType);

                    // If the target is registered or if a list or a map are asked don't mock anything
                    if (hasComponent(providedType, dependencyDescriptor.getRoleHint())
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

                registerComponent(cd, dependencyMock);
            }
        }
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
     * @return the component which is having its injections being mocked by the {@link MockitoComponentMockingRule} rule
     * @throws ComponentLookupException if the component under test has not been properly registered
     */
    public T getComponentUnderTest() throws ComponentLookupException
    {
        return getInstance(this.mockedComponentHint.getRoleType(), this.mockedComponentHint.getHint());
    }

    /**
     * @return the mocked Logger if the Component under Test has requested an Injection of a Logger or null otherwise
     */
    public Logger getMockedLogger()
    {
        // Note: the test class must get the mocked component instance before calling this method since this is this
        // action that injects the mock loggers... Note that we cannot call the mocked component here since the
        // component can be per-lookup and we would get a different instance...
        return this.mockLogger;
    }
}
