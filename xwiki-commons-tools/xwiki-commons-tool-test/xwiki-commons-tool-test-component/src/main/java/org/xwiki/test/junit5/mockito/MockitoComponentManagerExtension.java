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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstances;
import org.mockito.MockitoAnnotations;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.mockito.MockitoComponentManager;
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
 *     &#64;Inject
 *     private SomeComponentRole someComponent;
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
public class MockitoComponentManagerExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver
{
    private static final ComponentAnnotationLoader LOADER = new ComponentAnnotationLoader();

    private static final String COMPONENT_MANAGER = "componentManager";

    private static final String MOCKITO_AUTOCLOSEABLE = "mockitoAutoCloseable";

    @Override
    public void beforeEach(ExtensionContext context) throws Exception
    {
        Optional<TestInstances> testInstances = context.getTestInstances();
        if (testInstances.isPresent()) {
            // Initialize all test classes, including nested ones.
            for (Object testInstance : testInstances.get().getAllInstances()) {
                initializeTestInstance(testInstance, context);
            }
        }
    }

    private void initializeTestInstance(Object testInstance, ExtensionContext context) throws Exception
    {
        // Make sure tests don't leak one on another
        removeComponentManager(context);

        // We initialize the CM with the following steps:
        // - We create an empty instance of it
        // - We inject the component manager in the @InjectComponentManager annotated fields
        // - We create mocks for all @MockComponent annotations.
        // - We initialize the CM. This handles in the following order:
        //   - @BeforeComponent
        //   - @BeforeComponent("<testname>")
        //   - @ComponentList and @AllComponents
        //   - @AfterComponent
        //   - @AfterComponent("<testname>")
        // - We inject @InjectMockComponents fields
        // - We inject @Inject fields (this is just a shortcut to using @InjectComponentManager and then calling
        //   getInstance() on it)
        // - We inject Mockito annotations (@Mock, @InjectMocks, etc)
        //
        // Note: We handle @MockComponent before @InjectMockComponents to allow the test to have methods annotated with
        // @BeforeComponent which can configure mocks defined with @MockComponent annotations, so that when
        // @InjectMockComponents component are injected, if they implement Initializable, the test can have prepared
        // any component setup so that the call to initialize() will work fine.
        //
        // Note: We initialize the CM after handling @MockComponent so that it's possible use mocks injected with
        // @MockComponent inside @BeforeComponent and @AfterComponent methods.

        loadComponentManager(context);
        MockitoComponentManager mcm = new MockitoComponentManager();
        saveComponentManager(context, mcm);

        // Inject the Mockito Component Manager in all fields annotated with @InjectComponentManager
        for (Field field : ReflectionUtils.getAllFields(testInstance.getClass())) {
            if (field.isAnnotationPresent(InjectComponentManager.class)) {
                ReflectionUtils.setFieldValue(testInstance, field.getName(), mcm);
            }
        }

        // Register a mock component for all fields annotated with @MockComponent
        for (Field field : ReflectionUtils.getAllFields(testInstance.getClass())) {
            MockComponent mockComponentAnnotation = field.getAnnotation(MockComponent.class);
            if (mockComponentAnnotation != null) {
                // Get the hint from the @Named annotation (if any)
                Named namedAnnotation = field.getAnnotation(Named.class);
                Class<?> classToMock = mockComponentAnnotation.classToMock() != MockComponent.class
                    ? mockComponentAnnotation.classToMock() : null;
                Object mockComponent;
                if (namedAnnotation != null) {
                    mockComponent =
                        mcm.registerMockComponent(field.getGenericType(), namedAnnotation.value(), classToMock, true);
                } else {
                    mockComponent = mcm.registerMockComponent(field.getGenericType(), null, classToMock, true);
                }
                ReflectionUtils.setFieldValue(testInstance, field.getName(), mockComponent);
            }
        }

        initializeMockitoComponentManager(testInstance, mcm, context);

        // Create & register a component instance of all fields annotated with @InjectMockComponents with all its
        // @Inject-annotated fields injected with mocks or real implementations.
        processInjectMockComponents(testInstance, mcm);

        // Handle @Inject fields
        processInjectAnnotations(testInstance, mcm);

        // Make sure this is executed last since if we want to combine it with @InjectMockComponents annotation, we
        // need the field to be non-null when this line executes or otherwise Mockito will not inject anything...
        // Also note that all fields annotated with @InjectMocks will have their fields replaced by all mocks found
        // in the test class.
        saveMockitoAutoCloseable(context, MockitoAnnotations.openMocks(testInstance));
    }

    private void processInjectMockComponents(Object testInstance, MockitoComponentManager mcm) throws Exception
    {
        for (Field field : ReflectionUtils.getAllFields(testInstance.getClass())) {
            InjectMockComponents annotation = field.getAnnotation(InjectMockComponents.class);
            if (annotation != null) {
                processSingleInjectMockComponents(testInstance, field, annotation, mcm);
            }
        }
    }

    protected void processSingleInjectMockComponents(Object testInstance, Field field, InjectMockComponents annotation,
        MockitoComponentManager mcm) throws Exception
    {
        // Must not be an instance
        if (field.getType().isInterface()) {
            throw new Exception(String.format("The type of the field [%s] annotated with @%s cannot be an interface.",
                InjectMockComponents.class.getSimpleName(), field.getName()));
        }

        // Find Component descriptors
        List<ComponentDescriptor<?>> descriptors = LOADER.getComponentsDescriptors(field.getType());
        ComponentDescriptor<?> descriptor = getDescriptor(annotation.role(), descriptors, field);
        MockitoComponentMocker<?> mocker =
            new MockitoComponentMocker<>(mcm, field.getType(), descriptor.getRoleType(), descriptor.getRoleHint());
        mocker.mockComponent(testInstance);
        Object component = mcm.getInstance(descriptor.getRoleType(), descriptor.getRoleHint());
        ReflectionUtils.setFieldValue(testInstance, field.getName(), component);
    }

    private void processInjectAnnotations(Object testInstance, MockitoComponentManager mcm) throws Exception
    {
        for (Field field : ReflectionUtils.getAllFields(testInstance.getClass())) {
            Inject injectAnnotation = field.getAnnotation(Inject.class);
            if (injectAnnotation != null) {
                Named namedAnnotation = field.getAnnotation(Named.class);
                processSingleInjectAnnotations(testInstance, field, injectAnnotation, namedAnnotation, mcm);
            }
        }
    }

    private void processSingleInjectAnnotations(Object testInstance, Field field, Inject injectAnnotation,
        Named namedAnnotation, MockitoComponentManager mcm) throws Exception
    {
        // Must  be an instance
        if (!field.getType().isInterface()) {
            throw new Exception(String.format("The type of the field [%s] annotated with @%s must be an interface.",
                Inject.class.getSimpleName(), field.getName()));
        }

        Object component = (namedAnnotation == null) ? mcm.getInstance(field.getGenericType())
            : mcm.getInstance(field.getGenericType(), namedAnnotation.value());
        ReflectionUtils.setFieldValue(testInstance, field.getName(), component);
    }

    /**
     * To be overridden by extensions if they need to perform additional initializations.
     *
     * @param testInstance the test instance being initialized
     * @param mcm the already created (but not initialized) Mockito Component Manager
     * @param context the extension context
     * @throws Exception if the initialization fails
     */
    protected void initializeMockitoComponentManager(Object testInstance, MockitoComponentManager mcm,
        ExtensionContext context)
        throws Exception
    {
        Optional<Method> testMethod = context.getTestMethod();
        if (testMethod.isPresent()) {
            mcm.initializeTest(testInstance, testMethod.get(), mcm);
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception
    {
        MockitoComponentManager mcm = loadComponentManager(extensionContext);
        if (mcm != null) {
            mcm.dispose();
        }
        // Cleanup Mockito
        loadMockitoAutoCloseable(extensionContext).close();
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

    private ComponentDescriptor<?> getDescriptor(Class<?> role, List<ComponentDescriptor<?>> descriptors, Field field)
        throws Exception
    {
        // When the role is InjectMockComponents.class it means that no role has been set by the user, see the
        // InjectMockComponents javadoc.
        //
        // For disambiguation we support 2 ways:
        // - specify a role class in the @InjectMockComponents annotation
        // - specify a role string in the @Named annotation
        if (!isRolePresent(role) && !field.isAnnotationPresent(Named.class)) {
            if (descriptors.isEmpty()) {
                // Does not make sense to ask for the descriptor of a class which does not have any associated
                // descriptor.
                throw new Exception(
                    String.format("The component under field [%s] is not implementing any role.", field.getName()));
            } else if (descriptors.size() > 1) {
                // Note that we can have several descriptors if the component has one role but several hints. In this
                // case we can just take the first descriptor since it won't matter.
                if (areRolesIdentical(descriptors)) {
                    return descriptors.get(0);
                } else {
                    // Force user to specify a role in case of several
                    throw new Exception(String.format(
                        "The component under field [%s] is implementing several roles ([%s]). "
                            + "Please disambiguate by using the \"role\" parameter of the @%s annotation.",
                        field.getName(),
                        StringUtils.join(descriptors, ','),
                        InjectMockComponents.class.getSimpleName()));
                }
            } else {
                return descriptors.get(0);
            }
        } else {
            for (ComponentDescriptor<?> descriptor : descriptors) {
                if (isRolePresent(role)) {
                    Class<?> roleClass = ReflectionUtils.getTypeClass(descriptor.getRoleType());
                    if (roleClass.equals(role)) {
                        return descriptor;
                    }
                } else {
                    String roleHint = field.getAnnotation(Named.class).value();
                    if (descriptor.getRoleHint().equals(roleHint)) {
                        return descriptor;
                    }
                }
            }
            throw new Exception(String.format(
                "The role type specified in the @%s annotation for field [%s] isn't " + "implemented by the component.",
                field.getName(), InjectMockComponents.class.getSimpleName()));
        }
    }

    private boolean areRolesIdentical(List<ComponentDescriptor<?>> descriptors)
    {
        boolean areSame = true;
        Type type = null;
        for (ComponentDescriptor descriptor : descriptors) {
            if (type != null && !type.equals(descriptor.getRoleType())) {
                areSame = false;
                break;
            } else if (type == null) {
                type = descriptor.getRoleType();
            }
        }
        return areSame;
    }

    private boolean isRolePresent(Class<?> role)
    {
        return !role.equals(InjectMockComponents.class);
    }

    /**
     * @param context the test execution context, used to get the context store
     * @return the MockitoComponentManager found in the context store
     */
    public static MockitoComponentManager loadComponentManager(ExtensionContext context)
    {
        ExtensionContext.Store store = getStore(context);
        return store.get(COMPONENT_MANAGER, MockitoComponentManager.class);
    }

    private void removeComponentManager(ExtensionContext context)
    {
        ExtensionContext.Store store = getStore(context);
        store.remove(COMPONENT_MANAGER);
    }

    private void saveComponentManager(ExtensionContext context, MockitoComponentManager componentManager)
    {
        ExtensionContext.Store store = getStore(context);
        store.put(COMPONENT_MANAGER, componentManager);
    }

    private AutoCloseable loadMockitoAutoCloseable(ExtensionContext context)
    {
        ExtensionContext.Store store = getStore(context);
        return store.get(MOCKITO_AUTOCLOSEABLE, AutoCloseable.class);
    }

    private void saveMockitoAutoCloseable(ExtensionContext context, AutoCloseable mockitoAutoCloseable)
    {
        ExtensionContext.Store store = getStore(context);
        store.put(MOCKITO_AUTOCLOSEABLE, mockitoAutoCloseable);
    }

    /**
     * @param context the test execution context, used to get the context store
     * @return an extension context store for the currently running test
     */
    public static ExtensionContext.Store getStore(ExtensionContext context)
    {
        return context.getStore(Namespace.create(MockitoComponentManagerExtension.class,
            context.getRequiredTestMethod()));
    }
}
