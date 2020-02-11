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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.stability.Unstable;

/**
 * Dynamically loads all components defined using Annotations and declared in META-INF/components.txt files.
 *
 * @version $Id$
 * @since 1.8.1
 */
public class ComponentAnnotationLoader
{
    /**
     * Location in the classloader of the file defining the list of component implementation class to parser for
     * annotations.
     */
    public static final String COMPONENT_LIST = "META-INF/components.txt";

    /**
     * Location in the classloader of the file specifying which component implementation to use when several components
     * with the same role/hint are found.
     *
     * @deprecated starting with 3.3M1 use the notion of priorities instead (see {@link ComponentDeclaration}).
     */
    @Deprecated
    public static final String COMPONENT_OVERRIDE_LIST = "META-INF/component-overrides.txt";

    /**
     * The encoding used to parse component list files.
     */
    private static final String COMPONENT_LIST_ENCODING = "UTF-8";

    /**
     * Logger to use for logging...
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentAnnotationLoader.class);

    /**
     * Factory to create a Component Descriptor from an annotated class.
     */
    private ComponentDescriptorFactory factory = new ComponentDescriptorFactory();

    /**
     * Loads all components defined using annotations.
     *
     * @param manager the component manager to use to dynamically register components
     * @param classLoader the classloader to use to look for the Component list declaration file (
     *            {@code META-INF/components.txt})
     */
    public void initialize(ComponentManager manager, ClassLoader classLoader)
    {
        try {
            // Find all declared components by retrieving the list defined in COMPONENT_LIST.
            List<ComponentDeclaration> componentDeclarations = getDeclaredComponents(classLoader, COMPONENT_LIST);

            // Find all the Component overrides and adds them to the bottom of the list as component declarations with
            // the highest priority of 0. This is purely for backward compatibility since the override files is now
            // deprecated.
            List<ComponentDeclaration> componentOverrideDeclarations =
                getDeclaredComponents(classLoader, COMPONENT_OVERRIDE_LIST);
            for (ComponentDeclaration componentOverrideDeclaration : componentOverrideDeclarations) {
                // Since the old way to declare an override was to define it in both a component.txt and a
                // component-overrides.txt file we first need to remove the override component declaration stored in
                // componentDeclarations.
                componentDeclarations.remove(componentOverrideDeclaration);
                // Add it to the end of the list with the highest priority.
                componentDeclarations.add(new ComponentDeclaration(componentOverrideDeclaration
                    .getImplementationClassName(), 0));
            }

            initialize(manager, classLoader, componentDeclarations);
        } catch (Exception e) {
            // Make sure we make the calling code fail in order to fail fast and prevent the application to start
            // if something is amiss.
            throw new RuntimeException("Failed to get the list of components to load", e);
        }
    }

    /**
     * @param manager the component manager to use to dynamically register components
     * @param classLoader the classloader to use to look for the Component list declaration file (
     *            {@code META-INF/components.txt})
     * @param componentDeclarations the declarations of components to register
     * @since 3.3M1
     */
    public void initialize(ComponentManager manager, ClassLoader classLoader,
        List<ComponentDeclaration> componentDeclarations)
    {
        register(manager, classLoader, componentDeclarations);
    }

    /**
     * Find all component descriptors out of component declarations.
     *
     * @param classLoader the classloader used to load the component declaration implementation class.
     * @param componentDeclarations the list of component declarations for which you want to build the component
     * descriptor.
     * @return a collection of component descriptors corresponding to the given component declarations.
     * @since 11.1
     */
    private Collection<ComponentDescriptor<?>> getComponentsDescriptors(ClassLoader classLoader,
        List<ComponentDeclaration> componentDeclarations)
    {
        // For each component class name found, load its class and use introspection to find the necessary
        // annotations required to create a Component Descriptor.
        Map<RoleHint<?>, ComponentDescriptor<?>> descriptorMap = new HashMap<>();
        Map<RoleHint<?>, Integer> priorityMap = new HashMap<>();

        for (ComponentDeclaration componentDeclaration : componentDeclarations) {
            Class<?> componentClass;
            try {
                componentClass = classLoader.loadClass(componentDeclaration.getImplementationClassName());
            } catch (Exception e) {
                throw new RuntimeException(
                    String.format("Failed to load component class [%s] for annotation parsing",
                        componentDeclaration.getImplementationClassName()), e);
            }

            // Look for ComponentRole annotations and register one component per ComponentRole found
            for (Type componentRoleType : findComponentRoleTypes(componentClass)) {
                for (ComponentDescriptor<?> componentDescriptor : this.factory.createComponentDescriptors(
                    componentClass, componentRoleType)) {
                    // If there's already a existing role/hint in the list of descriptors then decide which one
                    // to keep by looking at their priorities. Highest priority wins (i.e. lowest integer value).
                    RoleHint<?> roleHint =
                        new RoleHint(componentDescriptor.getRoleType(), componentDescriptor.getRoleHint());

                    addComponent(descriptorMap, priorityMap, roleHint, componentDescriptor, componentDeclaration,
                        true);
                }
            }
        }

        return descriptorMap.values();
    }

    /**
     * @param manager the component manager to use to dynamically register components
     * @param componentDescriptors the descriptors of components to register
     * @since 11.1
     */
    @Unstable
    public void register(ComponentManager manager, Collection<ComponentDescriptor<?>> componentDescriptors)
    {
        try {
            // Activate all component descriptors
            for (ComponentDescriptor<?> descriptor : componentDescriptors) {
                manager.registerComponent(descriptor);
            }
        } catch (Exception e) {
            // Make sure we make the calling code fail in order to fail fast and prevent the application to start
            // if something is amiss.
            throw new RuntimeException("Failed to dynamically load components with annotations", e);
        }
    }

    /**
     * @param manager the component manager to use to dynamically register components
     * @param classLoader the classloader to use to look for the Component list declaration file (
     *            {@code META-INF/components.txt})
     * @param componentDeclarations the declarations of components to register
     * @since 4.0M1
     */
    public void register(ComponentManager manager, ClassLoader classLoader,
        List<ComponentDeclaration> componentDeclarations)
    {

        register(manager, getComponentsDescriptors(classLoader, componentDeclarations));
    }

    private void addComponent(Map<RoleHint<?>, ComponentDescriptor<?>> descriptorMap,
        Map<RoleHint<?>, Integer> priorityMap, RoleHint<?> roleHint, ComponentDescriptor<?> componentDescriptor,
        ComponentDeclaration componentDeclaration, boolean warn)
    {
        if (descriptorMap.containsKey(roleHint)) {
            // Compare priorities
            int currentPriority = priorityMap.get(roleHint);
            if (componentDeclaration.getPriority() < currentPriority) {
                // Override!
                descriptorMap.put(roleHint, componentDescriptor);
                priorityMap.put(roleHint, componentDeclaration.getPriority());
            } else if (componentDeclaration.getPriority() == currentPriority) {
                if (warn) {
                    // Warning that we're not overwriting since they have the same priorities
                    getLogger().warn(
                        "Component [{}] which implements [{}] tried to overwrite component "
                            + "[{}]. However, no action was taken since both components have the same priority "
                            + "level of [{}].",
                        componentDeclaration.getImplementationClassName(), roleHint,
                        descriptorMap.get(roleHint).getImplementation().getName(), currentPriority);
                }
            } else {
                getLogger().debug(
                    "Ignored component [{}] since its priority level of [{}] is lower "
                        + "than the currently registered component [{}] which has a priority of [{}]",
                    componentDeclaration.getImplementationClassName(), componentDeclaration.getPriority(),
                    descriptorMap.get(roleHint).getImplementation().getName(), currentPriority);
            }
        } else {
            descriptorMap.put(roleHint, componentDescriptor);
            priorityMap.put(roleHint, componentDeclaration.getPriority());
        }
    }

    /**
     * @param manager the component manager to use to dynamically register components
     * @param classLoader the classloader to use to look for the Component list declaration file (
     *            {@code META-INF/components.txt})
     * @param componentDeclarations the declarations of components to register
     * @since 4.0M1
     */
    public void unregister(ComponentManager manager, ClassLoader classLoader,
        List<ComponentDeclaration> componentDeclarations)
    {
        for (ComponentDeclaration componentDeclaration : componentDeclarations) {
            Class<?> componentClass = null;
            try {
                componentClass = classLoader.loadClass(componentDeclaration.getImplementationClassName());
            } catch (ClassNotFoundException e) {
                getLogger().warn("Can't find any existing component with class [{}]. Ignoring it.",
                    componentDeclaration.getImplementationClassName());
            } catch (Exception e) {
                getLogger().warn("Fail to load component implementation class [{}]. Ignoring it.",
                    componentDeclaration.getImplementationClassName(), e);
            }

            if (componentClass != null) {
                for (ComponentDescriptor<?> componentDescriptor : getComponentsDescriptors(componentClass)) {
                    manager.unregisterComponent(componentDescriptor);

                    if (componentDescriptor.getRoleType() instanceof ParameterizedType) {
                        Class roleClass = ReflectionUtils.getTypeClass(componentDescriptor.getRoleType());

                        DefaultComponentDescriptor<?> classComponentDescriptor =
                            new DefaultComponentDescriptor(componentDescriptor);
                        classComponentDescriptor.setRoleType(roleClass);

                        manager.unregisterComponent(classComponentDescriptor);
                    }
                }
            }
        }
    }

    public List<ComponentDescriptor> getComponentsDescriptors(Class<?> componentClass)
    {
        List<ComponentDescriptor> descriptors = new ArrayList<>();

        // Look for ComponentRole annotations and register one component per ComponentRole found
        for (Type componentRoleType : findComponentRoleTypes(componentClass)) {
            descriptors.addAll(this.factory.createComponentDescriptors(componentClass, componentRoleType));
        }

        return descriptors;
    }

    public Set<Type> findComponentRoleTypes(Class<?> componentClass)
    {
        return findComponentRoleTypes(componentClass, null);
    }

    public Set<Type> findComponentRoleTypes(Class<?> componentClass, Type[] parameters)
    {
        // Note: We use a Set to ensure that we don't register duplicate roles.
        Set<Type> types = new LinkedHashSet<>();

        Component component = componentClass.getAnnotation(Component.class);

        // If the roles are specified by the user then don't auto-discover roles!
        if (component != null && component.roles().length > 0) {
            types.addAll(Arrays.asList(component.roles()));
        } else {
            // Auto-discover roles by looking for a @Role annotation or a @Provider one in both the superclass
            // and implemented interfaces.
            for (Type interfaceType : getGenericInterfaces(componentClass)) {
                Class<?> interfaceClass;
                Type[] interfaceParameters;

                if (interfaceType instanceof ParameterizedType) {
                    ParameterizedType interfaceParameterizedType = (ParameterizedType) interfaceType;

                    interfaceClass = ReflectionUtils.getTypeClass(interfaceType);
                    Type[] variableParameters = interfaceParameterizedType.getActualTypeArguments();

                    interfaceParameters =
                        ReflectionUtils.resolveSuperArguments(variableParameters, componentClass, parameters);

                    if (interfaceParameters == null) {
                        interfaceType = interfaceClass;
                    } else if (interfaceParameters != variableParameters) {
                        interfaceType =
                            new DefaultParameterizedType(interfaceParameterizedType.getOwnerType(), interfaceClass,
                                interfaceParameters);
                    }
                } else if (interfaceType instanceof Class) {
                    interfaceClass = (Class<?>) interfaceType;
                    interfaceParameters = null;
                } else {
                    continue;
                }

                // Handle superclass of interfaces
                types.addAll(findComponentRoleTypes(interfaceClass, interfaceParameters));

                // Handle interfaces directly declared in the passed component class
                if (ReflectionUtils.getDirectAnnotation(Role.class, interfaceClass) != null) {
                    types.add(interfaceType);
                }

                // Handle javax.inject.Provider
                if (Provider.class.isAssignableFrom(interfaceClass)) {
                    types.add(interfaceType);
                }

                // Handle ComponentRole (retro-compatibility since 4.0M1)
                if (ReflectionUtils.getDirectAnnotation(ComponentRole.class, interfaceClass) != null) {
                    types.add(interfaceClass);
                }
            }

            // Note that we need to look into the superclass since the super class can itself implements an interface
            // that has the @Role annotation.
            Type superType = componentClass.getGenericSuperclass();
            if (superType != null && superType != Object.class) {
                if (superType instanceof ParameterizedType) {
                    ParameterizedType superParameterizedType = (ParameterizedType) superType;
                    types.addAll(findComponentRoleTypes((Class) superParameterizedType.getRawType(), ReflectionUtils
                        .resolveSuperArguments(superParameterizedType.getActualTypeArguments(), componentClass,
                            parameters)));
                } else if (superType instanceof Class) {
                    types.addAll(findComponentRoleTypes((Class) superType, null));
                }
            }
        }

        return types;
    }

    /**
     * Helper method that generate a {@link RuntimeException} in case of a reflection error.
     *
     * @param componentClass the component for which to return the interface types
     * @return the Types representing the interfaces directly implemented by the class or interface represented by this
     *         object
     * @throws RuntimeException in case of a reflection error such as
     *         {@link java.lang.reflect.MalformedParameterizedTypeException}
     */
    private Type[] getGenericInterfaces(Class<?> componentClass)
    {
        Type[] interfaceTypes;
        try {
            interfaceTypes = componentClass.getGenericInterfaces();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to get interface for [%s]", componentClass.getName()), e);
        }
        return interfaceTypes;
    }

    /**
     * Finds the interfaces that implement component roles by looking recursively in all interfaces of the passed
     * component implementation class. If the roles annotation value is specified then use the specified list instead of
     * doing auto-discovery. Also note that we support component classes implementing JSR 330's
     * {@link javax.inject.Provider} (and thus without a component role annotation).
     *
     * @param componentClass the component implementation class for which to find the component roles it implements
     * @return the list of component role classes implemented
     * @deprecated since 4.0M1 use {@link #findComponentRoleTypes(Class)} instead
     */
    @Deprecated
    public Set<Class<?>> findComponentRoleClasses(Class<?> componentClass)
    {
        // Note: We use a Set to ensure that we don't register duplicate roles.
        Set<Class<?>> classes = new LinkedHashSet<>();

        Component component = componentClass.getAnnotation(Component.class);
        if (component != null && component.roles().length > 0) {
            classes.addAll(Arrays.asList(component.roles()));
        } else {
            // Look in both superclass and interfaces for @Role or javax.inject.Provider
            for (Class<?> interfaceClass : componentClass.getInterfaces()) {
                // Handle superclass of interfaces
                classes.addAll(findComponentRoleClasses(interfaceClass));

                // Handle interfaces directly declared in the passed component class
                for (Annotation annotation : interfaceClass.getDeclaredAnnotations()) {
                    if (annotation.annotationType() == ComponentRole.class) {
                        classes.add(interfaceClass);
                    }
                }

                // Handle javax.inject.Provider
                if (Provider.class.isAssignableFrom(interfaceClass)) {
                    classes.add(interfaceClass);
                }
            }

            // Note that we need to look into the superclass since the super class can itself implements an interface
            // that has the @Role annotation.
            Class<?> superClass = componentClass.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                classes.addAll(findComponentRoleClasses(superClass));
            }
        }

        return classes;
    }

    /**
     * Get all components listed in the passed resource file.
     *
     * @param classLoader the classloader to use to find the resources
     * @param location the name of the resources to look for
     * @return the list of component implementation class names
     * @throws IOException in case of an error loading the component list resource
     * @since 3.3M1
     */
    public List<ComponentDeclaration> getDeclaredComponents(ClassLoader classLoader, String location)
        throws IOException
    {
        List<ComponentDeclaration> annotatedClassNames = new ArrayList<>();
        Enumeration<URL> urls = classLoader.getResources(location);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();

            LOGGER.debug("Loading declared component definitions from [{}]", url);

            InputStream componentListStream = url.openStream();

            try {
                annotatedClassNames.addAll(getDeclaredComponents(componentListStream));
            } finally {
                componentListStream.close();
            }
        }

        return annotatedClassNames;
    }

    /**
     * Get all components listed in the passed resource stream. The format is:
     * {@code (priority level):(fully qualified component implementation name)}.
     *
     * @param componentListStream the stream to parse
     * @return the list of component declaration (implementation class names and priorities)
     * @throws IOException in case of an error loading the component list resource
     * @since 3.3M1
     */
    public List<ComponentDeclaration> getDeclaredComponents(InputStream componentListStream) throws IOException
    {
        List<ComponentDeclaration> annotatedClassNames = new ArrayList<>();

        // Read all components definition from the URL
        // Always force UTF-8 as the encoding, since these files are read from the official jars, and those are
        // generated on an 8-bit system.
        BufferedReader in = new BufferedReader(new InputStreamReader(componentListStream, COMPONENT_LIST_ENCODING));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            // Make sure we don't add empty lines
            if (inputLine.trim().length() > 0) {
                try {
                    String[] chunks = inputLine.split(":");
                    ComponentDeclaration componentDeclaration;
                    if (chunks.length > 1) {
                        componentDeclaration = new ComponentDeclaration(chunks[1], Integer.parseInt(chunks[0]));
                    } else {
                        componentDeclaration = new ComponentDeclaration(chunks[0]);
                    }
                    LOGGER.debug("  - Adding component definition [{}] with priority [{}]",
                        componentDeclaration.getImplementationClassName(), componentDeclaration.getPriority());
                    annotatedClassNames.add(componentDeclaration);
                } catch (Exception e) {
                    getLogger().error("Failed to parse component declaration from [{}]", inputLine, e);
                }
            }
        }

        return annotatedClassNames;
    }

    /**
     * Get all components listed in a JAR file.
     *
     * @param jarFile the JAR file to parse
     * @return the list of component declaration (implementation class names and priorities)
     * @throws IOException in case of an error loading the component list resource
     */
    public List<ComponentDeclaration> getDeclaredComponentsFromJAR(InputStream jarFile) throws IOException
    {
        ZipInputStream zis = new ZipInputStream(jarFile);

        List<ComponentDeclaration> componentDeclarations = null;
        List<ComponentDeclaration> componentOverrideDeclarations = null;

        for (ZipEntry entry = zis.getNextEntry(); entry != null
            && (componentDeclarations == null || componentOverrideDeclarations == null); entry = zis.getNextEntry()) {
            if (entry.getName().equals(ComponentAnnotationLoader.COMPONENT_LIST)) {
                componentDeclarations = getDeclaredComponents(zis);
            } else if (entry.getName().equals(ComponentAnnotationLoader.COMPONENT_OVERRIDE_LIST)) {
                componentOverrideDeclarations = getDeclaredComponents(zis);
            }
        }

        // Merge all overrides found with a priority of 0. This is purely for backward compatibility since the
        // override files is now deprecated.
        if (componentOverrideDeclarations != null) {
            if (componentDeclarations == null) {
                componentDeclarations = new ArrayList<>();
            }
            for (ComponentDeclaration componentOverrideDeclaration : componentOverrideDeclarations) {
                componentDeclarations.add(new ComponentDeclaration(componentOverrideDeclaration
                    .getImplementationClassName(), 0));
            }
        }

        return componentDeclarations;
    }

    /**
     * Useful for unit tests that need to capture logs; they can return a mock logger instead of the real logger and
     * thus assert what's been logged.
     *
     * @return the Logger instance to use to log
     */
    protected Logger getLogger()
    {
        return LOGGER;
    }
}
