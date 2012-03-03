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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

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
 * {@link org.xwiki.test.AbstractComponentTestCase} test class.
 *
 * To use this class, add a private field of the type of the component class being tested and annotate it with
 * {@link org.xwiki.test.annotation.MockingRequirement}. This test case will then find all Requirements of the
 * component class being tested and inject mocks for each of them. To set expectations simply look them up in
 * setUp() (for example) and define their expectations in your test methods or setUp().
 *
 * For example:
 * <code><pre>
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
 * @version $Id$
 * @since 2.2M1
 */
public abstract class AbstractMockingComponentTestCase extends AbstractMockingTestCase
{
    private EmbeddableComponentManager componentManager;

    private ComponentAnnotationLoader loader = new ComponentAnnotationLoader();

    private ComponentDescriptorFactory factory = new ComponentDescriptorFactory();

    @Before
    public void setUp() throws Exception
    {
        this.componentManager = new EmbeddableComponentManager();

        // Step 1: Register all components available
        // TODO: Remove this so that tests are executed faster. Need to offer a way to register components manually.
        this.loader.initialize(this.componentManager, getClass().getClassLoader());

        // Step 2: Inject all fields annotated with @MockingRequirement.

        for (Field field : ReflectionUtils.getAllFields(getClass())) {
            MockingRequirement mockingRequirement = field.getAnnotation(MockingRequirement.class);
            if (mockingRequirement != null) {

                // Handle component fields
                Class< ? > componentRoleClass = findComponentRoleClass(field, mockingRequirement.value());
                for (ComponentDescriptor descriptor :
                    this.factory.createComponentDescriptors(field.getType(), componentRoleClass))
                {
                    // Only use the descriptor for the specified hint
                    if ((mockingRequirement.hint().length() > 0 && mockingRequirement.hint().equals(
                        descriptor.getRoleHint())) || mockingRequirement.hint().length() == 0)
                    {
                        registerMockDependencies(descriptor, Arrays.asList(mockingRequirement.exceptions()));
                        getComponentManager().registerComponent(descriptor);
                        configure();
                        ReflectionUtils.setFieldValue(this, field.getName(),
                            getComponentManager().lookup(descriptor.getRole(), descriptor.getRoleHint()));
                        break;
                    }
                }

                // Logging is a special case since the Component Manager will inject a Logger but a Logger is not
                // a component. The call to registerMockDependencies() above will have registered a Mock Logger
                // in the component manager. Thus get it and override the Logger field injected by the Component
                // manager.
                Collection<Field> mockedComponentfields = ReflectionUtils.getAllFields(field.getType());
                for (Field mockedField : mockedComponentfields) {
                    if (mockedField.getAnnotation(Inject.class) != null &&
                        Logger.class.isAssignableFrom(mockedField.getType()) &&
                        getComponentManager().hasComponent(Logger.class))
                    {
                        boolean isAccessible = field.isAccessible();
                        Object object;
                        try {
                            field.setAccessible(true);
                            object = field.get(this);
                        } finally {
                            field.setAccessible(isAccessible);
                        }
                        ReflectionUtils.setFieldValue(object, mockedField.getName(),
                            getComponentManager().lookup(Logger.class));
                    }
                }
            }
        }
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

    private <T> void registerMockDependencies(ComponentDescriptor<T> descriptor,  List<Class< ? >> exceptions)
        throws Exception
    {
        Collection<ComponentDependency< ? >> dependencyDescriptors = descriptor.getComponentDependencies();
        for (ComponentDependency< ? > dependencyDescriptor : dependencyDescriptors) {
            // Only register a mock if it isn't an exception
            // TODO: Handle multiple roles/hints.
            if (!exceptions.contains(dependencyDescriptor.getRole())) {
                DefaultComponentDescriptor cd = new DefaultComponentDescriptor();
                cd.setRole(dependencyDescriptor.getRole());
                cd.setRoleHint(dependencyDescriptor.getRoleHint());
                this.componentManager.registerComponent(
                    cd, getMockery().mock(dependencyDescriptor.getRole(), dependencyDescriptor.getName()));
            }
        }
    }

    private Class< ? > findComponentRoleClass(Field field, Class< ? > role)
    {
        Class< ? > componentRoleClass;

        Set<Class< ? >> componentRoleClasses = this.loader.findComponentRoleClasses(field.getType());
        if (!Object.class.getName().equals(role.getName())) {
            if (!componentRoleClasses.contains(role)) {
                throw new RuntimeException("Specified Component Role not found in component");
            } else {
                componentRoleClass = role;
            }
        } else {
            if (componentRoleClasses.isEmpty()) {
                throw new RuntimeException("Couldn't find roles for component [" + field.getType() + "]");
            } else if (componentRoleClasses.size() > 1) {
                throw new RuntimeException(
                    "Components with several roles must explicitely specify which role to use.");
            } else {
                componentRoleClass = componentRoleClasses.iterator().next();
            }
        }
        return componentRoleClass;
    }

    /**
     * @return a configured Component Manager
     */
    public EmbeddableComponentManager getComponentManager() throws Exception
    {
        return this.componentManager;
    }
}
