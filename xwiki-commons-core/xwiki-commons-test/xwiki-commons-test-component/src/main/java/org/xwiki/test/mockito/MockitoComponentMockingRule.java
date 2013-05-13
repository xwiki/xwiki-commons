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

import static org.mockito.Mockito.mock;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.annotation.ComponentDescriptorFactory;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.internal.ComponentRegistrator;

/**
 * Unit tests for Components should extend this class instead of using {@link MockitoComponentManagerRule} or
 * {@link org.xwiki.test.ComponentManagerRule} which should only be used for integration tests. To use this class,
 * define a JUnit {@code @Rule} and pass the component implementation class that you wish to have mocked for you. Then
 * in your test code, do a lookup of your component under test and you'll get a component instance which has all its
 * injected dependencies mocked automatically. For example: <code><pre>
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
 *     ...
 * }
 * </pre></code> Note that by default there are no component registered against the component manager except those
 * mocked automatically by the Rule. This has 2 advantages:
 * <ul>
 * <li>This is the spirit of this Rule since it's for unit testing and this testing your component in isolation from the
 * rest</li>
 * <li>It makes the tests up to 10 times faster</li>
 * </ul>
 * If you really need to register some components, use the {@link org.xwiki.test.annotation.ComponentList} annotation
 * and if you really really need to register all components (it takes time) then use
 * {@link org.xwiki.test.annotation.AllComponents}.
 * 
 * @param <T> the component role type, used to provide a typed instance when calling {@link #getComponentUnderTest()}
 * @version $Id$
 * @since 4.3.1
 */
public class MockitoComponentMockingRule<T> extends EmbeddableComponentManager implements MethodRule
{
    /**
     * Used to register components.
     */
    private ComponentRegistrator componentRegistrator = new ComponentRegistrator();

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
    private Class< ? > componentImplementationClass;

    /**
     * The list of component Roles that shouldn't be mocked.
     */
    private List<Class< ? >> excludedComponentImplementationDependencies = new ArrayList<Class< ? >>();

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
    public MockitoComponentMockingRule(Class< ? extends T> componentImplementationClass)
    {
        this.componentImplementationClass = componentImplementationClass;
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param excludedComponentImplementationDependencies list of component dependencies class that we don't want mocked
     */
    public MockitoComponentMockingRule(Class< ? extends T> componentImplementationClass,
        List< ? extends Class< ? >> excludedComponentImplementationDependencies)
    {
        this(componentImplementationClass);
        this.excludedComponentImplementationDependencies.addAll(excludedComponentImplementationDependencies);
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param componentRoleHint the role hint of the component implementation (when it has several), for disambiguation
     * @param excludedComponentImplementationDependencies list of component dependencies class that we don't want mocked
     */
    public MockitoComponentMockingRule(Class< ? extends T> componentImplementationClass, Type componentRoleType,
        String componentRoleHint, List< ? extends Class< ? >> excludedComponentImplementationDependencies)
    {
        this(componentImplementationClass, excludedComponentImplementationDependencies);
        this.componentRoleType = componentRoleType;
        this.componentRoleHint = componentRoleHint;
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param excludedComponentImplementationDependencies list of component dependencies class that we don't want mocked
     */
    public MockitoComponentMockingRule(Class< ? extends T> componentImplementationClass, Type componentRoleType,
        List< ? extends Class< ? >> excludedComponentImplementationDependencies)
    {
        this(componentImplementationClass, componentRoleType, null, excludedComponentImplementationDependencies);
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param componentRoleHint the role hint of the component implementation (when it has several), for disambiguation
     */
    public MockitoComponentMockingRule(Class< ? extends T> componentImplementationClass, Type componentRoleType,
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
    public MockitoComponentMockingRule(Class< ? extends T> componentImplementationClass, Type componentRoleType)
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
     * @return a new statement, which may be the same as {@code base}, a wrapper around {@code base}, or a completely
     *         new Statement.
     * @throws Throwable if anything goes wrong
     * @since 5.1M1
     */
    protected void before(final Statement base, final FrameworkMethod method, final Object target) throws Throwable
    {
        mockComponent(target);
    }

    /**
     * Called before the test.
     * 
     * @param base The {@link Statement} to be modified
     * @param method The method to be run
     * @param target The object on with the method will be run.
     * @return a new statement, which may be the same as {@code base}, a wrapper around {@code base}, or a completely
     *         new Statement.
     * @throws Throwable if anything goes wrong
     * @since 5.1M1
     */
    protected void after(final Statement base, final FrameworkMethod method, final Object target) throws Throwable
    {
        // Make sure to dispose all Disposable components in case they have resources/static to free
        dispose();
    }

    /**
     * Mock the injected components for the specified component implementation.
     * 
     * @param testInstance the test instance
     * @throws Exception in case of an error while mocking
     */
    private void mockComponent(final Object testInstance) throws Exception
    {
        // Step 1: Register the components that are needed by the tests.
        this.componentRegistrator.registerComponents(testInstance.getClass(), this);

        // Step 2: Get the injected components and register mocks of them

        // Handle component fields
        for (ComponentDescriptor<T> descriptor : this.factory.createComponentDescriptors(
            this.componentImplementationClass, findComponentRoleType())) {
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
    protected Object createLogger(Class< ? > instanceClass)
    {
        Object logger;

        if (!this.excludedComponentImplementationDependencies.contains(Logger.class)
            && this.componentImplementationClass == instanceClass) {
            logger = mock(Logger.class, instanceClass.getName());
            mockLogger = (Logger) logger;
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
        Collection<ComponentDependency< ? >> dependencyDescriptors = descriptor.getComponentDependencies();
        for (ComponentDependency< ? > dependencyDescriptor : dependencyDescriptors) {
            Class< ? > roleTypeClass = ReflectionUtils.getTypeClass(dependencyDescriptor.getRoleType());
            // Only register a mock if it isn't:
            // - An explicit exception specified by the user
            // - A logger
            // - A collection of components, we want to keep them as Java collections. Those collections are later
            // filled by the component manager with available components. Developers can register mocked components
            // in an override of #setupDependencies().
            // TODO: Handle multiple roles/hints.
            if (!this.excludedComponentImplementationDependencies.contains(roleTypeClass)
                && Logger.class != roleTypeClass && !roleTypeClass.isAssignableFrom(List.class)
                && !roleTypeClass.isAssignableFrom(Map.class)) {
                DefaultComponentDescriptor cd = new DefaultComponentDescriptor();
                cd.setRoleType(dependencyDescriptor.getRoleType());
                cd.setRoleHint(dependencyDescriptor.getRoleHint());
                registerComponent(
                    cd,
                    mock(ReflectionUtils.getTypeClass(dependencyDescriptor.getRoleType()),
                        dependencyDescriptor.getName()));
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
                throw new RuntimeException("Specified Component Role not found in component");
            } else {
                type = this.componentRoleType;
            }
        } else {
            if (componentRoleTypes.isEmpty()) {
                throw new RuntimeException(String.format("Couldn't find roles for component [%s]",
                    this.componentRoleType));
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
