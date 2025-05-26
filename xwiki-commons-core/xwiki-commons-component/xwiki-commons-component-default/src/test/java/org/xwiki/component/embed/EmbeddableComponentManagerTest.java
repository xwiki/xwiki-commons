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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import jakarta.inject.Provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.xwiki.component.annotation.DisposePriority;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.descriptor.DefaultComponentDependency;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;

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

/**
 * Unit tests for {@link EmbeddableComponentManager}.
 *
 * @version $Id$
 * @since 2.0M1
 */
// This class needs to remain public because some interfaces are reused in other tests
public class EmbeddableComponentManagerTest
{
    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.ERROR);

    public interface TestRole
    {
    }

    public static class CycleRoleImpl implements TestRole
    {
        @Inject
        private TestRole recursion;
    }

    public static class RoleImpl implements TestRole
    {
    }

    public static class OtherRoleImpl implements TestRole
    {
    }

    public static class FailingRoleImpl implements TestRole, Initializable
    {
        @Override
        public void initialize() throws InitializationException
        {
            throw new InitializationException("fail");
        }
    }

    private static String lastDisposedComponent;

    public static class InitializableRoleImpl implements TestRole, Initializable
    {
        private boolean initialized = false;

        @Override
        public void initialize()
        {
            this.initialized = true;
        }

        public boolean isInitialized()
        {
            return this.initialized;
        }
    }

    public static class DisposableRoleImpl implements TestRole, Disposable
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
    public static class DisposableWithPriorityRoleImpl implements TestRole, Disposable
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

    public static class LoggingRoleImpl implements TestRole
    {
        private Logger logger;

        public Logger getLogger()
        {
            return this.logger;
        }
    }

    @Test
    void lookupThisComponentManager() throws ComponentLookupException
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        assertSame(ecm.getInstance(ComponentManager.class), ecm);
    }

    @Test
    void getComponentDescriptorList() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> d1 = new DefaultComponentDescriptor<>();
        d1.setRoleType(TestRole.class);
        d1.setRoleHint("hint1");
        ecm.registerComponent(d1);

        DefaultComponentDescriptor<TestRole> d2 = new DefaultComponentDescriptor<>();
        d2.setRoleType(TestRole.class);
        d2.setRoleHint("hint2");
        ecm.registerComponent(d2);

        List<ComponentDescriptor<TestRole>> cds = ecm.getComponentDescriptorList(TestRole.class);
        assertEquals(2, cds.size());
        assertTrue(cds.contains(d1));
        assertTrue(cds.contains(d2));
    }

    @Test
    void getComponentDescriptorListInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager());

        List<ComponentDescriptor<TestRole>> cds = ecm.getComponentDescriptorList((Type) TestRole.class);
        assertEquals(1, cds.size());
    }

    @Test
    void getComponentDescriptorInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager("somehint"));

        ComponentDescriptor<TestRole> cd = ecm.getComponentDescriptor(TestRole.class, "somehint");
        assertNotNull(cd);
        assertEquals(RoleImpl.class, cd.getImplementation());
    }

    @Test
    void getComponentDescriptorWhenSomeComponentsInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager());

        // Register a component with the same Role and Hint as in the parent
        DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setImplementation(RoleImpl.class);
        TestRole roleImpl = new RoleImpl();
        ecm.registerComponent(cd1, roleImpl);

        // Register a component with the same Role as in the parent but with a different hint
        DefaultComponentDescriptor<TestRole> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(TestRole.class);
        cd2.setRoleHint("hint");
        cd2.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd2);

        // Verify that the components are found
        // Note: We find only 2 components since 2 components are registered with the same Role and Hint.

        List<ComponentDescriptor<TestRole>> descriptors = ecm.getComponentDescriptorList(TestRole.class);
        assertEquals(2, descriptors.size());
    }

    @Test
    void registerComponentOverExistingOne() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> d1 = new DefaultComponentDescriptor<>();
        d1.setRoleType(TestRole.class);
        d1.setImplementation(RoleImpl.class);
        ecm.registerComponent(d1);

        Object instance = ecm.getInstance(TestRole.class);
        assertSame(RoleImpl.class, instance.getClass());

        DefaultComponentDescriptor<TestRole> d2 = new DefaultComponentDescriptor<>();
        d2.setRoleType(TestRole.class);
        d2.setImplementation(OtherRoleImpl.class);
        ecm.registerComponent(d2);

        instance = ecm.getInstance(TestRole.class);
        assertSame(OtherRoleImpl.class, instance.getClass());
    }

    @Test
    void registerComponentInstance() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> d1 = new DefaultComponentDescriptor<>();
        d1.setRoleType(TestRole.class);
        d1.setImplementation(RoleImpl.class);
        TestRole instance = new RoleImpl();
        ecm.registerComponent(d1, instance);

        assertSame(instance, ecm.getInstance(TestRole.class));
    }

    @Test
    void unregisterComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> d1 = new DefaultComponentDescriptor<>();
        d1.setRoleType(TestRole.class);
        d1.setImplementation(RoleImpl.class);
        ecm.registerComponent(d1);

        // Verify that the component is properly registered
        assertSame(RoleImpl.class, ecm.getInstance(TestRole.class).getClass());

        ecm.unregisterComponent(d1.getRoleType(), d1.getRoleHint());

        // Verify that the component is not registered anymore
        Throwable exception = assertThrows(ComponentLookupException.class, () -> {
            ecm.getInstance(d1.getRoleType());
        });
        // The exception message doesn't matter. All we need to know is that the component descriptor
        // doesn't exist anymore.
    }

    @Test
    void getInstanceWhenComponentInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager());

        TestRole instance = ecm.getInstance(TestRole.class);
        assertNotNull(instance);
    }

    @Test
    void getInstanceListAndMapWhenSameTypeAndHintAndHintPriorityThanParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager());

        // Register a component with the same type, hint and hint priority as in the parent
        DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setImplementation(RoleImpl.class);
        TestRole roleInstance = new RoleImpl();
        ecm.registerComponent(cd1, roleInstance);

        List<TestRole> instanceList = ecm.getInstanceList(TestRole.class);
        assertEquals(List.of(roleInstance), instanceList);

        Map<String, TestRole> instances = ecm.getInstanceMap(TestRole.class);
        assertEquals(1, instances.size());
        assertSame(roleInstance, instances.get("default"));
    }

    @Test
    void getInstanceListAndMapWhenSameTypeAndHintAndLowerHintPriorityThanParent() throws Exception
    {
        ComponentManager parentcm = createParentComponentManager();
        TestRole parentInstance = parentcm.getInstance(TestRole.class);

        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(parentcm);

        // Register a component with the same type, hint as in the parent but lower hint priority
        DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setImplementation(RoleImpl.class);
        cd1.setRoleHintPriority(ComponentDescriptor.DEFAULT_PRIORITY + 1);
        TestRole roleInstance = new RoleImpl();
        ecm.registerComponent(cd1, roleInstance);

        List<TestRole> instanceList = ecm.getInstanceList(TestRole.class);
        assertEquals(List.of(parentInstance), instanceList);

        Map<String, TestRole> instances = ecm.getInstanceMap(TestRole.class);
        assertEquals(1, instances.size());
        assertSame(parentInstance, instances.get("default"));
    }

    @Test
    void getInstanceListAndMapWhenSameTypeAndHintPriorityThanParent() throws Exception
    {
        ComponentManager parentcm = createParentComponentManager();
        TestRole parentInstance = parentcm.getInstance(TestRole.class);

        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(parentcm);

        // Register a component with the same type, hint as in the parent but lower hint priority
        DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setRoleHint("hint1");
        cd1.setImplementation(RoleImpl.class);
        TestRole roleInstance = new RoleImpl();
        ecm.registerComponent(cd1, roleInstance);

        List<TestRole> instanceList = ecm.getInstanceList(TestRole.class);
        assertEquals(List.of(roleInstance, parentInstance), instanceList);

        Map<String, TestRole> instances = ecm.getInstanceMap(TestRole.class);
        assertEquals(2, instances.size());
        assertSame(roleInstance, instances.get("hint1"));
        assertSame(parentInstance, instances.get("default"));
    }

    @Test
    void getInstanceListAndMapWhenSameTypeAndLowerHintPriorityThanParent() throws Exception
    {
        ComponentManager parentcm = createParentComponentManager();
        TestRole parentInstance = parentcm.getInstance(TestRole.class);

        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(parentcm);

        // Register a component with the same type, hint as in the parent but lower hint priority
        DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setRoleHint("hint1");
        cd1.setImplementation(RoleImpl.class);
        cd1.setRoleTypePriority(ComponentDescriptor.DEFAULT_PRIORITY + 1);
        TestRole roleInstance = new RoleImpl();
        ecm.registerComponent(cd1, roleInstance);

        List<TestRole> instanceList = ecm.getInstanceList(TestRole.class);
        assertEquals(List.of(parentInstance, roleInstance), instanceList);

        Map<String, TestRole> instances = ecm.getInstanceMap(TestRole.class);
        assertEquals(2, instances.size());
        assertSame(roleInstance, instances.get("hint1"));
        assertSame(parentInstance, instances.get("default"));
    }

    @Test
    void getInstanceListAndMapWithoutTypePriorities() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setRoleHint("hint1");
        cd1.setImplementation(RoleImpl.class);
        TestRole roleImpl1 = new RoleImpl();
        ecm.registerComponent(cd1, roleImpl1);

        DefaultComponentDescriptor<TestRole> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(TestRole.class);
        cd2.setRoleHint("hint2");
        cd2.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd2);
        TestRole roleImpl2 = new RoleImpl();
        ecm.registerComponent(cd2, roleImpl2);

        DefaultComponentDescriptor<TestRole> cd3 = new DefaultComponentDescriptor<>();
        cd3.setRoleType(TestRole.class);
        cd3.setRoleHint("hint3");
        cd3.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd3);
        TestRole roleImpl3 = new RoleImpl();
        ecm.registerComponent(cd3, roleImpl3);

        List<TestRole> instanceList = ecm.getInstanceList(TestRole.class);
        assertEquals(List.of(roleImpl1, roleImpl2, roleImpl3), instanceList);

        Map<String, TestRole> instances = ecm.getInstanceMap(TestRole.class);
        assertEquals(3, instances.size());
        assertSame(roleImpl1, instances.get("hint1"));
        assertSame(roleImpl2, instances.get("hint2"));
        assertSame(roleImpl3, instances.get("hint3"));
    }

    @Test
    void getInstanceListAndMapWithTypePriorities() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setRoleHint("hint1");
        cd1.setRoleTypePriority(3);
        cd1.setImplementation(RoleImpl.class);
        TestRole roleImpl1 = new RoleImpl();
        ecm.registerComponent(cd1, roleImpl1);

        DefaultComponentDescriptor<TestRole> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(TestRole.class);
        cd2.setRoleHint("hint2");
        cd2.setRoleTypePriority(1);
        cd2.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd2);
        TestRole roleImpl2 = new RoleImpl();
        ecm.registerComponent(cd2, roleImpl2);

        DefaultComponentDescriptor<TestRole> cd3 = new DefaultComponentDescriptor<>();
        cd3.setRoleType(TestRole.class);
        cd3.setRoleHint("hint3");
        cd3.setRoleTypePriority(2);
        cd3.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd3);
        TestRole roleImpl3 = new RoleImpl();
        ecm.registerComponent(cd3, roleImpl3);

        List<TestRole> instanceList = ecm.getInstanceList(TestRole.class);
        assertEquals(List.of(roleImpl2, roleImpl3, roleImpl1), instanceList);

        Map<String, TestRole> instances = ecm.getInstanceMap(TestRole.class);
        assertEquals(3, instances.size());
        assertSame(roleImpl2, instances.get("hint2"));
        assertSame(roleImpl3, instances.get("hint3"));
        assertSame(roleImpl1, instances.get("hint1"));
    }

    @Test
    void getInstanceListAndMapWhenFailingComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setRoleHint("hint1");
        cd1.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd1);

        List<TestRole> instanceList = ecm.getInstanceList(TestRole.class);
        assertEquals(1, instanceList.size());
        assertSame(RoleImpl.class, instanceList.get(0).getClass());

        Map<String, TestRole> instanceMap = ecm.getInstanceMap(TestRole.class);
        assertEquals(1, instanceMap.size());
        assertSame(RoleImpl.class, instanceMap.get("hint1").getClass());

        // Register a component which fail to initialize but is not mandatory
        DefaultComponentDescriptor<TestRole> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(TestRole.class);
        cd2.setRoleHint("hint2");
        cd2.setImplementation(FailingRoleImpl.class);
        ecm.registerComponent(cd2);

        ComponentLookupException exception =
            assertThrows(ComponentLookupException.class, () -> ecm.getInstance(TestRole.class, "hint2"));
        assertEquals("fail", exception.getCause().getMessage());

        instanceList = ecm.getInstanceList(TestRole.class);
        assertEquals(1, instanceList.size());
        assertSame(RoleImpl.class, instanceList.get(0).getClass());

        assertEquals("Failed to lookup component with"
            + " type [interface org.xwiki.component.embed.EmbeddableComponentManagerTest$TestRole] and hint [hint2]",
            this.logCapture.getMessage(0));

        instanceMap = ecm.getInstanceMap(TestRole.class);
        assertEquals(1, instanceMap.size());
        assertSame(RoleImpl.class, instanceMap.get("hint1").getClass());

        assertEquals("Failed to lookup component with"
            + " type [interface org.xwiki.component.embed.EmbeddableComponentManagerTest$TestRole] and hint [hint2]",
            this.logCapture.getMessage(1));

        // Register a component which fail to initialize and is mandatory
        DefaultComponentDescriptor<TestRole> cd3 = new DefaultComponentDescriptor<>();
        cd3.setRoleType(TestRole.class);
        cd3.setRoleHint("hint3");
        cd3.setMandatory(true);
        cd3.setImplementation(FailingRoleImpl.class);
        ecm.registerComponent(cd3);

        exception = assertThrows(ComponentLookupException.class, () -> ecm.getInstanceList(TestRole.class));
        assertEquals("fail", exception.getCause().getMessage());

        assertEquals("Failed to lookup component with"
            + " type [interface org.xwiki.component.embed.EmbeddableComponentManagerTest$TestRole] and hint [hint2]",
            this.logCapture.getMessage(2));

        exception = assertThrows(ComponentLookupException.class, () -> ecm.getInstanceMap(TestRole.class));
        assertEquals("fail", exception.getCause().getMessage());

        assertEquals("Failed to lookup component with"
            + " type [interface org.xwiki.component.embed.EmbeddableComponentManagerTest$TestRole] and hint [hint2]",
            this.logCapture.getMessage(3));
    }

    @Test
    void hasComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> d1 = new DefaultComponentDescriptor<>();
        d1.setRoleType(TestRole.class);
        d1.setRoleHint("default");
        ecm.registerComponent(d1);

        assertTrue(ecm.hasComponent(TestRole.class));
        assertTrue(ecm.hasComponent(TestRole.class, "default"));
    }

    @Test
    void hasComponentWhenComponentInParent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.setParent(createParentComponentManager());

        assertTrue(ecm.hasComponent(TestRole.class));
        assertTrue(ecm.hasComponent(TestRole.class, "default"));
    }

    @Test
    void loggingInjection() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> d = new DefaultComponentDescriptor<>();
        d.setRoleType(TestRole.class);
        d.setImplementation(LoggingRoleImpl.class);

        DefaultComponentDependency dependencyDescriptor = new DefaultComponentDependency();
        dependencyDescriptor.setMappingType(Logger.class);
        dependencyDescriptor.setName("logger");

        d.addComponentDependency(dependencyDescriptor);
        ecm.registerComponent(d);

        LoggingRoleImpl impl = ecm.getInstance(TestRole.class);
        assertNotNull(impl.getLogger());
    }

    private ComponentManager createParentComponentManager() throws Exception
    {
        return createParentComponentManager(null);
    }

    private ComponentManager createParentComponentManager(String hint) throws Exception
    {
        EmbeddableComponentManager parent = new EmbeddableComponentManager();
        DefaultComponentDescriptor<TestRole> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(TestRole.class);
        cd.setImplementation(RoleImpl.class);
        if (hint != null) {
            cd.setRoleHint(hint);
        }
        parent.registerComponent(cd);
        return parent;
    }

    @Test
    void registerInitializableComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(TestRole.class);
        cd.setImplementation(InitializableRoleImpl.class);
        ecm.registerComponent(cd);
        InitializableRoleImpl instance = ecm.getInstance(TestRole.class);

        assertTrue(instance.isInitialized());
    }

    @Test
    void detectCycle() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(TestRole.class);
        cd.setImplementation(CycleRoleImpl.class);

        DefaultComponentDependency<TestRole> dependency = new DefaultComponentDependency<>();
        dependency.setRoleType(TestRole.class);
        dependency.setName("recursion");
        cd.addComponentDependency(dependency);

        ecm.registerComponent(cd);

        ComponentLookupException exception =
            assertThrows(ComponentLookupException.class, () -> ecm.getInstance(TestRole.class));
        assertEquals("Detected component construction cycle for component "
                + "[interface org.xwiki.component.embed.EmbeddableComponentManagerTest$TestRole] of hint [default].",
            exception.getCause().getCause().getMessage());
    }

    @Test
    void unregisterDisposableSingletonComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(TestRole.class);
        cd.setImplementation(DisposableRoleImpl.class);
        cd.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);

        ecm.registerComponent(cd);
        DisposableRoleImpl instance = ecm.getInstance(TestRole.class);

        assertFalse(instance.isFinalized());

        ecm.unregisterComponent(cd.getRoleType(), cd.getRoleHint());

        assertTrue(instance.isFinalized());
    }

    @Test
    void unregisterDisposableSingletonComponentWithInstance()
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(TestRole.class);
        cd.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);

        DisposableRoleImpl instance = new DisposableRoleImpl();
        ecm.registerComponent(cd, instance);

        assertFalse(instance.isFinalized());

        ecm.unregisterComponent(cd.getRoleType(), cd.getRoleHint());

        assertTrue(instance.isFinalized());
    }

    @Test
    void release() throws Exception
    {
        final EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        final DefaultComponentDescriptor<TestRole> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(TestRole.class);
        cd.setImplementation(RoleImpl.class);
        TestRole roleImpl = new RoleImpl();
        ecm.registerComponent(cd, roleImpl);

        final ComponentEventManager cem = mock(ComponentEventManager.class);
        ecm.setComponentEventManager(cem);

        ecm.release(roleImpl);

        verify(cem).notifyComponentUnregistered(cd, ecm);
        verify(cem).notifyComponentRegistered(cd, ecm);

        assertNotNull(ecm.getInstance(TestRole.class));
        assertNotSame(roleImpl, ecm.getInstance(TestRole.class));
    }

    @Test
    void releaseDisposableComponent() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(TestRole.class);
        cd.setImplementation(DisposableRoleImpl.class);
        cd.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);

        ecm.registerComponent(cd);
        DisposableRoleImpl instance = ecm.getInstance(TestRole.class);

        assertFalse(instance.isFinalized());

        ecm.release(instance);

        assertTrue(instance.isFinalized());
    }

    @Test
    void registerComponentNotification() throws Exception
    {
        final EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        final DefaultComponentDescriptor<TestRole> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(TestRole.class);
        cd.setImplementation(RoleImpl.class);

        final ComponentEventManager cem = mock(ComponentEventManager.class);
        ecm.setComponentEventManager(cem);

        ecm.registerComponent(cd);

        verify(cem).notifyComponentRegistered(cd, ecm);
    }

    @Test
    void unregisterComponentNotification() throws Exception
    {
        final EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        final DefaultComponentDescriptor<TestRole> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(TestRole.class);
        cd.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd);

        final ComponentEventManager cem = mock(ComponentEventManager.class);
        ecm.setComponentEventManager(cem);

        ecm.unregisterComponent(cd.getRoleType(), cd.getRoleHint());

        verify(cem).notifyComponentUnregistered(cd, ecm);
    }

    @Test
    void registerComponentNotificationOnSecondRegistration() throws Exception
    {
        final EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        final DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setImplementation(RoleImpl.class);
        ecm.registerComponent(cd1);

        final DefaultComponentDescriptor<TestRole> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(TestRole.class);
        cd2.setImplementation(OtherRoleImpl.class);

        final ComponentEventManager cem = mock(ComponentEventManager.class);
        ecm.setComponentEventManager(cem);

        ecm.registerComponent(cd2);

        verify(cem).notifyComponentUnregistered(cd1, ecm);
        verify(cem).notifyComponentRegistered(cd2, ecm);
    }

    @Test
    void dispose() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        // Register 2 components:
        // - a first one using a low dispose priority
        // - a second one using a default dispose priority

        // First component
        DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setRoleHint("instance1");
        cd1.setImplementation(DisposableWithPriorityRoleImpl.class);
        cd1.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);
        ecm.registerComponent(cd1);
        DisposableWithPriorityRoleImpl instance1 = ecm.getInstance(TestRole.class, "instance1");

        // Second component
        DefaultComponentDescriptor<TestRole> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(TestRole.class);
        cd2.setRoleHint("instance2");
        cd2.setImplementation(DisposableRoleImpl.class);
        cd2.setInstantiationStrategy(ComponentInstantiationStrategy.SINGLETON);
        ecm.registerComponent(cd2);
        DisposableRoleImpl instance2 = ecm.getInstance(TestRole.class, "instance2");

        assertFalse(instance1.isFinalized());
        assertFalse(instance2.isFinalized());

        ecm.dispose();

        assertTrue(instance1.isFinalized());
        assertTrue(instance2.isFinalized());

        assertNull(ecm.getComponentDescriptor(TestRole.class, "instance1"));
        assertNull(ecm.getComponentDescriptor(TestRole.class, "instance2"));
        assertNotNull(ecm.getComponentDescriptor(ComponentManager.class, "default"));

        // Verify that dispose() has been called in the right order.
        // We check that the last component which had its dispose() called is DisposableWithPriorityRoleImpl since
        // it has the lowest priority.
        assertEquals("DisposableWithPriorityRoleImpl", lastDisposedComponent);
    }

    @Test
    void diposeWhenImplementationIsECM()
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

    public static class ComponentDescriptorRoleImpl implements TestRole
    {
        private ComponentDescriptor<ComponentDescriptorRoleImpl> descriptor;

        public ComponentDescriptor<ComponentDescriptorRoleImpl> getComponentDescriptor()
        {
            return this.descriptor;
        }
    }

    @Test
    void componentDescriptorInjection() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        DefaultComponentDescriptor<TestRole> d = new DefaultComponentDescriptor<>();
        d.setRoleType(TestRole.class);
        d.setImplementation(ComponentDescriptorRoleImpl.class);

        DefaultComponentDependency dependencyDescriptor = new DefaultComponentDependency();
        dependencyDescriptor.setRoleType(
            new DefaultParameterizedType(null, ComponentDescriptor.class, ComponentDescriptorRoleImpl.class));
        dependencyDescriptor.setName("descriptor");

        d.addComponentDependency(dependencyDescriptor);
        ecm.registerComponent(d);

        ComponentDescriptorRoleImpl impl = ecm.getInstance(TestRole.class);
        assertNotNull(impl.getComponentDescriptor());
    }

    @Test
    void constructorWithNameSpace()
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager("namespace");
        assertEquals("namespace", ecm.getNamespace());
    }

    public static class JakartaProvider implements Provider<String>
    {
        @Override
        public String get()
        {
            return "jakarta";
        }
    }

    public static class JavaXProvider implements javax.inject.Provider<String>
    {
        @Override
        public String get()
        {
            return "javax";
        }
    }

    public static class ComponentWithJakartaProvider
    {
        Provider<String> provider;

        public String get()
        {
            return this.provider.get();
        }
    }

    public static class ComponentWithJavaXProvider
    {
        javax.inject.Provider<String> provider;

        public String get()
        {
            return this.provider.get();
        }
    }

    private static <T> DefaultComponentDependency<T> dependency(Type roleType, String roleHint, String field)
    {
        DefaultComponentDependency<T> dependency = new DefaultComponentDependency<>();
        dependency.setRoleType(roleType);
        dependency.setRoleHint(roleHint);
        dependency.setName(field);

        return dependency;
    }

    private <T> void registerComponent(EmbeddableComponentManager ecm, Type roleType, String roleHint,
        Class<? extends T> implementation, ComponentDependency<?>... dependencies) throws ComponentRepositoryException
    {
        DefaultComponentDescriptor<T> djakarta = new DefaultComponentDescriptor<>();
        djakarta.setRoleType(roleType);
        djakarta.setRoleHint(roleHint);
        djakarta.setImplementation(implementation);

        for (ComponentDependency<?> dependency : dependencies) {
            djakarta.addComponentDependency(dependency);
        }

        ecm.registerComponent(djakarta);
    }

    @Test
    void getInstanceJakartaJavaxProvider() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        ParameterizedType jakartaRoleType = new DefaultParameterizedType(null, Provider.class, String.class);
        registerComponent(ecm, jakartaRoleType, "jakarta", JakartaProvider.class);

        ParameterizedType javaxRoleType = new DefaultParameterizedType(null, javax.inject.Provider.class, String.class);
        registerComponent(ecm, javaxRoleType, "javax", JavaXProvider.class);

        assertEquals("jakarta", ecm.<Provider<String>>getInstance(jakartaRoleType, "jakarta").get());
        assertEquals("javax", ecm.<Provider<String>>getInstance(jakartaRoleType, "javax").get());

        assertEquals("javax", ecm.<javax.inject.Provider<String>>getInstance(javaxRoleType, "javax").get());
        assertEquals("jakarta", ecm.<javax.inject.Provider<String>>getInstance(javaxRoleType, "jakarta").get());
    }

    @Test
    void injectJakartaJavaxProvider() throws Exception
    {
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();

        ParameterizedType jakartaRoleType = new DefaultParameterizedType(null, Provider.class, String.class);
        registerComponent(ecm, jakartaRoleType, "jakarta", JakartaProvider.class);

        ParameterizedType javaxRoleType = new DefaultParameterizedType(null, javax.inject.Provider.class, String.class);
        registerComponent(ecm, javaxRoleType, "javax", JavaXProvider.class);

        registerComponent(ecm, ComponentWithJakartaProvider.class, "jakarta", ComponentWithJakartaProvider.class,
            dependency(jakartaRoleType, "jakarta", "provider"));
        registerComponent(ecm, ComponentWithJakartaProvider.class, "javax", ComponentWithJakartaProvider.class,
            dependency(jakartaRoleType, "javax", "provider"));

        registerComponent(ecm, ComponentWithJavaXProvider.class, "jakarta", ComponentWithJavaXProvider.class,
            dependency(javaxRoleType, "jakarta", "provider"));
        registerComponent(ecm, ComponentWithJavaXProvider.class, "javax", ComponentWithJavaXProvider.class,
            dependency(javaxRoleType, "javax", "provider"));

        assertEquals("jakarta",
            ecm.<ComponentWithJakartaProvider>getInstance(ComponentWithJakartaProvider.class, "jakarta").get());
        assertEquals("javax",
            ecm.<ComponentWithJakartaProvider>getInstance(ComponentWithJakartaProvider.class, "javax").get());

        assertEquals("jakarta",
            ecm.<ComponentWithJavaXProvider>getInstance(ComponentWithJavaXProvider.class, "jakarta").get());
        assertEquals("javax",
            ecm.<ComponentWithJavaXProvider>getInstance(ComponentWithJavaXProvider.class, "javax").get());
    }
}
