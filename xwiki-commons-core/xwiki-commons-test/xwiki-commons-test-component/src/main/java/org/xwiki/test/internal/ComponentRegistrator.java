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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.annotation.ComponentDeclaration;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.internal.MemoryConfigurationSource;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.ComponentList;

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
    private ComponentAnnotationLoader loader = new ComponentAnnotationLoader();

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
     * If the user has specified the {@link org.xwiki.test.annotation.AllComponents} annotation then all components
     * are loaded; however this is not recommended since it slows down the execution time and makes the test less
     * controlled; we recommend instead to use the {@link org.xwiki.test.annotation.ComponentList} annotation which
     * only registers the component implementation you pass to it.
     *
     * @param testClass the class containing the annotations
     * @param componentManager the component manager against which to register the components
     */
    public void registerComponents(Class<?> testClass, ComponentManager componentManager)
    {
        AllComponents allComponentsAnnotation = testClass.getAnnotation(AllComponents.class);
        if (allComponentsAnnotation != null) {
            this.loader.initialize(componentManager, testClass.getClassLoader());
        } else {
            ComponentList componentListAnnotation = testClass.getAnnotation(ComponentList.class);
            if (componentListAnnotation != null) {
                List<ComponentDeclaration> componentDeclarations = new ArrayList<ComponentDeclaration>();
                for (Class<?> componentClass : componentListAnnotation.value()) {
                    componentDeclarations.add(new ComponentDeclaration(componentClass.getName()));
                }
                this.loader.initialize(componentManager, testClass.getClassLoader(), componentDeclarations);
            }
        }
    }

    /**
     * Register in-memory data source for the default and "xwikiproperties" configuration sources.
     *
     * @param componentManager the component manager against which to register the configuration sources
     * @return the in-memory configuration source used for both default and "xwikiproperties" component hints
     * @throws Exception in case the registration fails
     */
    public MemoryConfigurationSource registerMemoryConfigurationSource(ComponentManager componentManager)
        throws Exception
    {
        MockConfigurationSource configurationSource = new MockConfigurationSource();
        componentManager.registerComponent(MockConfigurationSource.getDescriptor(null), configurationSource);
        componentManager.registerComponent(MockConfigurationSource.getDescriptor("xwikiproperties"),
            configurationSource);
        return configurationSource;
    }
}
