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
package org.xwiki.test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.annotation.MockingRequirement;

/**
 * Unit tests for Components should extend this class instead of the older
 * {@link org.xwiki.test.AbstractComponentTestCase} test class. To use this class, add a private field of the type of
 * the component class being tested and annotate it with {@link org.xwiki.test.annotation.MockingRequirement}. This test
 * case will then find all Requirements of the component class being tested and register mocks for each of them. To set
 * expectations simply look them up in setUp() (for example) and define their expectations in your test methods or
 * setUp().
 *
 * For example: <code><pre>
 * public class MyComponentTest
 * {
 *     &#64;MockingRequirement
 *     private MyComponent myComponent;
 * 
 *     private SomeRequirementComponentRoleClass requirement;
 * 
 *     &#64;Override
 *     &#64;Before
 *     public void setUp() throws Exception
 *     {
 *         super.setUp();
 *         requirement = getComponentManager().lookup(SomeRequirementComponentRoleClass.class);
 *     }
 *     ...
 * }
 * </code></pre>
 *
 * <p/>
 * Note that if your component under test is using other components in its {@code initialize()} method
 * you'll need to override the {@link #configure} method to add Mock expectations there.
 * </p>
 * Also note that by default there are no component registered against the component manager except those mocked
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
 */
public abstract class AbstractMockingComponentTestCase extends AbstractMockingTestCase
{
    private MockingComponentManager componentManager;

    private ComponentAnnotationLoader loader = new ComponentAnnotationLoader();

    private ComponentDescriptorFactory factory = new ComponentDescriptorFactory();

    private Map<Class, Logger> mockLoggers = new HashMap<Class, Logger>();

    /**
     * Extend EmbeddableComponentManager in order to mock Loggers since they're handled specially and are not
     * components.
     */
    private class LogSpecificMockingComponentManager extends MockingComponentManager
    {
        private List<Class< ? >> mockedComponentClasses = new ArrayList<Class< ? >>();

        public void addMockedComponentClass(Class< ? > mockedComponentClass)
        {
            this.mockedComponentClasses.add(mockedComponentClass);
        }

        @Override
        protected Object createLogger(Class< ? > instanceClass)
        {
            Object logger;

            if (this.mockedComponentClasses.contains(instanceClass)) {
                logger = getMockery().mock(Logger.class, instanceClass.getName());
                mockLoggers.put(instanceClass, (Logger) logger);
            } else {
                logger = super.createLogger(instanceClass);
            }
            return logger;
        }
    }

    /**
     * @return the Mock Logger if the Component under Test has requested an Injection of a Logger or null otherwise
     */
    public Logger getMockLogger(Class< ? > mockRequirementInstanceClass)
    {
        return this.mockLoggers.get(mockRequirementInstanceClass);
    }

    /**
     * @return the Mock Logger if the Component under Test has requested an Injection of a Logger or null otherwise
     */
    public Logger getMockLogger()
    {
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

        List<Object[]> components = new ArrayList<Object[]>();

        // Step 2: Register dependencies
        for (Field field : ReflectionUtils.getAllFields(getClass())) {
            MockingRequirement mockingRequirement = field.getAnnotation(MockingRequirement.class);
            if (mockingRequirement != null) {

                List<Class< ? >> exclusions = Arrays.asList(mockingRequirement.exceptions());

                // Mark the component class having its deps mocked so that our MockingEmbeddableComponentManager will
                // serve a mock Logger (but only if the Logger class is not in the exclusion list)
                if (!exclusions.contains(Logger.class)) {
                    ((LogSpecificMockingComponentManager) this.componentManager)
                        .addMockedComponentClass(field.getType());
                }

                // Handle component fields
                Type componentRoleType = findComponentRoleType(field, mockingRequirement.value());
                for (ComponentDescriptor descriptor : this.factory.createComponentDescriptors(field.getType(),
                    componentRoleType)) {
                    // Only use the descriptor for the specified hint
                    if ((mockingRequirement.hint().length() > 0 && mockingRequirement.hint().equals(
                        descriptor.getRoleHint()))
                        || mockingRequirement.hint().length() == 0) {
                        registerMockDependencies(descriptor, exclusions);
                        getComponentManager().registerComponent(descriptor);
                        configure();

                        components.add(new Object[] {field, descriptor});

                        break;
                    }
                }
            }
        }

        // Step 3: Let test setup dependencies
        setupDependencies();

        // Step 4: Inject all fields annotated with @MockingRequirement.
        for (Object[] component : components) {
            Field field = (Field) component[0];
            ComponentDescriptor< ? > descriptor = (ComponentDescriptor< ? >) component[1];

            ReflectionUtils.setFieldValue(this, field.getName(),
                getComponentManager().getInstance(descriptor.getRoleType(), descriptor.getRoleHint()));
        }
    }

    /**
     * If the user has specified the {@link org.xwiki.test.annotation.AllComponents} annotation then all components
     * are lodaded; however this is not recommended since it slows down the execution time; we recommend instead to
     * use the {@link ComponentList} annotation which only registers the component implementation you pass to it; or
     * even better don't use any annotation which means no component is registered explicitely which should be the
     * default since you're mocking all component dependencies with the {@link MockingRequirement} annotation.
     */
    private void registerComponents()
    {
        AllComponents allComponentsAnnotation = this.getClass().getAnnotation(AllComponents.class);
        if (allComponentsAnnotation != null) {
            this.loader.initialize(this.componentManager, getClass().getClassLoader());
        } else {
            ComponentList componentListAnnotation = this.getClass().getAnnotation(ComponentList.class);
            if (componentListAnnotation != null) {
                List<ComponentDeclaration> componentDeclarations = new ArrayList<ComponentDeclaration>();
                for (Class<?> componentClass : componentListAnnotation.value()) {
                    componentDeclarations.add(new ComponentDeclaration(componentClass.getName()));
                }
                this.loader.initialize(this.componentManager, getClass().getClassLoader(), componentDeclarations);
            }
        }
    }

    /**
     * Setup mock dependencies before initializing the @MockingRequirement components.
     */
    protected void setupDependencies() throws Exception
    {
    }

    /**
     * Provides a hook so that users of this class can perform configuration before the component is looked up. This
     * allows for example the ability to set expectations on mocked components used in Initializable.initialize()
     * methods.
     */
    public void configure() throws Exception
    {
        // Do nothing by default, this method is supposed to be overridden if needed.
    }

    private <T> void registerMockDependencies(ComponentDescriptor<T> descriptor, List<Class< ? >> exceptions)
        throws Exception
    {
        Collection<ComponentDependency< ? >> dependencyDescriptors = descriptor.getComponentDependencies();
        for (ComponentDependency< ? > dependencyDescriptor : dependencyDescriptors) {
            // Only register a mock if it isn't an exception
            // TODO: Handle multiple roles/hints.
            if (!exceptions.contains(ReflectionUtils.getTypeClass(dependencyDescriptor.getRoleType()))
                && Logger.class != ReflectionUtils.getTypeClass(dependencyDescriptor.getRoleType())) {
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

    private Type findComponentRoleType(Field field, Class< ? > role)
    {
        Type componentRoleType;

        Set<Type> componentRoleTypes = this.loader.findComponentRoleTypes(field.getType());
        if (!Object.class.getName().equals(role.getName())) {
            if (!componentRoleTypes.contains(role)) {
                throw new RuntimeException("Specified Component Role not found in component");
            } else {
                componentRoleType = role;
            }
        } else {
            if (componentRoleTypes.isEmpty()) {
                throw new RuntimeException("Couldn't find roles for component [" + field.getType() + "]");
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
