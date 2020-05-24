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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.internal.StackingComponentEventManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.configuration.internal.MemoryConfigurationSource;
import org.xwiki.observation.ObservationManager;
import org.xwiki.test.annotation.AfterComponent;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.internal.ComponentRegistrator;

/**
 * Initialize a Component Manager and uses the {@link org.xwiki.test.annotation.AllComponents} and
 * {@link org.xwiki.test.annotation.ComponentList} annotations to decide what components to discover and register.
 * <p>
 * Also offers helper APIs to register components and in-memory configuration sources.
 *
 * @version $Id$
 * @since 5.1RC1
 */
public class TestComponentManager extends EmbeddableComponentManager
{
    /**
     * Used to register components.
     */
    protected ComponentRegistrator componentRegistrator = new ComponentRegistrator();

    /**
     * Register in-memory data source for the default and "xwikiproperties" configuration sources.
     *
     * @return the in-memory configuration source used for both default and "xwikiproperties" component hints
     * @throws Exception in case the registration fails
     */
    public MemoryConfigurationSource registerMemoryConfigurationSource() throws Exception
    {
        return this.componentRegistrator.registerMemoryConfigurationSource(this);
    }

    /**
     * Registers a component.
     *
     * @param roleType the type of the component role to register
     * @param roleHint the role hint of the component to register
     * @param instance the instance to register
     * @throws Exception in case of an error during registration
     */
    public void registerComponent(Type roleType, String roleHint, Object instance) throws Exception
    {
        this.componentRegistrator.registerComponent(roleType, roleHint, instance, this);
    }

    /**
     * Registers a component (with a default role hint).
     *
     * @param roleType the type of the component role to register
     * @param instance the instance to register
     * @throws Exception in case of an error during registration
     */
    public void registerComponent(Type roleType, Object instance) throws Exception
    {
        this.componentRegistrator.registerComponent(roleType, instance, this);
    }

    /**
     * Register components associated to the provided class.
     *
     * @param testClass the class containing the annotations
     * @throws Exception in case of an error during registration
     * @since 5.2M1
     */
    public void registerComponent(Class<?> testClass) throws Exception
    {
        this.componentRegistrator.registerComponent(testClass, this);
    }

    /**
     * Register components associated to the provided class if there is not already one.
     *
     * @param testClass the class containing the annotations
     * @throws Exception in case of an error during registration
     * @since 7.2M2
     */
    public void registerComponentIfDontExist(Class<?> testClass) throws Exception
    {
        this.componentRegistrator.registerComponent(testClass, this);
    }

    /**
     * Initialize the test component manager by registering components based on the presence of
     * {@link org.xwiki.test.annotation.AllComponents} and {@link org.xwiki.test.annotation.ComponentList} annotations.
     * Also calls methods annotated with {@link BeforeComponent} and {@link AfterComponent}.
     *
     * @param testClassInstance the test instance on which the annotations are present
     * @param test the test method being executed
     * @param parameterInstances the instances that will be passed as parameters to methods annotated with
     *        {@code @BeforeComponent} and {@code @AfterComponent}
     * @throws Exception if an error happens during initialization
     */
    public void initializeTest(Object testClassInstance, Method test, Object... parameterInstances) throws Exception
    {
        Class<?> testClass = testClassInstance.getClass();

        // If there are methods annotated with the BeforeComponent annotation then call them. This gives an
        // opportunity for the test to register some components *before* we register the other components below.
        for (Method method : getBeforeComponentAnnotatedMethods(testClass)) {
            String target = method.getAnnotation(BeforeComponent.class).value();
            invokeMethod(test, target, method, testClassInstance, parameterInstances);
        }

        this.componentRegistrator.registerComponents(testClass, this);

        // If there are methods annotated with the AfterComponent annotation then call them. This gives an
        // opportunity to override or modify some components *after* they are actually used.
        for (Method method : getAfterComponentAnnotatedMethods(testClass)) {
            String target = method.getAnnotation(AfterComponent.class).value();
            invokeMethod(test, target, method, testClassInstance, parameterInstances);
        }
    }

    private List<Method> getBeforeComponentAnnotatedMethods(Class<?> testClass)
    {
        LinkedList<Method> methods = new LinkedList<>();
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(BeforeComponent.class)) {
                String target = method.getAnnotation(BeforeComponent.class).value();
                // Add to the top if the method applies globally to all test and at the bottom if not so that we
                // execute them in the right order! Indeed, test-specific BeforeComponent methods may require fixture
                // defined in the global ones.
                if (StringUtils.isEmpty(target)) {
                    methods.addFirst(method);
                } else {
                    methods.addLast(method);
                }
            }
        }
        return methods;
    }

    private List<Method> getAfterComponentAnnotatedMethods(Class<?> testClass)
    {
        LinkedList<Method> methods = new LinkedList<>();
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AfterComponent.class)) {
                String target = method.getAnnotation(AfterComponent.class).value();
                // Add to the top if the method applies globally to all test and at the bottom if not so that we
                // execute them in the right order! Indeed, test-specific BeforeComponent methods may require fixture
                // defined in the global ones.
                if (StringUtils.isEmpty(target)) {
                    methods.addFirst(method);
                } else {
                    methods.addLast(method);
                }
            }
        }
        return methods;
    }

    private void invokeMethod(Method test, String target, Method declaredMethod, Object testClassInstance,
        Object... parameterInstances) throws Exception
    {
        if (test == null || StringUtils.isEmpty(target) || target.equalsIgnoreCase(test.getName())) {
            invokeMethod(declaredMethod, testClassInstance, parameterInstances);
        }
    }

    /**
     * Initialize the test component manager by registering components based on the presence of
     * {@link org.xwiki.test.annotation.AllComponents} and {@link org.xwiki.test.annotation.ComponentList} annotations.
     * Also calls methods annotated with {@link BeforeComponent} and {@link AfterComponent}.
     *
     * @param testClassInstance the test instance on which the annotations are present
     * @param parameterInstances the instances that will be passed as parameters to methods annotated with
     *        {@code @BeforeComponent} and {@code @AfterComponent}
     * @throws Exception if an error happens during initialization
     */
    public void initializeTest(Object testClassInstance, Object... parameterInstances) throws Exception
    {
        initializeTest(testClassInstance, null, parameterInstances);
    }

    private void invokeMethod(Method declaredMethod, Object testClassInstance, Object... parameterInstances)
        throws Exception
    {
        // If parameters are of a type found in parameterInstances, then call the method.
        List<Object> validatedParameterInstances = new ArrayList<>();
        boolean isSupported = true;
        for (Parameter parameter : declaredMethod.getParameters()) {
            // Is there a matching parameter instance for the parameter?
            boolean hasMatchingParameterInstance = false;
            for (Object object : parameterInstances) {
                if (object.getClass().isAssignableFrom(parameter.getType())) {
                    hasMatchingParameterInstance = true;
                    validatedParameterInstances.add(object);
                    break;
                }
            }
            if (!hasMatchingParameterInstance) {
                isSupported = false;
                break;
            }
        }

        if (isSupported) {
            boolean isAccessible = declaredMethod.isAccessible();
            try {
                // Allow calling methods located in package-private classes.
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(testClassInstance, validatedParameterInstances.toArray());
            } finally {
                declaredMethod.setAccessible(isAccessible);
            }
        }
    }

    /**
     * Enabled notification of component descriptor registration/unregistration.
     * 
     * @throws ComponentLookupException when failing to lookup {@link ObservationManager} component
     */
    public void notifyComponentDescriptorEvent() throws ComponentLookupException
    {
        // Set component event manager if available
        if (hasComponent(ObservationManager.class)) {
            StackingComponentEventManager eventManager = new StackingComponentEventManager();
            eventManager.setObservationManager(this.<ObservationManager>getInstance(ObservationManager.class));
            eventManager.shouldStack(false);

            setComponentEventManager(eventManager);
        }
    }

    /**
     * Cleans up the test component manager by disposing components registered in it.
     *
     * @throws Exception if an error happens during clean up
     */
    public void shutdownTest() throws Exception
    {
        // Make sure to dispose all Disposable components in case they have resources/static to free
        dispose();
    }
}
