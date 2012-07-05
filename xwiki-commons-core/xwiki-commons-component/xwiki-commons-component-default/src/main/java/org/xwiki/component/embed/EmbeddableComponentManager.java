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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Provider;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
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

    /**
     * Used as fallback for lookup methods.
     */
    private ComponentManager parent;

    private static class ComponentEntry<R>
    {
        /**
         * Descriptor of the component.
         */
        public final ComponentDescriptor<R> descriptor;

        /**
         * Cached instance of the component. Lazily initialized when needed.
         * <p>
         * This variable can be accesses and modified by many different threads at the same time so we make it volatile
         * to ensure it's really shared and sync between all of them and not in each thread memory.
         */
        public volatile R instance;

        public ComponentEntry(ComponentDescriptor<R> descriptor, R instance)
        {
            this.descriptor = descriptor;
            this.instance = instance;
        }
    }

    private Map<RoleHint< ? >, ComponentEntry< ? >> componentEntries =
        new ConcurrentHashMap<RoleHint< ? >, ComponentEntry< ? >>();

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
            List<ComponentManagerInitializer> initializers = this.getInstanceList(ComponentManagerInitializer.class);
            for (ComponentManagerInitializer initializer : initializers) {
                initializer.initialize(this);
            }
        } catch (ComponentLookupException e) {
            // Should never happen
            this.logger.error("Failed to lookup ComponentManagerInitializer components", e);
        }
    }

    @Override
    public boolean hasComponent(Type role)
    {
        return hasComponent(role, "default");
    }

    @Override
    public boolean hasComponent(Type role, String hint)
    {
        if (this.componentEntries.containsKey(new RoleHint<Object>(role, hint))) {
            return true;
        }

        return getParent() != null ? getParent().hasComponent(role, hint) : false;
    }

    @Override
    public <T> T getInstance(Type roleType) throws ComponentLookupException
    {
        return getComponentInstance(new RoleHint<T>(roleType));
    }

    @Override
    public <T> T getInstance(Type roleType, String roleHint) throws ComponentLookupException
    {
        return getComponentInstance(new RoleHint<T>(roleType, roleHint));
    }

    @Override
    public <T> List<T> getInstanceList(Type role) throws ComponentLookupException
    {
        // Reuse getInstanceMap to make sure to not return components from parent Component Manager overridden by this
        // Component Manager
        Map<String, T> objects = getInstanceMap(role);

        return objects.isEmpty() ? Collections.<T> emptyList() : new ArrayList<T>(objects.values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getInstanceMap(Type role) throws ComponentLookupException
    {
        Map<String, T> objects = new HashMap<String, T>();

        for (Map.Entry<RoleHint< ? >, ComponentEntry< ? >> entry : this.componentEntries.entrySet()) {
            RoleHint< ? > roleHint = entry.getKey();

            if (role.equals(roleHint.getRoleType())) {
                try {
                    objects.put(roleHint.getHint(), getComponentInstance((ComponentEntry<T>) entry.getValue()));
                } catch (Exception e) {
                    throw new ComponentLookupException("Failed to lookup component [" + roleHint + "]", e);
                }
            }
        }

        // Add parent's list of components
        if (getParent() != null) {
            // If the hint already exists in the children Component Manager then don't add the one from the parent.
            for (Map.Entry<String, T> entry : getParent().<T>getInstanceMap(role).entrySet()) {
                if (!objects.containsKey(entry.getKey())) {
                    objects.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return objects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ComponentDescriptor<T> getComponentDescriptor(Type role, String hint)
    {
        ComponentEntry<T> componentEntry = (ComponentEntry<T>) this.componentEntries.get(new RoleHint<T>(role, hint));
        return componentEntry != null ? componentEntry.descriptor : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Type role)
    {
        List<ComponentDescriptor<T>> results = new ArrayList<ComponentDescriptor<T>>();
        for (Map.Entry<RoleHint< ? >, ComponentEntry< ? >> entry : this.componentEntries.entrySet()) {
            // It's possible Class reference are not the same when it coming for different ClassLoader so we
            // compare class names
            if (entry.getKey().getRoleType().equals(role)) {
                results.add((ComponentDescriptor<T>) entry.getValue().descriptor);
            }
        }

        return results;
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

            Class< ? > dependencyRoleClass = ReflectionUtils.getTypeClass(dependency.getRoleType());

            if (dependencyRoleClass.isAssignableFrom(Logger.class)) {
                fieldValue = createLogger(instance.getClass());
            } else if (dependencyRoleClass.isAssignableFrom(List.class)) {
                fieldValue = getInstanceList(ReflectionUtils.getLastTypeGenericArgument(dependency.getRoleType()));
            } else if (dependencyRoleClass.isAssignableFrom(Map.class)) {
                fieldValue = getInstanceMap(ReflectionUtils.getLastTypeGenericArgument(dependency.getRoleType()));
            } else if (dependencyRoleClass.isAssignableFrom(Provider.class)) {
                try {
                    fieldValue = getInstance(dependency.getRoleType(), dependency.getRoleHint());
                } catch (ComponentLookupException e) {
                    fieldValue =
                        new GenericProvider<Object>(this, new RoleHint<Object>(
                            ReflectionUtils.getLastTypeGenericArgument(dependency.getRoleType()),
                            dependency.getRoleHint()));
                }
            } else {
                fieldValue = getInstance(dependency.getRoleType(), dependency.getRoleHint());
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

    /**
     * Create a Logger instance to inject.
     */
    protected Object createLogger(Class< ? > instanceClass)
    {
        return LoggerFactory.getLogger(instanceClass);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getComponentInstance(RoleHint<T> roleHint) throws ComponentLookupException
    {
        T instance;

        ComponentEntry<T> componentEntry = (ComponentEntry<T>) this.componentEntries.get(roleHint);

        if (componentEntry != null) {
            try {
                instance = getComponentInstance(componentEntry);
            } catch (Throwable e) {
                throw new ComponentLookupException(String.format("Failed to lookup component [%s] identifier by [%s]",
                    componentEntry.descriptor.getImplementation().getName(), roleHint.toString()), e);
            }
        } else {
            if (getParent() != null) {
                instance = getParent().getInstance(roleHint.getRoleClass(), roleHint.getHint());
            } else {
                throw new ComponentLookupException("Can't find descriptor for the component [" + roleHint + "]");
            }
        }

        return instance;
    }

    private <T> T getComponentInstance(ComponentEntry<T> componentEntry) throws Exception
    {
        T instance;

        ComponentDescriptor<T> descriptor = componentEntry.descriptor;

        if (descriptor.getInstantiationStrategy() == ComponentInstantiationStrategy.SINGLETON) {
            if (componentEntry.instance != null) {
                // If the instance exists return it
                instance = componentEntry.instance;
            } else {
                synchronized (componentEntry) {
                    // Recheck in case it has been created while we were waiting
                    if (componentEntry.instance != null) {
                        instance = componentEntry.instance;
                    } else {
                        componentEntry.instance = createInstance(descriptor);
                        instance = componentEntry.instance;
                    }
                }
            }
        } else {
            instance = createInstance(descriptor);
        }

        return instance;
    }

    // Add

    private <T> RoleHint<T> getRoleHint(ComponentDescriptor<T> componentDescriptor)
    {
        return new RoleHint<T>(componentDescriptor.getRoleType(), componentDescriptor.getRoleHint());
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor) throws ComponentRepositoryException
    {
        registerComponent(componentDescriptor, null);
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor, T componentInstance)
    {
        RoleHint<T> roleHint = getRoleHint(componentDescriptor);

        // Remove any existing component associated to the provided roleHint
        removeComponentWithoutException(roleHint);

        // Register new component
        addComponent(roleHint, new DefaultComponentDescriptor<T>(componentDescriptor), componentInstance);
    }

    private <T> void addComponent(RoleHint<T> roleHint, ComponentDescriptor<T> descriptor, T instance)
    {
        ComponentEntry<T> componentEntry = new ComponentEntry<T>(descriptor, instance);

        // Register new component
        this.componentEntries.put(roleHint, componentEntry);

        // Send event about component registration
        if (this.eventManager != null) {
            this.eventManager.notifyComponentRegistered(descriptor, this);
        }
    }

    // Remove

    @Override
    public void unregisterComponent(Type role, String hint)
    {
        removeComponentWithoutException(new RoleHint<Object>(role, hint));
    }

    @Override
    public void unregisterComponent(ComponentDescriptor< ? > componentDescriptor)
    {
        if (ObjectUtils.equals(
            getComponentDescriptor(componentDescriptor.getRoleType(), componentDescriptor.getRoleHint()),
            componentDescriptor)) {
            unregisterComponent(componentDescriptor.getRoleType(), componentDescriptor.getRoleHint());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void release(Object component) throws ComponentLifecycleException
    {
        // First find the descriptor matching the passed component
        RoleHint< ? > key = null;
        ComponentDescriptor< ? > oldDescriptor = null;
        for (Map.Entry<RoleHint< ? >, ComponentEntry< ? >> entry : this.componentEntries.entrySet()) {
            if (entry.getValue().instance == component) {
                key = entry.getKey();
                oldDescriptor = entry.getValue().descriptor;
                break;
            }
        }

        // Note that we're not removing inside the for loop above since it would cause a Concurrent
        // exception since we'd modify the map accessed by the iterator.
        if (key != null) {
            // We do the following:
            // - fire an unregistration event, to tell the world that this reference is now dead
            // - fire a registration event, to tell the world that it could get a new reference for this component
            // now
            // We need to do this since code holding a reference on the released component may need to know it's
            // been removed and thus discard its own reference to that component and look it up again.
            // Another solution would be to introduce a new event for Component creation/destruction (right now
            // we only send events for Component registration/unregistration).
            removeComponent(key);
            addComponent((RoleHint<Object>) key, (ComponentDescriptor<Object>) oldDescriptor, null);
        }
    }

    private void releaseInstance(ComponentEntry< ? > componentEntry) throws ComponentLifecycleException
    {
        // Make sure the singleton component instance can't be "lost" (impossible to dispose because returned but not
        // stored).
        synchronized (componentEntry) {
            Object instance = componentEntry.instance;

            // Give a chance to the component to clean up
            if (instance instanceof Disposable) {
                ((Disposable) instance).dispose();
            }

            componentEntry.instance = null;
        }
    }

    private void releaseComponentEntry(ComponentEntry< ? > componentEntry) throws ComponentLifecycleException
    {
        // clean existing instance
        releaseInstance(componentEntry);
    }

    private void removeComponent(RoleHint< ? > roleHint) throws ComponentLifecycleException
    {
        // Make sure to remove the entry from the map before destroying it to reduce at the minimum the risk of
        // lookupping something invalid
        ComponentEntry< ? > componentEntry = this.componentEntries.remove(roleHint);

        if (componentEntry != null) {
            ComponentDescriptor< ? > oldDescriptor = componentEntry.descriptor;

            // clean any resource associated to the component instance and descriptor
            releaseComponentEntry(componentEntry);

            // Send event about component unregistration
            if (this.eventManager != null && oldDescriptor != null) {
                this.eventManager.notifyComponentUnregistered(oldDescriptor, this);
            }
        }
    }

    /**
     * Note: This method shouldn't exist but register/unregister methods should throw a
     * {@link ComponentLifecycleException} but that would break backward compatibility to add it.
     */
    private <T> void removeComponentWithoutException(RoleHint<T> roleHint)
    {
        try {
            removeComponent(roleHint);
        } catch (Exception e) {
            logger.warn("Instance released but disposal failed. Some resources may not have been released.", e);
        }
    }

    // Deprecated

    @Override
    @SuppressWarnings("unchecked")
    @Deprecated
    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Class<T> role)
    {
        List<ComponentDescriptor<T>> results = new ArrayList<ComponentDescriptor<T>>();
        for (Map.Entry<RoleHint< ? >, ComponentEntry< ? >> entry : this.componentEntries.entrySet()) {
            // It's possible Class reference are not the same when it coming for different ClassLoader so we
            // compare class names
            if (entry.getKey().getRoleClass() == role) {
                results.add((ComponentDescriptor<T>) entry.getValue().descriptor);
            }
        }
        return results;
    }
}
