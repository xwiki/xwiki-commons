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
import org.xwiki.test.mockito.MockitoComponentManager;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.xwiki.test.mockito.MockitoComponentMocker;

/**
 * JUnit5 extension to help write unit tests for XWiki Components.
 * <p>
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
 *     &#64;InjectComponentManager
 *     private MockitoComponentManager componentManager;
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
public class MockitoComponentManagerExtension implements TestInstancePostProcessor, AfterAllCallback, ParameterResolver
{
    private static final Namespace NAMESPACE = Namespace.create(MockitoComponentManagerExtension.class);

    private static final ComponentAnnotationLoader LOADER = new ComponentAnnotationLoader();

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception
    {
        // Make sure tests don't leak one on another
        removeComponentManager(context);

        // We initialize the CM in 3 steps:
        // - First we create an instance of it
        // - Then we create mocks for all @MockComponent annotations
        // - Then we initialize the CM.
        // This allows the test to have methods annotated with @BeforeComponent which can configure mocks defined
        // with @MockComponent annotations, so that when @InjectMockComponents component are injected, if they
        // implement Initializable, the test can have prepared any component setup so that the call to initialize()
        // will work fine.
        MockitoComponentManager mcm = loadComponentManager(context);
        boolean initializeCM = mcm == null;
        if (initializeCM) {
            mcm = new MockitoComponentManager();
            saveComponentManager(context, mcm);
        }

        // Register a mock component for all fields annotated with @MockComponent
        for (Field field : ReflectionUtils.getAllFields(testInstance.getClass())) {
            if (field.isAnnotationPresent(MockComponent.class)) {
                // Get the hint from the @Named annotation (if any)
                Named namedAnnotation = field.getAnnotation(Named.class);
                Object mockComponent;
                if (namedAnnotation != null) {
                    mockComponent = mcm.registerMockComponent(field.getGenericType(), namedAnnotation.value());
                } else {
                    mockComponent = mcm.registerMockComponent(field.getGenericType());
                }
                ReflectionUtils.setFieldValue(testInstance, field.getName(), mockComponent);
            }
        }

        if (initializeCM) {
            initializeMockitoComponentManager(testInstance, mcm, context);
        }

        // Create & register a component instance of all fields annotated with @InjectMockComponents with all its
        // @Inject-annotated fields injected with mocks or real implementations.
        for (Field field : ReflectionUtils.getAllFields(testInstance.getClass())) {
            InjectMockComponents annotation = field.getAnnotation(InjectMockComponents.class);
            if (annotation != null) {
                processInjectMockComponents(testInstance, field, annotation, mcm);
            }
        }

        // Inject the Mockito Component Manager in all fields annotated with @InjectComponentManager
        for (Field field : ReflectionUtils.getAllFields(testInstance.getClass())) {
            if (field.isAnnotationPresent(InjectComponentManager.class)) {
                ReflectionUtils.setFieldValue(testInstance, field.getName(), mcm);
            }
        }

        // Make sure this is executed last since if we want to combine it with @InjectMockComponents annotation, we
        // need the field to be non-null when this line executes or otherwise Mockito will not inject anything...
        // Also note that all fields annotated with @InjectMocks will have their fields replaced by all mocks found
        // in the test class.
        MockitoAnnotations.initMocks(testInstance);
    }

    protected void processInjectMockComponents(Object testInstance, Field field, InjectMockComponents annotation,
        MockitoComponentManager mcm) throws Exception
    {
        // Must not be an instance
        if (field.getType().isInterface()) {
            throw new Exception(String.format("The type of the field [%s] annotated with @%s cannot be an interface.",
                InjectMockComponents.class.getSimpleName(), field.getName()));
        }

        // Find Component descriptors
        List<ComponentDescriptor> descriptors = LOADER.getComponentsDescriptors(field.getType());
        ComponentDescriptor<?> descriptor = getDescriptor(annotation.role(), descriptors, field);
        MockitoComponentMocker<?> mocker =
            new MockitoComponentMocker<>(mcm, field.getType(), descriptor.getRoleType(), descriptor.getRoleHint());
        mocker.mockComponent(testInstance);
        Object component = mcm.getInstance(descriptor.getRoleType(), descriptor.getRoleHint());
        ReflectionUtils.setFieldValue(testInstance, field.getName(), component);
    }

    /**
     * To be overridden by extensions if they need to perform additional initializations.
     *
     * @param testInstance the test instance object
     * @param mcm the already created (but not initialized) Mockito Component Manager
     * @param context the extension context
     * @throws Exception if the intialization fails
     */
    protected void initializeMockitoComponentManager(Object testInstance, MockitoComponentManager mcm,
        ExtensionContext context) throws Exception
    {
        mcm.initializeTest(testInstance, mcm);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception
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
                throw new Exception(String.format(
                    "The component under field [%s] is implementing several roles. "
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
            throw new Exception(String.format(
                "The role type specified in the @%s annotation for field [%s] isn't " + "implemented by the component.",
                field.getName(), InjectMockComponents.class.getSimpleName()));
        }
    }

    protected MockitoComponentManager loadComponentManager(ExtensionContext context)
    {
        ExtensionContext.Store store = getStore(context);
        Class<?> testClass = context.getRequiredTestClass();
        return store.get(testClass, MockitoComponentManager.class);
    }

    private void removeComponentManager(ExtensionContext context)
    {
        ExtensionContext.Store store = getStore(context);
        Class<?> testClass = context.getRequiredTestClass();
        store.remove(testClass);
    }

    private void saveComponentManager(ExtensionContext context, MockitoComponentManager componentManager)
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
