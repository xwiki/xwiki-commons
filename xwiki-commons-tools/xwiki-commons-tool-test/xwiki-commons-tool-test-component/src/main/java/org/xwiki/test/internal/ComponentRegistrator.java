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
package org.xwiki.test.internal;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.annotation.ComponentDeclaration;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.internal.MemoryConfigurationSource;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.ComponentList;

import static org.xwiki.component.annotation.ComponentAnnotationLoader.COMPONENT_LIST;

/**
 * Helper methods to configure components for testing.
 *
 * @version $Id$
 * @since 4.3.1
 */
public class ComponentRegistrator
{
    /**
     * Used to register components from their annotations.
     */
    private ComponentAnnotationLoader loader = new TestComponentAnnotationLoader();

    /**
     * Registers a component (using the default role hint).
     *
     * @param roleType the type of the component role to register
     * @param instance the instance to register
     * @param componentManager the component manager against which to register the component
     * @throws Exception in case of an error during registration
     */
    public void registerComponent(Type roleType, Object instance, ComponentManager componentManager) throws Exception
    {
        registerComponent(roleType, null, instance, componentManager);
    }

    /**
     * Registers a component.
     *
     * @param roleType the type of the component role to register
     * @param roleHint the role hint of the component to register
     * @param instance the instance to register
     * @param componentManager the component manager against which to register the component
     * @throws Exception in case of an error during registration
     */
    public void registerComponent(Type roleType, String roleHint, Object instance, ComponentManager componentManager)
        throws Exception
    {
        DefaultComponentDescriptor descriptor = new DefaultComponentDescriptor();
        descriptor.setRoleType(roleType);
        if (roleHint != null) {
            descriptor.setRoleHint(roleHint);
        }
        componentManager.registerComponent(descriptor, instance);
    }

    /**
     * Register components associated to the provided class.
     *
     * @param componentImplementation the implementation of the component
     * @param componentManager the component manager against which to register the components
     * @throws Exception in case of an error during registration
     * @since 5.2M1
     */
    public void registerComponent(Class<?> componentImplementation, ComponentManager componentManager) throws Exception
    {
        List<ComponentDescriptor<?>> descriptors = this.loader.getComponentsDescriptors(componentImplementation);

        for (ComponentDescriptor descriptor : descriptors) {
            componentManager.registerComponent(descriptor);
        }
    }

    /**
     * Register components associated to the provided class if there is not already one.
     *
     * @param componentImplementation the implementation of the component
     * @param componentManager the component manager against which to register the components
     * @throws Exception in case of an error during registration
     * @since 7.2M2
     */
    public void registerComponentIfDontExist(Class<?> componentImplementation, ComponentManager componentManager)
        throws Exception
    {
        List<ComponentDescriptor<?>> descriptors = this.loader.getComponentsDescriptors(componentImplementation);

        for (ComponentDescriptor descriptor : descriptors) {
            if (!componentManager.hasComponent(descriptor.getRoleType(), descriptor.getRoleHint())) {
                componentManager.registerComponent(descriptor);
            }
        }
    }

    /**
     * If the user has specified the {@link org.xwiki.test.annotation.AllComponents} annotation then all components are
     * loaded; however this is not recommended since it slows down the execution time and makes the test less
     * controlled; we recommend instead to use the {@link org.xwiki.test.annotation.ComponentList} annotation which only
     * registers the component implementation you pass to it.
     *
     * @param testClass the class containing the annotations
     * @param componentManager the component manager against which to register the components
     * @throws IOException in case of an error loading the component list resource
     */
    public void registerComponents(Class<?> testClass, ComponentManager componentManager) throws IOException
    {
        AllComponents allComponentsAnnotation = testClass.getAnnotation(AllComponents.class);
        if (allComponentsAnnotation != null) {
            // Exclude component types that are specified by the user when using the AllComponents annotation.
            List<String> excludedComponentTypes = new ArrayList<>();
            for (Type excludedComponentType : allComponentsAnnotation.excludes()) {
                excludedComponentTypes.add(excludedComponentType.getTypeName());
            }
            List<ComponentDeclaration> componentDeclarations =
                this.loader.getDeclaredComponents(testClass.getClassLoader(), COMPONENT_LIST);
            componentDeclarations.removeIf(i -> excludedComponentTypes.contains(i.getImplementationClassName()));
            this.loader.initialize(componentManager, testClass.getClassLoader(), componentDeclarations);
        } else {
            // Find all Annotations that are annotated with ComponentList to get the list of Component Declarations
            List<ComponentDeclaration> componentDeclarations =
                getComponentDeclarationsFromAnnotation(testClass.getAnnotations(), new ArrayList<>());
            if (!componentDeclarations.isEmpty()) {
                this.loader.initialize(componentManager, testClass.getClassLoader(), componentDeclarations);
            }
        }
    }

    /**
     * @param annotations the list of annotations to check
     * @param alreadyProcessedAnnotations the list of already processed annotation to avoid circular dependencies
     * @return the list of Component Declarations declared by any passed annotation annotated by {@link ComponentList}
     */
    private List<ComponentDeclaration> getComponentDeclarationsFromAnnotation(Annotation[] annotations,
        List<Annotation> alreadyProcessedAnnotations)
    {
        Set<ComponentDeclaration> componentDeclarations = new LinkedHashSet<>();
        for (Annotation annotation : annotations) {
            // Is the Annotation (or one of the Annotation annotating the Annotation) tagged with @ComponentList?
            // If so, then call the "value()" method to get the list of Component Declarations
            if (annotation instanceof ComponentList) {
                ComponentList componentListAnnotation = (ComponentList) annotation;
                for (Class<?> componentClass : componentListAnnotation.value()) {
                    componentDeclarations.add(new ComponentDeclaration(componentClass.getName()));
                }
            } else {
                // Protection against Annotations that tag themselves with themselves like @Retention, @Documented, etc
                if (!alreadyProcessedAnnotations.contains(annotation)) {
                    alreadyProcessedAnnotations.add(annotation);
                    componentDeclarations.addAll(getComponentDeclarationsFromAnnotation(
                        annotation.annotationType().getAnnotations(), alreadyProcessedAnnotations));
                }
            }
        }

        return new ArrayList<>(componentDeclarations);
    }

    /**
     * Register in-memory data source for the default, "xwikiproperties" and "all" configuration sources.
     *
     * @param componentManager the component manager against which to register the configuration sources
     * @return the in-memory configuration source used for default, "xwikiproperties" and "all" component hints
     * @throws Exception in case the registration fails
     */
    public MemoryConfigurationSource registerMemoryConfigurationSource(ComponentManager componentManager)
        throws Exception
    {
        MockConfigurationSource configurationSource = new MockConfigurationSource();
        componentManager.registerComponent(MockConfigurationSource.getDescriptor(null), configurationSource);
        componentManager.registerComponent(MockConfigurationSource.getDescriptor("xwikiproperties"),
            configurationSource);
        componentManager.registerComponent(MockConfigurationSource.getDescriptor("all"), configurationSource);
        return configurationSource;
    }
}
