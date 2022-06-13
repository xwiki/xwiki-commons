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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.slf4j.Logger;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.annotation.ComponentDeclaration;
import org.xwiki.component.annotation.ComponentDescriptorFactory;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.jmock.annotation.MockingRequirement;
import org.xwiki.test.jmock.annotation.MockingRequirements;

/**
 * To use this class, annotate your test class with {@link org.xwiki.test.jmock.annotation.MockingRequirement},
 * passing the implementation class you're testing.
 * Then in your test code, do a lookup of your component under test and you'll get a component instance which has all
 * its injected dependencies mocked automatically. For example:
 * <pre>{@code
 * &#64;MockingRequirement(MyComponentImplementation.class)
 * public class MyComponentTest extends AbstractMockingComponentTestCase<MyComponent>
 * {
 *     &#64;Test
 *     public void someTest() throws Exception
 *     {
 *         MyComponent myComponent = getMockedComponent();
 *         ...
 *     }
 * ...
 * }
 * }</pre>
 *
 * Note that by default there are no component registered against the component manager except those mocked
 * automatically by the {@code @MockingRequirement} annotation. This has 2 advantages:
 * <ul>
 *   <li>This is the spirit of AbstractMockingComponentTestCase since they're supposed to mock all dependencies and
 *       define their behaviors</li>
 *   <li>It makes the tests up to 10 times faster</li>
 * </ul>
 * If you really need to register some components, use the {@link ComponentList} annotation and if you really really
 * need to register all components (it takes time) then use {@link AllComponents}.
 *
 * @version $Id$
 * @since 2.2M1
 * @deprecated use {@link org.xwiki.test.junit5.mockito.ComponentTest} instead
 */
@Deprecated(since = "4.3.1")
public abstract class AbstractMockingComponentTestCase<T> extends AbstractMockingTestCase
{
    private MockingComponentManager componentManager;

    private ComponentAnnotationLoader loader = new ComponentAnnotationLoader();

    private ComponentDescriptorFactory factory = new ComponentDescriptorFactory();

    private Map<Class, Logger> mockLoggers = new HashMap<>();

    private Map<Class, RoleHint<T>> mockedComponents = new HashMap<>();

    /**
     * Extend EmbeddableComponentManager in order to mock Loggers since they're handled specially and are not
     * components.
     */
    private class LogSpecificMockingComponentManager extends MockingComponentManager
    {
        private List<Class<?>> mockedComponentClasses = new ArrayList<>();

        public void addMockedComponentClass(Class<?> mockedComponentClass)
        {
            this.mockedComponentClasses.add(mockedComponentClass);
        }

        @Override
        protected Object createLogger(Class<?> instanceClass)
        {
            Object logger;

            if (this.mockedComponentClasses.contains(instanceClass)) {
                logger = getMockery().mock(Logger.class, instanceClass.getName());
                AbstractMockingComponentTestCase.this.mockLoggers.put(instanceClass, (Logger) logger);
            } else {
                logger = super.createLogger(instanceClass);
            }
            return logger;
        }
    }

    /**
     * @return the first component mocked by a {@link MockingRequirement} annotation
     * @since 4.2M3
     */
    public T getMockedComponent() throws ComponentLookupException
    {
        if (this.mockedComponents.size() == 0) {
            throw new RuntimeException("You need to have at least one @MockingRequirement annotation!");
        }
        if (this.mockedComponents.size() > 1) {
            throw new RuntimeException("When there are several @MockingRequirement annotations you muse use the "
                + "getMockedComponent(mockedComponentClass) signature!");
        } else {
            return getMockedComponent(this.mockedComponents.keySet().iterator().next());
        }
    }

    /**
     * @param mockedComponentClass the class of the mocked component to return
     * @return the component mocked by a {@link MockingRequirement} annotation that is of the passed class type
     * @since 4.2M3
     */
    public T getMockedComponent(Class mockedComponentClass) throws ComponentLookupException
    {
        RoleHint<T> roleHint = this.mockedComponents.get(mockedComponentClass);
        return this.componentManager.getInstance(roleHint.getRoleType(), roleHint.getHint());
    }

    /**
     * @return the Mock Logger if the Component under Test has requested an Injection of a Logger or null otherwise
     */
    public Logger getMockLogger(Class<?> mockedComponentClass) throws Exception
    {
        // Note: the test class must get the mocked component instance before calling this method since this is this
        // action that injects the mock loggers... Note that we cannot call the mocked component here since the
        // component can be per-lookup and we would get a different instance...
        return this.mockLoggers.get(mockedComponentClass);
    }

    /**
     * @return the Mock Logger if the Component under Test has requested an Injection of a Logger or null otherwise
     */
    public Logger getMockLogger() throws Exception
    {
        // Note: the test class must get the mocked component instance before calling this method since this is this
        // action that injects the mock loggers... Note that we cannot call the mocked component here since the
        // component can be per-lookup and we would get a different instance...

        if (this.mockLoggers.size() == 1) {
            return this.mockLoggers.values().iterator().next();
        } else if (this.mockLoggers.size() == 0) {
            throw new RuntimeException("You have excluded the Logger from being mocked in your @MockingRequirement!");
        } else {
            throw new RuntimeException("When there are several @MockingRequirement annotations you muse use the "
                + "getMockLogger(mockRequirementInstanceClass) signature!");
        }
    }

    @Before
    public void setUp() throws Exception
    {
        this.componentManager = new LogSpecificMockingComponentManager();

        // Step 1: Register the components that are needed by the tests.
        registerComponents();

        // Step 2: Create MockRequirement instances and register them as components
        for (MockingRequirement mockingRequirement : getMockingRequirements()) {
            List<Class<?>> exclusions = Arrays.asList(mockingRequirement.exceptions());

            // Mark the component class having its deps mocked so that our MockingEmbeddableComponentManager will
            // serve a mock Logger (but only if the Logger class is not in the exclusion list)
            if (!exclusions.contains(Logger.class)) {
                ((LogSpecificMockingComponentManager) this.componentManager)
                    .addMockedComponentClass(mockingRequirement.value());
            }

            // Handle component fields
            Type componentRoleType = findComponentRoleType(mockingRequirement);
            for (ComponentDescriptor descriptor : this.factory.createComponentDescriptors(mockingRequirement.value(),
                componentRoleType))
            {
                // Only use the descriptor for the specified hint
                if ((mockingRequirement.hint().length() > 0 && mockingRequirement.hint().equals(
                    descriptor.getRoleHint())) || mockingRequirement.hint().length() == 0)
                {
                    registerMockDependencies(descriptor, exclusions);
                    getComponentManager().registerComponent(descriptor);

                    // Save the mocked component information so that the test can get an instance of this component
                    // easily by calling getMockedComponent(...)
                    this.mockedComponents.put(mockingRequirement.value(),
                        new RoleHint<>(descriptor.getRoleType(), descriptor.getRoleHint()));
                    break;
                }
            }
        }
    }

    private List<MockingRequirement> getMockingRequirements()
    {
        MockingRequirements mockingRequirementsAnnotation = getClass().getAnnotation(MockingRequirements.class);
        List<MockingRequirement> mockingRequirements;
        if (mockingRequirementsAnnotation != null) {
            mockingRequirements = Arrays.asList(mockingRequirementsAnnotation.value());
        } else {
            MockingRequirement mockingRequirementAnnotation = getClass().getAnnotation(MockingRequirement.class);
            if (mockingRequirementAnnotation != null) {
                mockingRequirements = Collections.singletonList(mockingRequirementAnnotation);
            } else {
                throw new RuntimeException("When extending " + AbstractMockingComponentTestCase.class.getSimpleName()
                    + " you must have at least one @" + MockingRequirement.class.getSimpleName() + ".");
            }
        }
        return mockingRequirements;
    }

    /**
     * If the user has specified the {@link org.xwiki.test.annotation.AllComponents} annotation then all components
     * are lodaded; however this is not recommended since it slows down the execution time and makes the test less
     * controlled; we recommend instead to use the {@link ComponentList} annotation which only registers the component
     * implementation you pass to it; or even better don't use any annotation which means no component is registered
     * explicitly which should be the default since you're mocking all component dependencies with the
     * {@link MockingRequirement} annotation.
     */
    private void registerComponents()
    {
        AllComponents allComponentsAnnotation = this.getClass().getAnnotation(AllComponents.class);
        if (allComponentsAnnotation != null) {
            this.loader.initialize(this.componentManager, getClass().getClassLoader());
        } else {
            ComponentList componentListAnnotation = this.getClass().getAnnotation(ComponentList.class);
            if (componentListAnnotation != null) {
                List<ComponentDeclaration> componentDeclarations = new ArrayList<>();
                for (Class<?> componentClass : componentListAnnotation.value()) {
                    componentDeclarations.add(new ComponentDeclaration(componentClass.getName()));
                }
                this.loader.initialize(this.componentManager, getClass().getClassLoader(), componentDeclarations);
            }
        }
    }

    private <T> void registerMockDependencies(ComponentDescriptor<T> descriptor, List<Class<?>> exceptions)
        throws Exception
    {
        Collection<ComponentDependency<?>> dependencyDescriptors = descriptor.getComponentDependencies();
        for (ComponentDependency<?> dependencyDescriptor : dependencyDescriptors) {
            Class<?> roleTypeClass = ReflectionUtils.getTypeClass(dependencyDescriptor.getRoleType());
            // Only register a mock if it isn't:
            // - An explicit exception specified by the user
            // - A logger
            // - A collection of components, we want to keep them  as Java collections. Those collections are later
            //   filled by the component manager with available components. Developers can register mocked components
            //   in an override of #setupDependencies().
            // TODO: Handle multiple roles/hints.
            if (!exceptions.contains(roleTypeClass) && Logger.class != roleTypeClass
                && !roleTypeClass.isAssignableFrom(List.class) && !roleTypeClass.isAssignableFrom(Map.class))
            {
                DefaultComponentDescriptor cd = new DefaultComponentDescriptor();
                cd.setRoleType(dependencyDescriptor.getRoleType());
                cd.setRoleHint(dependencyDescriptor.getRoleHint());
                this.componentManager.registerComponent(
                    cd,
                    getMockery().mock(ReflectionUtils.getTypeClass(dependencyDescriptor.getRoleType()),
                        dependencyDescriptor.getName()));
            }
        }
    }

    private Type findComponentRoleType(MockingRequirement mockingRequirement)
    {
        Type componentRoleType;

        Set<Type> componentRoleTypes = this.loader.findComponentRoleTypes(mockingRequirement.value());
        Class<?> role = mockingRequirement.role();
        if (!Object.class.getName().equals(role.getName())) {
            if (!componentRoleTypes.contains(role)) {
                throw new RuntimeException("Specified Component Role not found in component");
            } else {
                componentRoleType = role;
            }
        } else {
            if (componentRoleTypes.isEmpty()) {
                throw new RuntimeException(
                    String.format("Couldn't find roles for component [%s]", mockingRequirement.value()));
            } else if (componentRoleTypes.size() > 1) {
                throw new RuntimeException("Components with several roles must explicitly specify which role to use.");
            } else {
                componentRoleType = componentRoleTypes.iterator().next();
            }
        }
        return componentRoleType;
    }

    /**
     * @return a configured Component Manager
     */
    @Override
    public MockingComponentManager getComponentManager() throws Exception
    {
        return this.componentManager;
    }
}
