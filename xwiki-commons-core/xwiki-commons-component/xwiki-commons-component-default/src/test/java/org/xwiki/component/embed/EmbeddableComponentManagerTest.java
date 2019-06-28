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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.xwiki.component.annotation.DisposePriority;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.descriptor.DefaultComponentDependency;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.util.DefaultParameterizedType;

/**
 * Unit tests for {@link EmbeddableComponentManager}.
 *
 * @version $Id$
 * @since 2.0M1
 */
public class EmbeddableComponentManagerTest
{
    public static interface Role
    {
    }

    public static class RoleImpl implements Role
    {
    }

    public static class OtherRoleImpl implements Role
    {
    }

    private static String lastDisposedComponent;

    public static class InitializableRoleImpl implements Role, Initializable
    {
        private boolean initialized = false;

        @Override
        public void initialize() throws InitializationException
        {
            this.initialized = true;
        }

        public boolean isInitialized()
        {
            return this.initialized;
        }
    }

    public static class DisposableRoleImpl implements Role, Disposable
    {
        private boolean finalized = false;

        @Override
        public void dispose()
        {
            this.finalized = true;
            lastDisposedComponent = "DisposableRoleImpl";
        }

        public boolean isFinalized()
        {
            return this.finalized;
        }
    }

    @DisposePriority(2000)
    public static class DisposableWithPriorityRoleImpl implements Role, Disposable
    {
        private boolean finalized = false;

        @Override
        public void dispose()
        {
            this.finalized = true;
            lastDisposedComponent = "DisposableWithPriorityRoleImpl";
        }

        public boolean isFinalized()
        {
            return this.finalized;
        }
    }

    public static class LoggingRoleImpl implements Role
    {
        private Logger logger;

        public Logger getLogger()
        {
            return this.logger;
        }
    }

    @Test
    public void lookupThisComponentManager() throws ComponentLookupException
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        assertSame(ecm.getInstance(ComponentManager.class), ecm);
    }

    @Test
    public void getComponentDescriptorList() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> d1 = new DefaultComponentDescriptor<>();
        d1.setRoleType(Role.class);
        d1.setRoleHint("hint1");
        ecm.registerComponent(d1);

        DefaultComponentDescriptor<Role> d2 = new DefaultComponentDescriptor<>();
        d2.setRoleType(Role.class);
        d2.setRoleHint("hint2");
        ecm.registerComponent(d2);

        List<ComponentDescriptor<Role>> cds = ecm.getComponentDescriptorList(Role.class);
        assertEquals(2, cds.size());
        assertTrue(cds.contains(d1));
        assertTrue(cds.contains(d2));
    }

    @Test
    public void getComponentDescriptorListInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager());

        List<ComponentDescriptor<Role>> cds = ecm.getComponentDescriptorList((Type) Role.class);
        assertEquals(1, cds.size());
    }

    @Test
    public void getComponentDescriptorInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager("somehint"));

        ComponentDescriptor<Role> cd = ecm.getComponentDescriptor(Role.class, "somehint");
        assertNotNull(cd);
        assertEquals(RoleImpl.class, cd.getImplementation());
    }

    @Test
    public void getComponentDescriptorWhenSomeComponentsInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager());

        // Register a component with the same Role and Hint as in the parent
        DefaultComponentDescriptor<Role> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(Role.class);
        cd1.setImplementation(RoleImpl.class);
        Role roleImpl = new RoleImpl();
        ecm.registerComponent(cd1, roleImpl);

        // Register a component with the same Role as in the parent but with a different hint
        DefaultComponentDescriptor<Role> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(Role.class);
        cd2.setRoleHint("hint");
        cd2.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd2);

        // Verify that the components are found
        // Note: We find only 2 components since 2 components are registered with the same Role and Hint.

        List<ComponentDescriptor<Role>> descriptors = ecm.getComponentDescriptorList(Role.class);
        assertEquals(2, descriptors.size());
    }

    @Test
    public void registerComponentOverExistingOne() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> d1 = new DefaultComponentDescriptor<>();
        d1.setRoleType(Role.class);
        d1.setImplementation(RoleImpl.class);
        ecm.registerComponent(d1);

        Object instance = ecm.getInstance(Role.class);
        assertSame(RoleImpl.class, instance.getClass());

        DefaultComponentDescriptor<Role> d2 = new DefaultComponentDescriptor<>();
        d2.setRoleType(Role.class);
        d2.setImplementation(OtherRoleImpl.class);
        ecm.registerComponent(d2);

        instance = ecm.getInstance(Role.class);
        assertSame(OtherRoleImpl.class, instance.getClass());
    }

    @Test
    public void registerComponentInstance() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> d1 = new DefaultComponentDescriptor<>();
        d1.setRoleType(Role.class);
        d1.setImplementation(RoleImpl.class);
        Role instance = new RoleImpl();
        ecm.registerComponent(d1, instance);

        assertSame(instance, ecm.getInstance(Role.class));
    }

    @Test
    public void unregisterComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> d1 = new DefaultComponentDescriptor<>();
        d1.setRoleType(Role.class);
        d1.setImplementation(RoleImpl.class);
        ecm.registerComponent(d1);

        // Verify that the component is properly registered
        assertSame(RoleImpl.class, ecm.getInstance(Role.class).getClass());

        ecm.unregisterComponent(d1.getRoleType(), d1.getRoleHint());

        // Verify that the component is not registered anymore
        Throwable exception = assertThrows(ComponentLookupException.class, () -> {
            ecm.getInstance(d1.getRoleType());
        });
        // The exception message doesn't matter. All we need to know is that the component descriptor
        // doesn't exist anymore.
    }

    @Test
    public void getInstanceWhenComponentInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager());

        Role instance = ecm.getInstance(Role.class);
        assertNotNull(instance);
    }

    @Test
    public void getInstanceListAndMapWhenSomeComponentsInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager());

        // Register a component with the same Role and Hint as in the parent
        DefaultComponentDescriptor<Role> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(Role.class);
        cd1.setImplementation(RoleImpl.class);
        Role roleImpl = new RoleImpl();
        ecm.registerComponent(cd1, roleImpl);

        // Register a component with the same Role as in the parent but with a different hint
        DefaultComponentDescriptor<Role> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(Role.class);
        cd2.setRoleHint("hint");
        cd2.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd2);

        // Verify that the components are found
        // Note: We find only 2 components since 2 components are registered with the same Role and Hint.

        List<Role> instanceList = ecm.getInstanceList(Role.class);
        assertEquals(2, instanceList.size());
        assertSame(roleImpl, instanceList.get(0));

        Map<String, Role> instances = ecm.getInstanceMap(Role.class);
        assertEquals(2, instances.size());
        assertSame(roleImpl, instances.get("default"));
    }

    @Test
    public void hasComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> d1 = new DefaultComponentDescriptor<>();
        d1.setRoleType(Role.class);
        d1.setRoleHint("default");
        ecm.registerComponent(d1);

        assertTrue(ecm.hasComponent(Role.class));
        assertTrue(ecm.hasComponent(Role.class, "default"));
    }

    @Test
    public void hasComponentWhenComponentInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager());

        assertTrue(ecm.hasComponent(Role.class));
        assertTrue(ecm.hasComponent(Role.class, "default"));
    }

    @Test
    public void loggingInjection() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> d = new DefaultComponentDescriptor<>();
        d.setRoleType(Role.class);
        d.setImplementation(LoggingRoleImpl.class);

        DefaultComponentDependency dependencyDescriptor = new DefaultComponentDependency();
        dependencyDescriptor.setMappingType(Logger.class);
        dependencyDescriptor.setName("logger");

        d.addComponentDependency(dependencyDescriptor);
        ecm.registerComponent(d);

        LoggingRoleImpl impl = ecm.getInstance(Role.class);
        assertNotNull(impl.getLogger());
    }

    private ComponentManager createParentComponentManager() throws Exception
    {
        return createParentComponentManager(null);
    }

    private ComponentManager createParentComponentManager(String hint) throws Exception
    {
        EmbeddableComponentManager parent = new EmbeddableComponentManager();
        DefaultComponentDescriptor<Role> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(Role.class);
        cd.setImplementation(RoleImpl.class);
        if (hint != null) {
            cd.setRoleHint(hint);
        }
        parent.registerComponent(cd);
        return parent;
    }

    @Test
    public void registerInitializableComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(Role.class);
        cd.setImplementation(InitializableRoleImpl.class);
        ecm.registerComponent(cd);
        InitializableRoleImpl instance = ecm.getInstance(Role.class);

        assertTrue(instance.isInitialized());
    }

    @Test
    public void unregisterDisposableSingletonComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(Role.class);
        cd.setImplementation(DisposableRoleImpl.class);
        cd.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);

        ecm.registerComponent(cd);
        DisposableRoleImpl instance = ecm.getInstance(Role.class);

        assertFalse(instance.isFinalized());

        ecm.unregisterComponent(cd.getRoleType(), cd.getRoleHint());

        assertTrue(instance.isFinalized());
    }

    @Test
    public void unregisterDisposableSingletonComponentWithInstance()
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(Role.class);
        cd.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);

        DisposableRoleImpl instance = new DisposableRoleImpl();
        ecm.registerComponent(cd, instance);

        assertFalse(instance.isFinalized());

        ecm.unregisterComponent(cd.getRoleType(), cd.getRoleHint());

        assertTrue(instance.isFinalized());
    }

    @Test
    public void release() throws Exception
    {
        final EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        final DefaultComponentDescriptor<Role> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(Role.class);
        cd.setImplementation(RoleImpl.class);
        Role roleImpl = new RoleImpl();
        ecm.registerComponent(cd, roleImpl);

        final ComponentEventManager cem = mock(ComponentEventManager.class);
        ecm.setComponentEventManager(cem);

        ecm.release(roleImpl);

        verify(cem).notifyComponentUnregistered(cd, ecm);
        verify(cem).notifyComponentRegistered(cd, ecm);

        assertNotNull(ecm.getInstance(Role.class));
        assertNotSame(roleImpl, ecm.getInstance(Role.class));
    }

    @Test
    public void releaseDisposableComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(Role.class);
        cd.setImplementation(DisposableRoleImpl.class);
        cd.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);

        ecm.registerComponent(cd);
        DisposableRoleImpl instance = ecm.getInstance(Role.class);

        assertFalse(instance.isFinalized());

        ecm.release(instance);

        assertTrue(instance.isFinalized());
    }

    @Test
    public void registerComponentNotification() throws Exception
    {
        final EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        final DefaultComponentDescriptor<Role> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(Role.class);
        cd.setImplementation(RoleImpl.class);

        final ComponentEventManager cem = mock(ComponentEventManager.class);
        ecm.setComponentEventManager(cem);

        ecm.registerComponent(cd);

        verify(cem).notifyComponentRegistered(cd, ecm);
    }

    @Test
    public void unregisterComponentNotification() throws Exception
    {
        final EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        final DefaultComponentDescriptor<Role> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(Role.class);
        cd.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd);

        final ComponentEventManager cem = mock(ComponentEventManager.class);
        ecm.setComponentEventManager(cem);

        ecm.unregisterComponent(cd.getRoleType(), cd.getRoleHint());

        verify(cem).notifyComponentUnregistered(cd, ecm);
    }

    @Test
    public void registerComponentNotificationOnSecondRegistration() throws Exception
    {
        final EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        final DefaultComponentDescriptor<Role> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(Role.class);
        cd1.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd1);

        final DefaultComponentDescriptor<Role> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(Role.class);
        cd2.setImplementation(OtherRoleImpl.class);

        final ComponentEventManager cem = mock(ComponentEventManager.class);
        ecm.setComponentEventManager(cem);

        ecm.registerComponent(cd2);

        verify(cem).notifyComponentUnregistered(cd1, ecm);
        verify(cem).notifyComponentRegistered(cd2, ecm);
    }

    @Test
    public void dispose() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        // Register 2 components:
        // - a first one using a low dispose priority
        // - a second one using a default dispose priority

        // First component
        DefaultComponentDescriptor<Role> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(Role.class);
        cd1.setRoleHint("instance1");
        cd1.setImplementation(DisposableWithPriorityRoleImpl.class);
        cd1.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);
        ecm.registerComponent(cd1);
        DisposableWithPriorityRoleImpl instance1 = ecm.getInstance(Role.class, "instance1");

        // Second component
        DefaultComponentDescriptor<Role> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(Role.class);
        cd2.setRoleHint("instance2");
        cd2.setImplementation(DisposableRoleImpl.class);
        cd2.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);
        ecm.registerComponent(cd2);
        DisposableRoleImpl instance2 = ecm.getInstance(Role.class, "instance2");

        assertFalse(instance1.isFinalized());
        assertFalse(instance2.isFinalized());

        ecm.dispose();

        assertTrue(instance1.isFinalized());
        assertTrue(instance2.isFinalized());

        assertNull(ecm.getComponentDescriptor(Role.class, "instance1"));
        assertNull(ecm.getComponentDescriptor(Role.class, "instance2"));
        assertNotNull(ecm.getComponentDescriptor(ComponentManager.class, "default"));

        // Verify that dispose() has been called in the right order.
        // We check that the last component which had its dispose() called is DisposableWithPriorityRoleImpl since
        // it has the lowest priority.
        assertEquals("DisposableWithPriorityRoleImpl", lastDisposedComponent);
    }

    @Test
    public void diposeWhenImplementationIsECM()
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<ComponentManager> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(ComponentManager.class);
        cd.setRoleHint("hint");
        cd.setImplementation(EmbeddableComponentManager.class);
        cd.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);
        ecm.registerComponent(cd, ecm);

        // If the test fails, the following line will generate a StackOverflowException
        ecm.dispose();
    }

    public static class ComponentDescriptorRoleImpl implements Role
    {
        private ComponentDescriptor<ComponentDescriptorRoleImpl> descriptor;

        public ComponentDescriptor<ComponentDescriptorRoleImpl> getComponentDescriptor()
        {
            return this.descriptor;
        }
    }

    @Test
    public void componentDescriptorInjection() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<Role> d = new DefaultComponentDescriptor<>();
        d.setRoleType(Role.class);
        d.setImplementation(ComponentDescriptorRoleImpl.class);

        DefaultComponentDependency dependencyDescriptor = new DefaultComponentDependency();
        dependencyDescriptor.setRoleType(
            new DefaultParameterizedType(null, ComponentDescriptor.class, ComponentDescriptorRoleImpl.class));
        dependencyDescriptor.setName("descriptor");

        d.addComponentDependency(dependencyDescriptor);
        ecm.registerComponent(d);

        ComponentDescriptorRoleImpl impl = ecm.getInstance(Role.class);
        assertNotNull(impl.getComponentDescriptor());
    }
}
