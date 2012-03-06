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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Before;
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

    private class MockingEmbeddableComponentManager extends EmbeddableComponentManager
    {
        private Logger mockLogger;

        @Override
        protected <T> T getComponentInstance(RoleHint<T> roleHint) throws ComponentLookupException
        {
            T component;

            // Logging is a special case since the Component Manager will inject a Logger but a Logger is not
            // a component. Thus we need to intercept it and return a mock instead.
            if (Logger.class == roleHint.getRoleType()) {
                if (this.mockLogger != null) {
                    component = (T) this.mockLogger;
                } else {
                    this.mockLogger = getMockery().mock(Logger.class);
                    component = (T) this.mockLogger ;
                }
            } else {
                component = super.getComponentInstance(roleHint);
            }

            return component;
        }
    }

    @Before
    public void setUp() throws Exception
    {
        this.componentManager = new MockingEmbeddableComponentManager();

        // Step 1: Register all components available
        // TODO: Remove this so that tests are executed faster. Need to offer a way to register components manually.
        this.loader.initialize(this.componentManager, getClass().getClassLoader());

        // Step 2: Inject all fields annotated with @MockingRequirement.
        for (Field field : ReflectionUtils.getAllFields(getClass())) {
            MockingRequirement mockingRequirement = field.getAnnotation(MockingRequirement.class);
            if (mockingRequirement != null) {

                // Handle component fields
                Type componentRoleType = findComponentRoleType(field, mockingRequirement.value());
                for (ComponentDescriptor descriptor :
                    this.factory.createComponentDescriptors(field.getType(), componentRoleType))
                {
                    // Only use the descriptor for the specified hint
                    if ((mockingRequirement.hint().length() > 0 && mockingRequirement.hint().equals(
                        descriptor.getRoleHint())) || mockingRequirement.hint().length() == 0)
                    {
                        registerMockDependencies(descriptor, Arrays.asList(mockingRequirement.exceptions()));
                        getComponentManager().registerComponent(descriptor);
                        configure();
                        ReflectionUtils.setFieldValue(this, field.getName(),
                            getComponentManager().lookupComponent(descriptor.getRoleType(), descriptor.getRoleHint()));
                        break;
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
            if (!exceptions.contains(dependencyDescriptor.getRoleType())
                && Logger.class != dependencyDescriptor.getRoleType())
            {
                DefaultComponentDescriptor cd = new DefaultComponentDescriptor();
                cd.setRoleType(dependencyDescriptor.getRoleType());
                cd.setRoleHint(dependencyDescriptor.getRoleHint());
                this.componentManager.registerComponent(cd, getMockery().mock(
                    ReflectionUtils.getTypeClass(dependencyDescriptor.getRoleType()), dependencyDescriptor.getName()));
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
                throw new RuntimeException(
                    "Components with several roles must explicitly specify which role to use.");
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
