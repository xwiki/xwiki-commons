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
import org.xwiki.component.annotation.ComponentDescriptorFactory;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.annotation.MockingRequirement;

/**
 * Unit tests for Components should extend this class instead of the older
 * {@link org.xwiki.test.AbstractComponentTestCase} test class. To use this class, add a private field of the type of
 * the component class being tested and annotate it with {@link org.xwiki.test.annotation.MockingRequirement}. This test
 * case will then find all Requirements of the component class being tested and inject mocks for each of them. To set
 * expectations simply look them up in setUp() (for example) and define their expectations in your test methods or
 * setUp(). For example: <code><pre>
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
 * </code></pre> Note that if your component under test is using other components in its {@code initialize()} method
 * you'll need to override the {@link #configure} method to add Mock expectations there.
 * 
 * @version $Id$
 * @since 2.2M1
 */
public abstract class AbstractMockingComponentTestCase extends AbstractMockingTestCase
{
    private EmbeddableComponentManager componentManager;

    private ComponentAnnotationLoader loader = new ComponentAnnotationLoader();

    private ComponentDescriptorFactory factory = new ComponentDescriptorFactory();

    private Map<Class, Logger> mockLoggers = new HashMap<Class, Logger>();

    /**
     * Extend EmbeddableComponentManager in order to mock Loggers since they're handled specially and are not
     * components.
     */
    private class MockingEmbeddableComponentManager extends EmbeddableComponentManager
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
        this.componentManager = new MockingEmbeddableComponentManager();

        // Step 1: Register all components available
        // TODO: Remove this so that tests are executed faster. Need to offer a way to register components manually.
        this.loader.initialize(this.componentManager, getClass().getClassLoader());

        List<Object[]> components = new ArrayList<Object[]>();

        // Step 2: Register dependencies
        for (Field field : ReflectionUtils.getAllFields(getClass())) {
            MockingRequirement mockingRequirement = field.getAnnotation(MockingRequirement.class);
            if (mockingRequirement != null) {

                List<Class< ? >> exclusions = Arrays.asList(mockingRequirement.exceptions());

                // Mark the component class having its deps mocked so that our MockingEmbeddableComponentManager will
                // server a mock Logger (but only if the Logger class is not in the exclusion list)
                if (!exclusions.contains(Logger.class)) {
                    ((MockingEmbeddableComponentManager) this.componentManager)
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
    public EmbeddableComponentManager getComponentManager() throws Exception
    {
        return this.componentManager;
    }
}
