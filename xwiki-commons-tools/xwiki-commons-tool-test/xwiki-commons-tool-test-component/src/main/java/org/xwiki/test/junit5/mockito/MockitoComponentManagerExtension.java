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
package org.xwiki.test.junit5.mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;

import javax.inject.Named;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.MockitoAnnotations;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.xwiki.test.mockito.MockitoComponentMocker;

/**
 * JUnit5 extension to help write unit tests for XWiki Components.
 *
 * For example:
 *
 * <pre>
 * {@code
 * &#64;ComponentTest
 * &#64;ComponentList({
 *     Component3Impl.class
 * })
 * public class MyComponentTest
 * {
 *     &#64;Mock
 *     private List<String> list;
 *
 *     &#64;MockComponent
 *     private Component1Role component1;
 *
 *     &#64;InjectMocks
 *     &#64;InjectMockComponents
 *     private Component4Impl component4;
 *
 *     &#64;BeforeEach
 *     public void before(MockitoComponentManager componentManager)
 *     {
 *         ...
 *     }
 *
 *     &#64;Test
 *     public void test1(MockitoComponentManager componentManager)
 *     {
 *         ...
 *     }
 *
 *     &#64;Test
 *     public void test2(ComponentManager componentManager)
 *     {
 *         ...
 *     }
 * ...
 * }
 * }
 * </pre>
 *
 * @version $Id$
 * @since 10.3RC1
 */
public class MockitoComponentManagerExtension implements TestInstancePostProcessor, AfterAllCallback,
    ParameterResolver
{
    private static final Namespace NAMESPACE = Namespace.create(MockitoComponentManagerExtension.class);

    private static final ComponentAnnotationLoader LOADER = new ComponentAnnotationLoader();

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception
    {
        // We initialize the Mockito Component Manager if one of the following annotations are found:
        // - @AllComponents (class level)
        // - @ComponentList (class level)
        // - @MockComponent (field level)
        // - @InjectComponentMocks (field level)
        if (shouldInitializeComponentManager(testInstance)) {
            MockitoComponentManager mcm = loadComponentManager(context);
            if (mcm == null) {
                mcm = new MockitoComponentManager();
                mcm.initializeTest(testInstance);
                saveComponentManager(context, mcm);
            }

            // Register a mock component for all fields annotated with @MockComponent
            for (Field field : testInstance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(MockComponent.class)) {
                    // Get the hint from the @Named annotation (if any)
                    Named namedAnnotation = field.getAnnotation(Named.class);
                    Object mockComponent;
                    if (namedAnnotation != null) {
                        mockComponent = mcm.registerMockComponent(field.getType(), namedAnnotation.value());
                    } else {
                        mockComponent = mcm.registerMockComponent(field.getType());
                    }
                    ReflectionUtils.setFieldValue(testInstance, field.getName(), mockComponent);
                }
            }

            // Create & register a component instance of all fields annotated with @InjectMockComponents with all its
            // @Inject-annotated fields injected with mocks or real implementations.
            for (Field field : testInstance.getClass().getDeclaredFields()) {
                InjectMockComponents annotation = field.getAnnotation(InjectMockComponents.class);
                if (annotation != null) {
                    // Find Component descriptors
                    List<ComponentDescriptor> descriptors = LOADER.getComponentsDescriptors(field.getType());
                    ComponentDescriptor<?> descriptor = getDescriptor(annotation.role(), descriptors, field);
                    MockitoComponentMocker<?> mocker = new MockitoComponentMocker<>(mcm, field.getType(),
                        descriptor.getRoleType(), descriptor.getRoleHint());
                    mocker.mockComponent(testInstance);
                    Object component = mcm.getInstance(descriptor.getRoleType(), descriptor.getRoleHint());
                    ReflectionUtils.setFieldValue(testInstance, field.getName(), component);
                }
            }

        }

        MockitoAnnotations.initMocks(testInstance);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext)
    {
        MockitoComponentManager mcm = loadComponentManager(extensionContext);
        if (mcm != null) {
            mcm.dispose();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        Parameter parameter = parameterContext.getParameter();
        return ComponentManager.class.isAssignableFrom(parameter.getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return loadComponentManager(extensionContext);
    }

    private ComponentDescriptor<?> getDescriptor(Class<?> role, List<ComponentDescriptor> descriptors, Field field)
        throws Exception
    {
        if (role.equals(InjectMockComponents.class)) {
            if (descriptors.size() > 1) {
                // Force user to specify a role in case of several
                throw new Exception(String.format("The component under field [%s] is implementing several roles. "
                    + "Please disambiguate by using the \"role\" parameter of the @%s annotation.",
                    field.getName(), InjectMockComponents.class.getSimpleName()));
            } else {
                return descriptors.get(0);
            }
        } else {
            for (ComponentDescriptor<?> descriptor : descriptors) {
                Class<?> roleClass = ReflectionUtils.getTypeClass(descriptor.getRoleType());
                if (roleClass.equals(role)) {
                    return descriptor;
                }
            }
            throw new Exception(String.format("The role type specified in the @%s annotation for field [%s] isn't "
                + "implemented by the component.", field.getName(), InjectMockComponents.class.getSimpleName()));
        }
    }

    private boolean shouldInitializeComponentManager(Object testInstance)
    {
        boolean result = false;
        if (testInstance.getClass().getAnnotation(AllComponents.class) != null
            || testInstance.getClass().getAnnotation(ComponentList.class) != null)
        {
            result = true;
        } else {
            // If one field is annotated with @MockComponent or @InjectMockComponents then it's enough to require
            // initializing the Mockito Component Manager.
            for (Field field : testInstance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(MockComponent.class)
                    || field.isAnnotationPresent(InjectMockComponents.class))
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private MockitoComponentManager loadComponentManager(ExtensionContext context)
    {
        ExtensionContext.Store store = getStore(context);
        Class<?> testClass = context.getRequiredTestClass();
        return store.get(testClass, MockitoComponentManager.class);
    }

    private void saveComponentManager(ExtensionContext context,
        MockitoComponentManager componentManager)
    {
        ExtensionContext.Store store = getStore(context);
        Class<?> testClass = context.getRequiredTestClass();
        store.put(testClass, componentManager);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context)
    {
        return context.getRoot().getStore(NAMESPACE);
    }
}
