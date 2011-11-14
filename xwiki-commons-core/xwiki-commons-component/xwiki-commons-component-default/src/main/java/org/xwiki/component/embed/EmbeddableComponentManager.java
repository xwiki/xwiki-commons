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
package org.xwiki.component.embed;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentManagerInitializer;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Simple implementation of {@link ComponentManager} to be used when using some XWiki modules standalone.
 * 
 * @version $Id$
 * @since 2.0M1
 */
public class EmbeddableComponentManager implements ComponentManager
{
    private ComponentEventManager eventManager;

    private ComponentManager parent;

    private Map<RoleHint< ? >, ComponentDescriptor< ? >> descriptors =
        new HashMap<RoleHint< ? >, ComponentDescriptor< ? >>();

    private Map<RoleHint< ? >, Object> components = new ConcurrentHashMap<RoleHint< ? >, Object>();

    private Map<RoleHint< ? >, Provider< ? >> providers = new ConcurrentHashMap<RoleHint< ? >, Provider< ? >>();

    private Logger logger = LoggerFactory.getLogger(EmbeddableComponentManager.class);

    /**
     * Finds all lifecycle handlers to use when instantiating a Component.
     */
    private ServiceLoader<LifecycleHandler> lifecycleHandlers = ServiceLoader.load(LifecycleHandler.class);

    /**
     * Load all component annotations and register them as components.
     * 
     * @param classLoader the class loader to use to look for component definitions
     */
    public void initialize(ClassLoader classLoader)
    {
        ComponentAnnotationLoader loader = new ComponentAnnotationLoader();
        loader.initialize(this, classLoader);

        // Extension point to allow component to manipulate ComponentManager initialized state.
        try {
            List<ComponentManagerInitializer> initializers = this.lookupList(ComponentManagerInitializer.class);

            for (ComponentManagerInitializer initializer : initializers) {
                initializer.initialize(this);
            }
        } catch (ComponentLookupException e) {
            // Should never happen
            this.logger.error("Failed to lookup ComponentManagerInitializer components", e);
        }
    }

    @Override
    public <T> boolean hasComponent(Class<T> role, String hint)
    {
        return this.descriptors.containsKey(new RoleHint<T>(role, hint));
    }

    @Override
    public <T> boolean hasComponent(Class<T> role)
    {
        return this.descriptors.containsKey(new RoleHint<T>(role));
    }

    @Override
    public <T> T lookup(Class<T> role) throws ComponentLookupException
    {
        return initialize(new RoleHint<T>(role));
    }

    @Override
    public <T> T lookup(Class<T> role, String hint) throws ComponentLookupException
    {
        return initialize(new RoleHint<T>(role, hint));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> lookupList(Class<T> role) throws ComponentLookupException
    {
        List<T> objects = new ArrayList<T>();
        synchronized (this) {
            for (RoleHint< ? > roleHint : this.descriptors.keySet()) {
                // It's possible Class reference are not the same when it's coming form different ClassLoader so we
                // compare class names
                if (roleHint.getRole().getName().equals(role.getName())) {
                    objects.add(initialize((RoleHint<T>) roleHint));
                }
            }
            // Add parent's list of components
            if (getParent() != null) {
                objects.addAll(getParent().lookupList(role));
            }
        }
        return objects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException
    {
        Map<String, T> objects = new HashMap<String, T>();
        synchronized (this) {
            for (RoleHint< ? > roleHint : this.descriptors.keySet()) {
                // It's possible Class reference are not the same when it coming for different ClassLoader so we
                // compare class names
                if (roleHint.getRole().getName().equals(role.getName())) {
                    objects.put(roleHint.getHint(), initialize((RoleHint<T>) roleHint));
                }
            }
            // Add parent's list of components
            if (getParent() != null) {
                // If the hint already exists in the children Component Manager then don't add the one from the parent.
                for (Map.Entry<String, T> entry : getParent().lookupMap(role).entrySet()) {
                    if (!objects.containsKey(entry.getKey())) {
                        objects.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        return objects;
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor) throws ComponentRepositoryException
    {
        registerComponent(componentDescriptor, null);
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor, T componentInstance)
    {
        ComponentDescriptor<T> registeredDescriptor;

        synchronized (this) {
            RoleHint<T> roleHint = new RoleHint<T>(componentDescriptor.getRole(), componentDescriptor.getRoleHint());

            // Remove any existing instance since we're replacing it
            registeredDescriptor = (ComponentDescriptor<T>) this.descriptors.get(roleHint);
            removeComponentWithoutException(roleHint, registeredDescriptor);

            if (componentInstance != null) {
                // Set initial instance of the component
                addComponent(roleHint, componentInstance, componentDescriptor);
            }

            // Register the new descriptor
            this.descriptors.put(roleHint, componentDescriptor);
        }

        // Send event about component registration
        if (this.eventManager != null) {
            if (registeredDescriptor != null) {
                this.eventManager.notifyComponentUnregistered(registeredDescriptor);
            }
            this.eventManager.notifyComponentRegistered(componentDescriptor);
        }
    }

    @Override
    public <T> void unregisterComponent(Class<T> role, String hint)
    {
        ComponentDescriptor<T> oldDescriptor;

        synchronized (this) {
            RoleHint<T> roleHint = new RoleHint<T>(role, hint);

            oldDescriptor = (ComponentDescriptor<T>) this.descriptors.get(roleHint);
            removeComponentWithoutException(roleHint, oldDescriptor);

            if (oldDescriptor != null) {
                this.descriptors.remove(roleHint);
            }
        }

        // Send event about component unregistration
        if (oldDescriptor != null && this.eventManager != null) {
            this.eventManager.notifyComponentUnregistered(oldDescriptor);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ComponentDescriptor<T> getComponentDescriptor(Class<T> role, String hint)
    {
        synchronized (this) {
            return (ComponentDescriptor<T>) this.descriptors.get(new RoleHint<T>(role, hint));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Class<T> role)
    {
        synchronized (this) {
            List<ComponentDescriptor<T>> results = new ArrayList<ComponentDescriptor<T>>();
            for (Map.Entry<RoleHint< ? >, ComponentDescriptor< ? >> entry : this.descriptors.entrySet()) {
                // It's possible Class reference are not the same when it coming for different ClassLoader so we
                // compare class names
                if (entry.getKey().getRole().getName().equals(role.getName())) {
                    results.add((ComponentDescriptor<T>) entry.getValue());
                }
            }
            return results;
        }
    }

    @Override
    public <T> void release(T component) throws ComponentLifecycleException
    {
        synchronized (this) {
            // First find the descriptor matching the passed component
            RoleHint<?> key = null;
            for (Map.Entry<RoleHint<?>, ? extends Object> entry : this.components.entrySet()) {
                if (entry.getValue() == component) {
                    key = entry.getKey();
                    break;
                }
            }
            // Note that we're not removing inside the for loop above since it would cause a Concurrent
            // exception since we'd modify the map accessed by the iterator.
            if (key != null) {
                removeComponent(key);
            }
        }
    }

    @Override
    public ComponentEventManager getComponentEventManager()
    {
        return this.eventManager;
    }

    @Override
    public void setComponentEventManager(ComponentEventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    @Override
    public ComponentManager getParent()
    {
        return this.parent;
    }

    @Override
    public void setParent(ComponentManager parentComponentManager)
    {
        this.parent = parentComponentManager;
    }

    @SuppressWarnings("unchecked")
    private <T> T initialize(RoleHint<T> roleHint) throws ComponentLookupException
    {
        // If the instance exists return it
        T instance = (T) this.components.get(roleHint);

        if (instance == null) {
            synchronized (this) {
                // If the instance exists return it
                instance = (T) this.components.get(roleHint);
                if (instance == null) {
                    // If there's a component descriptor, create the instance
                    ComponentDescriptor<T> descriptor = (ComponentDescriptor<T>) this.descriptors.get(roleHint);
                    if (descriptor != null) {
                        try {
                            instance = createInstance(descriptor);
                            if (instance == null) {
                                throw new ComponentLookupException("Failed to lookup component [" + roleHint + "]");
                            } else if (this.descriptors.get(
                                roleHint).getInstantiationStrategy() == ComponentInstantiationStrategy.SINGLETON)
                            {
                                addComponent(roleHint, instance, descriptor);
                            }
                        } catch (Exception e) {
                            throw new ComponentLookupException("Failed to lookup component [" + roleHint + "]", e);
                        }
                    } else {
                        // Look for the component in the parent Component Manager (if there's a parent)
                        ComponentManager parent = getParent();
                        if (parent != null) {
                            instance = getParent().lookup(roleHint.getRole(), roleHint.getHint());
                        } else {
                            throw new ComponentLookupException("Can't find descriptor for the component [" + roleHint
                                + "]");
                        }
                    }
                }
            }
        }

        return instance;
    }

    private <T> T createInstance(ComponentDescriptor<T> descriptor) throws Exception
    {
        T instance = descriptor.getImplementation().newInstance();

        // Set each dependency
        for (ComponentDependency< ? > dependency : descriptor.getComponentDependencies()) {

            // TODO: Handle dependency cycles

            // Handle different field types
            Object fieldValue;

            // Step 1: Verify if there's a Provider registered for the field type
            // - A Provider is a component like any other (except it cannot have a field produced by itself!)
            // - A Provider must implement the JSR330 Producer interface
            //
            // Step 2: Handle Logger injection.
            //
            // Step 3: No producer found, handle scalar and collection types by looking up standard component
            // implementations.

            if (Provider.class.isAssignableFrom(dependency.getMappingType())) {
                // Then get the class the Provider is providing for
                Field field = instance.getClass().getDeclaredField(dependency.getName());
                Class< ? > fieldRole = ReflectionUtils.getLastGenericFieldType(field);
                // Then lookup for a Provider registered with the default hint and for the Component Role it provides
                RoleHint roleHint = new RoleHint(fieldRole, dependency.getRoleHint());
                Provider< ? > provider = this.providers.get(roleHint);
                if (provider != null) {
                    fieldValue = provider;
                } else {
                    // Inject a default Provider
                    fieldValue = new GenericProvider(this, new RoleHint(fieldRole));
                }
            } else if ((dependency.getMappingType() != null)
                && Logger.class.isAssignableFrom(dependency.getMappingType()))
            {
                fieldValue = LoggerFactory.getLogger(instance.getClass());
            } else if ((dependency.getMappingType() != null)
                && List.class.isAssignableFrom(dependency.getMappingType()))
            {
                fieldValue = lookupList(dependency.getRole());
            } else if ((dependency.getMappingType() != null)
                && Map.class.isAssignableFrom(dependency.getMappingType()))
            {
                fieldValue = lookupMap(dependency.getRole());
            } else {
                fieldValue = lookup(dependency.getRole(), dependency.getRoleHint());
            }

            // Set the field by introspection
            if (fieldValue != null) {
                ReflectionUtils.setFieldValue(instance, dependency.getName(), fieldValue);
            }
        }

        // Call Lifecycle Handlers
        for (LifecycleHandler lifecycleHandler : this.lifecycleHandlers) {
            lifecycleHandler.handle(instance, descriptor, this);
        }
        
        return instance;
    }

    private <T> void addComponent(RoleHint<T> roleHint, T instance, ComponentDescriptor<T> descriptor)
    {
        this.components.put(roleHint, instance);

        // If the instance is a Provider also add it in the Providers's cache.
        if (Provider.class.isAssignableFrom(instance.getClass())) {
            Provider<?> provider = (Provider) instance;
            Class<?> roleClass = ReflectionUtils.getLastGenericClassType(instance.getClass(), Provider.class);
            this.providers.put(new RoleHint(roleClass, descriptor.getRoleHint()), provider);
        }
    }

    private <T> T removeComponent(RoleHint<T> roleHint) throws ComponentLifecycleException
    {
        return removeComponent(roleHint, (ComponentDescriptor<T>) this.descriptors.get(roleHint));
    }

    private <T> T removeComponent(RoleHint<T> roleHint, ComponentDescriptor<T> descriptor)
        throws ComponentLifecycleException
    {
        T component = (T) this.components.remove(roleHint);

        // Give a chance to the component to clean up
        if (component instanceof Disposable) {
            ((Disposable) component).dispose();
        }

        // If the component is a Provider also remove it from the Providers's cache.
        if (component != null && Provider.class.isAssignableFrom(component.getClass())) {
            Class<?> roleClass = ReflectionUtils.getLastGenericClassType(component.getClass(), Provider.class);
            this.providers.remove(new RoleHint(roleClass, descriptor.getRoleHint()));
        }

        return component;
    }

    /**
     * Note: This method shouldn't exist but register/unregister methods should throw a
     * {@link ComponentLifecycleException} but that would break backward compatibility to add it.
     */
    private <T> void removeComponentWithoutException(RoleHint<T> roleHint, ComponentDescriptor<T> descriptor)
    {
        try {
            removeComponent(roleHint, descriptor);
        } catch (Exception e) {
            logger.warn("Instance released but disposal failed. Some resources may not have been released.", e);
        }
    }
}
