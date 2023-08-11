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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.collection.internal.PriorityEntries;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.annotation.DisposePriority;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.internal.JakartaGenericProvider;
import org.xwiki.component.internal.JakartaJavaxProvider;
import org.xwiki.component.internal.JavaxGenericProvider;
import org.xwiki.component.internal.JavaxJakartaProvider;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentManagerInitializer;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.manager.NamespacedComponentManager;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Simple implementation of {@link ComponentManager} to be used when using some XWiki modules standalone.
 *
 * @version $Id$
 * @since 2.0M1
 */
public class EmbeddableComponentManager implements NamespacedComponentManager, Disposable
{
    /**
     * Logger to use to log shutdown information (opposite of initialization).
     */
    private static final Logger SHUTDOWN_LOGGER = LoggerFactory.getLogger("org.xwiki.shutdown");

    private static class ComponentEntry<R> implements Comparable<ComponentEntry<R>>
    {
        /**
         * Descriptor of the component.
         */
        final ComponentDescriptor<R> descriptor;

        /**
         * Cached instance of the component. Lazily initialized when needed.
         * <p>
         * This variable can be accesses and modified by many different threads at the same time so we make it volatile
         * to ensure it's really shared and sync between all of them and not in each thread memory.
         */
        volatile R instance;

        /**
         * Flag used when computing the disposal order.
         */
        boolean disposing = false;

        ComponentEntry(ComponentDescriptor<R> descriptor, R instance)
        {
            this.descriptor = descriptor;
            this.instance = instance;
        }

        @Override
        public int compareTo(ComponentEntry<R> other)
        {
            return this.descriptor.getRoleTypePriority() - other.descriptor.getRoleTypePriority();
        }

        @Override
        public String toString()
        {
            return this.descriptor.toString();
        }
    }

    private static class ComponentEntries<R> extends PriorityEntries<ComponentEntry<R>>
    {
        ComponentEntries()
        {
            super();
        }

        ComponentEntries(int initialCapacity)
        {
            super(initialCapacity);
        }

        ComponentEntry<R> get(R instance)
        {
            this.lock.readLock().lock();

            try {
                for (ComponentEntry<R> entry : this.map.values()) {
                    if (entry.instance == instance) {
                        return entry;
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }

            return null;
        }

        List<ComponentDescriptor<R>> toComponentDescriptorList()
        {
            this.lock.readLock().lock();

            try {
                List<ComponentDescriptor<R>> list = new ArrayList<>(size());
                for (ComponentEntry<R> entry : getSorted()) {
                    list.add(entry.descriptor);
                }

                return list;
            } finally {
                this.lock.readLock().unlock();
            }
        }

        void put(ComponentDescriptor<R> descriptor)
        {
            put(descriptor, null);
        }

        void put(ComponentDescriptor<R> descriptor, R instance)
        {
            this.lock.writeLock().lock();

            try {
                ComponentEntry<R> currentEntry = this.map.get(descriptor.getRoleHint());

                if (currentEntry == null
                    // In case of same priority, first inserted wins
                    || currentEntry.descriptor.getRoleHintPriority() > descriptor.getRoleHintPriority()) {
                    put(descriptor.getRoleHint(), new ComponentEntry<>(descriptor, instance));
                }
            } finally {
                this.lock.writeLock().unlock();
            }
        }

        void putAll(ComponentEntries<R> newEntries)
        {
            this.lock.writeLock().lock();

            try {
                for (ComponentEntry<R> newEntry : newEntries.map.values()) {
                    ComponentEntry<R> currentEntry = this.map.get(newEntry.descriptor.getRoleHint());

                    if (currentEntry == null
                        // In case of same priority, first inserted wins
                        || currentEntry.descriptor.getRoleHintPriority() > newEntry.descriptor.getRoleHintPriority()) {
                        put(newEntry.descriptor.getRoleHint(), newEntry);
                    }
                }
            } finally {
                this.lock.writeLock().unlock();
            }
        }
    }

    /**
     * @see #getNamespace()
     */
    private String namespace;

    private ComponentEventManager eventManager;

    /**
     * Used as fallback for lookup methods.
     */
    private ComponentManager parent;

    private ConcurrentMap<Type, ComponentEntries> componentEntries = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(EmbeddableComponentManager.class);

    /**
     * Finds all lifecycle handlers to use when instantiating a Component.
     */
    private ServiceLoader<LifecycleHandler> lifecycleHandlers = ServiceLoader.load(LifecycleHandler.class);

    public EmbeddableComponentManager()
    {
        registerThis();
    }

    /**
     * @param namespace the namespace associated with this component manager
     * @since 6.4M2
     */
    public EmbeddableComponentManager(String namespace)
    {
        registerThis();

        this.namespace = namespace;
    }

    @Override
    public String getNamespace()
    {
        return this.namespace;
    }

    /**
     * Allow to lookup the this as default {@link ComponentManager} implementation.
     */
    private void registerThis()
    {
        DefaultComponentDescriptor<ComponentManager> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(ComponentManager.class);

        registerComponent(cd, this);
    }

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
        return hasComponent(role, null);
    }

    @Override
    public boolean hasComponent(Type roleType, String roleHint)
    {
        if (getComponentEntry(roleType, roleHint, true) != null) {
            return true;
        }

        return getParent() != null ? getParent().hasComponent(roleType, roleHint) : false;
    }

    @Override
    public <T> T getInstance(Type roleType) throws ComponentLookupException
    {
        return getInstance(roleType, null);
    }

    @Override
    public <T> T getInstance(Type roleType, String roleHint) throws ComponentLookupException
    {
        T instance;

        ComponentEntry<T> componentEntry = (ComponentEntry<T>) getComponentEntry(roleType, roleHint, true);

        if (componentEntry == null) {
            if (getParent() != null) {
                // Try the parent
                instance = getParent().getInstance(roleType, roleHint);
            } else {
                throw new ComponentLookupException(
                    "Can't find descriptor for the component with type [" + roleType + "] and hint [" + roleHint + "]");
            }
        } else if (getParent() != null) {
            // Get parent descriptor
            ComponentDescriptor<T> parentDescriptor = getParent().getComponentDescriptor(roleType, roleHint);

            if (parentDescriptor != null
                && parentDescriptor.getRoleHintPriority() < componentEntry.descriptor.getRoleHintPriority()) {
                // The parent component has priority over the current one
                instance = getParent().getInstance(roleType, roleHint);
            } else {
                // The current component has priority over the parent one
                instance = getInstance(componentEntry);
            }
        } else {
            instance = getInstance(componentEntry);
        }

        return instance;
    }

    private <T> T getInstance(ComponentEntry<T> componentEntry) throws ComponentLookupException
    {
        try {
            return getComponentInstance(componentEntry);
        } catch (Throwable e) {
            throw new ComponentLookupException(
                String.format("Failed to lookup component [%s] identified by type [%s] and hint [%s]",
                    componentEntry.descriptor.getImplementation().getName(), componentEntry.descriptor.getRoleType(),
                    componentEntry.descriptor.getRoleHint()),
                e);
        }
    }

    @Override
    public <T> List<T> getInstanceList(Type role) throws ComponentLookupException
    {
        // Reuse getInstanceMap to make sure to not return components from parent Component Manager overridden by this
        // Component Manager
        Map<String, T> objects = getInstanceMap(role);

        return objects.isEmpty() ? Collections.<T>emptyList() : new ArrayList<>(objects.values());
    }

    private <T> List<ComponentDescriptor<T>> getParentDescriptors(Type roleType)
    {
        return getParent() != null ? getParent().getComponentDescriptorList(roleType) : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getInstanceMap(Type roleType) throws ComponentLookupException
    {
        Map<String, T> entries;

        ComponentEntries<T> localEntries = this.componentEntries.get(roleType);
        if (localEntries == null || localEntries.isEmpty()) {
            if (getParent() != null) {
                // We have only parent entries
                entries = getParent().getInstanceMap(roleType);
            } else {
                // We have neither parent nor local entries
                entries = Collections.emptyMap();
            }
        } else {
            localEntries.getLock().readLock().lock();

            try {
                // Get parent descriptors
                List<ComponentDescriptor<T>> parentDescriptors = getParentDescriptors(roleType);
                if (!parentDescriptors.isEmpty()) {
                    // We have both parent and local entries
                    ComponentEntries<T> mergedEntries =
                        new ComponentEntries<>(localEntries.size() + parentDescriptors.size());

                    // Add local entries
                    mergedEntries.putAll(localEntries);

                    // Add parent entries
                    for (ComponentDescriptor<T> parentDescriptor : parentDescriptors) {
                        mergedEntries.put(parentDescriptor,
                            getParent().getInstance(roleType, parentDescriptor.getRoleHint()));
                    }

                    entries = toInstanceMap(mergedEntries);
                } else {
                    // We have only local entries
                    entries = toInstanceMap(localEntries);
                }
            } finally {
                localEntries.getLock().readLock().unlock();
            }
        }

        return entries;
    }

    private <T> Map<String, T> toInstanceMap(ComponentEntries<T> componentEntries) throws ComponentLookupException
    {
        Map<String, T> map = new LinkedHashMap<>();

        for (ComponentEntry<T> roleEntry : componentEntries.getSorted()) {
            T instance = getMapInstance(roleEntry);
            if (instance != null) {
                map.put(roleEntry.descriptor.getRoleHint(), instance);
            }
        }

        return map;
    }

    private <T> T getMapInstance(ComponentEntry<T> roleEntry) throws ComponentLookupException
    {
        try {
            return getComponentInstance(roleEntry);
        } catch (Exception | Error e) {
            if (roleEntry.descriptor.isMandatory()) {
                throw new ComponentLookupException("Failed to lookup component with type ["
                    + roleEntry.descriptor.getRoleType() + "] and hint [" + roleEntry.descriptor.getRoleHint() + "]",
                    e);
            } else {
                this.logger.error("Failed to lookup component with type [{}] and hint [{}]",
                    roleEntry.descriptor.getRoleType(), roleEntry.descriptor.getRoleHint(), e);
            }
        }

        return null;
    }

    private ComponentEntry<?> tryOtherProvider(Type roleType, String roleHint)
    {
        ComponentEntry<?> entry = null;

        if (roleType instanceof ParameterizedType) {
            ParameterizedType parameterizedRoleType = (ParameterizedType) roleType;

            if (parameterizedRoleType.getRawType() == Provider.class) {
                // Try to get the javax version of the provider
                ParameterizedType javaxProviderType = new DefaultParameterizedType(null, javax.inject.Provider.class,
                    parameterizedRoleType.getActualTypeArguments()[0]);
                ComponentEntry<?> javaxEntry = getComponentEntry(javaxProviderType, roleHint);

                if (javaxEntry != null) {
                    // Wrap it as a jakarta provider
                    DefaultComponentDescriptor descriptor = new DefaultComponentDescriptor<>(javaxEntry.descriptor);
                    descriptor.setRoleType(Provider.class);
                    descriptor.setImplementation(JavaxJakartaProvider.class);

                    entry = new ComponentEntry(descriptor, new JavaxJakartaProvider(this, javaxProviderType, roleHint));
                }
            } else if (parameterizedRoleType.getRawType() == javax.inject.Provider.class) {
                // Try to get the jakarta version of the provider
                ParameterizedType jakartaProviderType = new DefaultParameterizedType(null, Provider.class,
                    parameterizedRoleType.getActualTypeArguments()[0]);
                ComponentEntry<?> jakartaEntry = getComponentEntry(jakartaProviderType, roleHint);

                if (jakartaEntry != null) {
                    // Wrap it as a javax provider
                    DefaultComponentDescriptor descriptor = new DefaultComponentDescriptor<>(jakartaEntry.descriptor);
                    descriptor.setRoleType(javax.inject.Provider.class);
                    descriptor.setImplementation(JakartaJavaxProvider.class);

                    entry =
                        new ComponentEntry(descriptor, new JakartaJavaxProvider(this, jakartaProviderType, roleHint));
                }
            }
        }

        return entry;
    }

    private ComponentEntry<?> getComponentEntry(Type role, String hint, boolean javaxBridge)
    {
        ComponentEntry<?> entry = getComponentEntry(role, hint);

        if (entry == null && javaxBridge) {
            // Retro compatibility: proxy javax or jakarta Provider to the other side
            entry = tryOtherProvider(role, hint);
        }

        return entry;
    }

    private ComponentEntry<?> getComponentEntry(Type role, String hint)
    {
        ComponentEntries<?> entries = this.componentEntries.get(role);
        if (entries != null) {
            return entries.get(hint != null ? hint : RoleHint.DEFAULT_HINT);
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ComponentDescriptor<T> getComponentDescriptor(Type role, String hint)
    {
        ComponentDescriptor<T> result = null;

        ComponentEntry<?> componentEntry = getComponentEntry(role, hint);
        if (componentEntry == null) {
            // Check in parent!
            if (getParent() != null) {
                // We have only parent entries
                result = getParent().getComponentDescriptor(role, hint);
            }
        } else {
            result = (ComponentDescriptor<T>) componentEntry.descriptor;
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Type roleType)
    {
        List<ComponentDescriptor<T>> descriptors;

        ComponentEntries<T> localEntries = this.componentEntries.get(roleType);
        List<ComponentDescriptor<T>> parentDescriptors = getParentDescriptors(roleType);
        if (localEntries == null || localEntries.isEmpty()) {
            if (!parentDescriptors.isEmpty()) {
                // We have only parent entries
                descriptors = parentDescriptors;
            } else {
                // We have neither parent nor local entries
                descriptors = Collections.emptyList();
            }
        } else {
            // Get parent descriptors
            if (!parentDescriptors.isEmpty()) {
                // We have both parent and local entries
                ComponentEntries<T> mergedEntries =
                    new ComponentEntries<>(localEntries.size() + parentDescriptors.size());

                // Add local entries
                mergedEntries.putAll(localEntries);

                // Add parent entries
                for (ComponentDescriptor<T> parentDescriptor : parentDescriptors) {
                    mergedEntries.put(parentDescriptor);
                }

                descriptors = mergedEntries.toComponentDescriptorList();
            } else {
                // We have only local entries
                descriptors = localEntries.toComponentDescriptorList();
            }
        }

        return descriptors;
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
        for (ComponentDependency<?> dependency : descriptor.getComponentDependencies()) {

            // TODO: Handle dependency cycles

            // Handle different field types
            Object fieldValue = getDependencyInstance(descriptor, instance, dependency);

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

    protected Object getDependencyInstance(ComponentDescriptor<?> descriptor, Object parentInstance,
        ComponentDependency<?> dependency) throws ComponentLookupException
    {
        // TODO: Handle dependency cycles

        // Handle different field types
        Object fieldValue;

        ComponentEntry<?> dependencyEntry = getComponentEntry(dependency.getRoleType(), dependency.getRoleHint(), true);

        if (dependencyEntry != null) {
            // There is a component explicitly registered for this role type
            fieldValue = getInstance(dependency.getRoleType(), dependency.getRoleHint());
        } else {
            // There is no component explicitly registered for this role type, check for various types with a special
            // meaning
            Class<?> dependencyRoleClass = ReflectionUtils.getTypeClass(dependency.getRoleType());

            if (dependencyRoleClass.isAssignableFrom(Logger.class)) {
                fieldValue = createLogger(parentInstance.getClass());
            } else if (dependencyRoleClass.isAssignableFrom(List.class)) {
                fieldValue = getInstanceList(ReflectionUtils.getLastTypeGenericArgument(dependency.getRoleType()));
            } else if (dependencyRoleClass.isAssignableFrom(Map.class)) {
                fieldValue = getInstanceMap(ReflectionUtils.getLastTypeGenericArgument(dependency.getRoleType()));
            } else if (dependencyRoleClass.isAssignableFrom(Provider.class)) {
                // Check if there's a Provider registered for the type
                if (hasComponent(dependency.getRoleType(), dependency.getRoleHint())) {
                    fieldValue = getInstance(dependency.getRoleType(), dependency.getRoleHint());
                } else {
                    fieldValue = createJakartaGenericProvider(descriptor, dependency);
                }
            } else if (dependencyRoleClass.isAssignableFrom(javax.inject.Provider.class)) {
                // Check if there's a Provider registered for the type
                if (hasComponent(dependency.getRoleType(), dependency.getRoleHint())) {
                    fieldValue = getInstance(dependency.getRoleType(), dependency.getRoleHint());
                } else {
                    fieldValue = createGenericProvider(descriptor, dependency);
                }
            } else if (dependencyRoleClass.isAssignableFrom(ComponentDescriptor.class)) {
                fieldValue = new DefaultComponentDescriptor<>(descriptor);
            } else {
                fieldValue = getInstance(dependency.getRoleType(), dependency.getRoleHint());
            }
        }

        return fieldValue;
    }

    protected javax.inject.Provider<?> createGenericProvider(ComponentDescriptor<?> descriptor,
        ComponentDependency<?> dependency)
    {
        return new JavaxGenericProvider<>(this, new RoleHint<>(
            ReflectionUtils.getLastTypeGenericArgument(dependency.getRoleType()), dependency.getRoleHint()));
    }

    protected Provider<?> createJakartaGenericProvider(ComponentDescriptor<?> descriptor,
        ComponentDependency<?> dependency)
    {
        return new JakartaGenericProvider<>(this, new RoleHint<>(
            ReflectionUtils.getLastTypeGenericArgument(dependency.getRoleType()), dependency.getRoleHint()));
    }

    /**
     * Create a Logger instance to inject.
     */
    protected Object createLogger(Class<?> instanceClass)
    {
        return LoggerFactory.getLogger(instanceClass);
    }

    /**
     * @deprecated use {@link #getInstance(Type, String)} instead
     */
    @Deprecated
    protected <T> T getComponentInstance(RoleHint<T> roleHint) throws ComponentLookupException
    {
        return getInstance(roleHint.getRoleType(), roleHint.getHint());
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
                    // Re-check in case it has been created while we were waiting
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

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor) throws ComponentRepositoryException
    {
        registerComponent(componentDescriptor, null);
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor, T componentInstance)
    {
        ComponentEntry<?> componentEntry =
            getComponentEntry(componentDescriptor.getRoleType(), componentDescriptor.getRoleHint(), true);

        if (componentEntry == null
            || componentEntry.descriptor.getRoleHintPriority() >= componentDescriptor.getRoleHintPriority()) {
            // Remove any existing component associated to the provided roleHint
            removeComponentWithoutException(componentDescriptor.getRoleType(), componentDescriptor.getRoleHint());

            // Register new component
            addComponent(new DefaultComponentDescriptor<>(componentDescriptor), componentInstance);
        }
    }

    private <T> void addComponent(ComponentDescriptor<T> descriptor, T instance)
    {
        // Register new component
        ComponentEntries<T> entries =
            this.componentEntries.computeIfAbsent(descriptor.getRoleType(), (k) -> new ComponentEntries());
        entries.put(descriptor, instance);

        // Send event about component registration
        if (this.eventManager != null) {
            this.eventManager.notifyComponentRegistered(descriptor, this);
        }
    }

    // Remove

    @Override
    public void unregisterComponent(Type role, String hint)
    {
        removeComponentWithoutException(role, hint);
    }

    @Override
    public void unregisterComponent(ComponentDescriptor<?> componentDescriptor)
    {
        if (Objects.equals(getComponentDescriptor(componentDescriptor.getRoleType(), componentDescriptor.getRoleHint()),
            componentDescriptor)) {
            unregisterComponent(componentDescriptor.getRoleType(), componentDescriptor.getRoleHint());
        }
    }

    @Override
    public void release(Object component) throws ComponentLifecycleException
    {
        // First find the descriptor matching the passed component
        ComponentEntry<?> componentEntry = null;
        for (ComponentEntries entries : this.componentEntries.values()) {
            componentEntry = entries.get(component);
            if (componentEntry != null) {
                break;
            }
        }

        // Note that we're not removing inside the for loop above since it would cause a Concurrent
        // exception since we'd modify the map accessed by the iterator.
        if (componentEntry != null) {
            // Release the entry
            releaseInstance(componentEntry);

            // Warn others about it:
            // - fire an unregistration event, to tell the world that this reference is now dead
            // - fire a registration event, to tell the world that it could get a new reference for this component
            // now
            // We need to do this since code holding a reference on the released component may need to know it's
            // been removed and thus discard its own reference to that component and look it up again.
            // Another solution would be to introduce a new event for Component creation/destruction (right now
            // we only send events for Component registration/unregistration).
            if (this.eventManager != null) {
                this.eventManager.notifyComponentUnregistered(componentEntry.descriptor, this);
                this.eventManager.notifyComponentRegistered(componentEntry.descriptor, this);
            }
        }
    }

    private void releaseInstance(ComponentEntry<?> componentEntry) throws ComponentLifecycleException
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

    private void releaseComponentEntry(ComponentEntry<?> componentEntry) throws ComponentLifecycleException
    {
        // clean existing instance
        releaseInstance(componentEntry);
    }

    private void removeComponent(Type role, String hint) throws ComponentLifecycleException
    {
        // Make sure to remove the entry from the map before destroying it to reduce at the minimum the risk of
        // lookupping something invalid
        ComponentEntries<?> entries = this.componentEntries.get(role);

        if (entries != null) {
            ComponentEntry<?> componentEntry = entries.remove(hint != null ? hint : RoleHint.DEFAULT_HINT);

            if (componentEntry != null) {
                ComponentDescriptor<?> oldDescriptor = componentEntry.descriptor;

                // We don't want the component manager to dispose itself just because it's not registered as component*
                // anymore
                if (componentEntry.instance != this) {
                    // clean any resource associated to the component instance and descriptor
                    releaseComponentEntry(componentEntry);
                }

                // Send event about component unregistration
                if (this.eventManager != null && oldDescriptor != null) {
                    this.eventManager.notifyComponentUnregistered(oldDescriptor, this);
                }
            }
        }
    }

    /**
     * Note: This method shouldn't exist but register/unregister methods should throw a
     * {@link ComponentLifecycleException} but that would break backward compatibility to add it.
     */
    private void removeComponentWithoutException(Type role, String hint)
    {
        try {
            removeComponent(role, hint);
        } catch (Exception e) {
            this.logger.warn("Instance released but disposal failed. Some resources may not have been released.", e);
        }
    }

    private void addForDisposalReversedOrder(ComponentEntry<?> componentEntry, List<RoleHint<?>> keys)
    {
        if (!componentEntry.disposing) {
            componentEntry.disposing = true;
            ComponentDescriptor<?> descriptor = componentEntry.descriptor;
            for (ComponentDependency<?> dependency : descriptor.getComponentDependencies()) {
                ComponentEntry<?> dependencyEntry =
                    getComponentEntry(dependency.getRoleType(), dependency.getRoleHint());
                if (dependencyEntry != null) {
                    addForDisposalReversedOrder(dependencyEntry, keys);
                }
            }
            keys.add(new RoleHint<>(descriptor.getRoleType(), descriptor.getRoleHint()));
        }
    }

    private void addForDisposalReversedOrder(List<RoleHint<?>> keys)
    {
        for (ComponentEntries<?> entries : this.componentEntries.values()) {
            entries.getLock().readLock().lock();

            try {
                entries.forEachEntry(e -> addForDisposalReversedOrder(e, keys));
            } finally {
                entries.getLock().readLock().unlock();
            }
        }
    }

    @Override
    public void dispose()
    {
        List<RoleHint<?>> keys = new ArrayList<>(this.componentEntries.size() * 2);

        // Add components based on dependencies relations.
        addForDisposalReversedOrder(keys);
        Collections.reverse(keys);

        // Exclude this component
        RoleHint<ComponentManager> cmRoleHint = new RoleHint<>(ComponentManager.class);
        ComponentEntry<?> cmEntry = getComponentEntry(cmRoleHint.getRoleType(), cmRoleHint.getHint());
        if (cmEntry != null && cmEntry.instance == this) {
            cmEntry.disposing = false;
            keys.remove(cmRoleHint);
        }

        // Sort component by DisposePriority
        Collections.sort(keys, new Comparator<RoleHint<?>>()
        {
            @Override
            public int compare(RoleHint<?> rh1, RoleHint<?> rh2)
            {
                return getPriority(rh1) - getPriority(rh2);
            }

            private int getPriority(RoleHint<?> rh)
            {
                Object instance = getComponentEntry(rh.getRoleType(), rh.getHint()).instance;
                if (instance == null) {
                    // The component has not been instantiated yet. We don't need to dispose it in this case... :)
                    // Return the default priority since it doesn't matter.
                    return DisposePriority.DEFAULT_PRIORITY;
                } else {
                    DisposePriority priorityAnnotation = instance.getClass().getAnnotation(DisposePriority.class);
                    return (priorityAnnotation == null) ? DisposePriority.DEFAULT_PRIORITY : priorityAnnotation.value();
                }
            }
        });

        // Dispose old components
        for (RoleHint<?> key : keys) {
            ComponentEntry<?> componentEntry = getComponentEntry(key.getRoleType(), key.getHint());

            synchronized (componentEntry) {
                Object instance = componentEntry.instance;

                // Protection to prevent infinite recursion in case a component implementation points to this
                // instance.
                if (instance instanceof Disposable && componentEntry.instance != this) {
                    try {
                        SHUTDOWN_LOGGER.debug("Disposing component [{}]...", instance.getClass().getName());
                        ((Disposable) instance).dispose();
                        SHUTDOWN_LOGGER.debug("Component [{}] has been disposed", instance.getClass().getName());
                    } catch (ComponentLifecycleException e) {
                        this.logger.error("Failed to dispose component with role type [{}] and role hint [{}]",
                            componentEntry.descriptor.getRoleType(), componentEntry.descriptor.getRoleHint(), e);
                    }
                }
            }
        }

        // Remove disposed components from the map. Doing it in two steps to give as many chances as possible to the
        // components that have to use a component already disposed (usually because it dynamically requires it and
        // there is no way for the ComponentManager to know that dependency).
        for (RoleHint<?> key : keys) {
            this.componentEntries.get(key.getRoleType()).remove(key.getHint());
        }
    }
}
