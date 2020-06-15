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
package org.xwiki.component.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Constructs a Component Descriptor out of a class definition that contains Annotations.
 *
 * @version $Id$
 * @since 1.8.1
 * @see ComponentAnnotationLoader
 */
public class ComponentDescriptorFactory
{
    /**
     * Load all Component Descriptor Factories implementations using the JDK's Service Loader facility. Note that we
     * cannot use Components to do this since it would be a chicken and egg issue since this factory class is used to
     * initialize Components...
     */
    private ServiceLoader<ComponentDependencyFactory> componentDependencyFactories = ServiceLoader
        .load(ComponentDependencyFactory.class);

    /**
     * Create component descriptors for the passed component implementation class and component role class. There can be
     * more than one descriptor if the component class has specified several hints.
     *
     * @param componentClass the component implementation class
     * @param componentRoleClass the component role class
     * @return the component descriptors with resolved component dependencies
     * @param <T> the described class type
     * @deprecated since 4.0M1 use {@link #createComponentDescriptors(Class, Type)} instead
     */
    @Deprecated
    public <T> List<ComponentDescriptor<T>> createComponentDescriptors(Class<? extends T> componentClass,
        Class<?> componentRoleClass)
    {
        return createComponentDescriptors(componentClass, (Type) componentRoleClass);
    }

    /**
     * Create component descriptors for the passed component implementation class and component role class. There can be
     * more than one descriptor if the component class has specified several hints.
     *
     * @param componentClass the component implementation class
     * @param componentRoleType the component role type
     * @param <T> the described class type
     * @return the component descriptors with resolved component dependencies
     * @since 4.0M1
     */
    public <T> List<ComponentDescriptor<T>> createComponentDescriptors(Class<? extends T> componentClass,
        Type componentRoleType)
    {
        List<ComponentDescriptor<T>> descriptors = new ArrayList<>();

        // If there's a @Named annotation, use it and ignore hints specified in the @Component annotation.
        String[] hints;
        Named named = componentClass.getAnnotation(Named.class);
        if (named != null) {
            hints = new String[] {named.value()};
        } else {
            // If the Component annotation has several hints specified ignore the default hint value and for each
            // specified hint create a Component Descriptor
            Component component = componentClass.getAnnotation(Component.class);
            if (component != null && component.hints().length > 0) {
                hints = component.hints();
            } else {
                if (component != null && component.value().trim().length() > 0) {
                    hints = new String[] {component.value().trim()};
                } else {
                    hints = new String[] {"default"};
                }
            }
        }

        // Create the descriptors
        for (String hint : hints) {
            descriptors.add(createComponentDescriptor(componentClass, hint, componentRoleType));
        }

        return descriptors;
    }

    /**
     * Create a component descriptor for the passed component implementation class, hint and component role class.
     *
     * @param componentClass the component implementation class
     * @param hint the hint
     * @param componentRoleType the component role type
     * @return the component descriptor with resolved component dependencies
     */
    private <T> ComponentDescriptor<T> createComponentDescriptor(Class<? extends T> componentClass, String hint,
        Type componentRoleType)
    {
        DefaultComponentDescriptor<T> descriptor = new DefaultComponentDescriptor<>();
        descriptor.setRoleType(componentRoleType);
        descriptor.setImplementation(componentClass);
        descriptor.setRoleHint(hint);
        descriptor.setInstantiationStrategy(createComponentInstantiationStrategy(componentClass));

        // Set the injected fields.
        // Note: that we need to find all fields since we can have some inherited fields which are annotated in a
        // superclass. Since Java doesn't offer a method to return all fields we have to traverse all parent classes
        // looking for declared fields.
        for (Field field : ReflectionUtils.getAllFields(componentClass)) {
            ComponentDependency<?> dependency = createComponentDependency(field);
            if (dependency != null) {
                descriptor.addComponentDependency(dependency);
            }
        }

        return descriptor;
    }

    /**
     * @param componentClass the component class from which to extract the component instantiation strategy
     * @return the component instantiation strategy to use
     */
    private ComponentInstantiationStrategy createComponentInstantiationStrategy(Class<?> componentClass)
    {
        ComponentInstantiationStrategy strategy;

        // Support both InstantiationStrategy and JSR 330's Singleton annotations.
        Singleton singleton = componentClass.getAnnotation(Singleton.class);
        if (singleton != null) {
            strategy = ComponentInstantiationStrategy.SINGLETON;
        } else {
            InstantiationStrategy instantiationStrategy = componentClass.getAnnotation(InstantiationStrategy.class);
            if (instantiationStrategy != null) {
                strategy = instantiationStrategy.value();
            } else {
                // TODO: In order to be JSR330 compliant we need to change this behavior and consider components are
                // per lookup when no annotation is specified. Before we can do this we need to modify the full xwiki
                // code base and possibly introduce a configuration option. To be discussed.
                strategy = ComponentInstantiationStrategy.SINGLETON;
            }
        }

        return strategy;
    }

    /**
     * @param field the field for which to extract a Component Dependency
     * @return the Component Dependency instance created from the passed field
     */
    private ComponentDependency<?> createComponentDependency(Field field)
    {
        ComponentDependency<?> dependency = null;

        // Try each factory till one returns a non null result
        for (ComponentDependencyFactory factory : this.componentDependencyFactories) {
            dependency = factory.createComponentDependency(field);
            if (dependency != null) {
                break;
            }
        }

        return dependency;
    }
}
