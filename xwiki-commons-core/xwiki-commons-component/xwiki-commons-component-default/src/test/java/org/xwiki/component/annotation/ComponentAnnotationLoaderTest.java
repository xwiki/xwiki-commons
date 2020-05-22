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

import java.lang.reflect.Type;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.xwiki.component.ProviderIntegrationTest;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.component.internal.RootComponentManager;
import org.xwiki.component.internal.embed.EmbeddableComponentManagerFactory;
import org.xwiki.component.internal.multi.DefaultComponentManagerManager;
import org.xwiki.component.internal.namespace.DefaultNamespaceValidator;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ComponentAnnotationLoader}.
 *
 * @version $Id$
 * @since 1.8.1
 */
public class ComponentAnnotationLoaderTest
{
    @SuppressWarnings("deprecation")
    @ComponentRole
    public interface NotGenericRole<T>
    {
    }

    @SuppressWarnings("deprecation")
    @ComponentRole
    public interface ExtendedRole extends NotGenericRole<String>
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    public class RoleImpl implements ExtendedRole
    {
    }

    /**
     * Note that even though it's not necessary we implement Role to ensure that the SuperRoleImpl component will only
     * be registered once.
     */
    @Component(staticRegistration = false)
    @Singleton
    public class SuperRoleImpl extends RoleImpl implements NotGenericRole<String>
    {
    }

    // Test overrides with priorities (see components.txt file)
    @Component(value = "test")
    @Singleton
    public class SimpleRole implements NotGenericRole<String>
    {
    }

    @Component(value = "test")
    @Singleton
    public class OverrideRole implements NotGenericRole<String>
    {
    }

    // Verify backward compatibility for deprecated component-overrides.txt file
    @Component(value = "deprecated")
    @Singleton
    public class DeprecatedSimpleRole implements NotGenericRole<String>
    {
    }

    @Component(value = "deprecated")
    @Singleton
    public class DeprecatedOverrideRole implements NotGenericRole<String>
    {
    }

    @Component(staticRegistration = false)
    @Named("customprovider")
    @Singleton
    public class ProviderImpl implements Provider<NotGenericRole<String>>
    {
        @Override
        public NotGenericRole<String> get()
        {
            return new RoleImpl();
        }
    }

    @Role
    public interface GenericRole<T>
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    public class GenericComponent implements GenericRole<String>
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    @SuppressWarnings("rawtypes")
    public class NonGenericComponent implements GenericRole
    {
    }

    public abstract class AbstractGenericComponent<V> implements GenericRole<V>
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    public class ExtendingGenericComponent extends AbstractGenericComponent<String>
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    @SuppressWarnings("rawtypes")
    public class ExtendingNonGenericComponent extends AbstractGenericComponent
    {
    }

    private ComponentAnnotationLoader loader;

    private class TestableComponentAnnotationLoader extends ComponentAnnotationLoader
    {
        private Logger logger;

        TestableComponentAnnotationLoader(Logger logger)
        {
            this.logger = logger;
        }

        @Override
        protected Logger getLogger()
        {
            return this.logger;
        }
    }

    @BeforeEach
    void setupLogger()
    {
        // Note: we don't define any expectation on the Logger since we want to be sure that the tests below don't
        // generate any logging at all.
        this.loader = new TestableComponentAnnotationLoader(mock(Logger.class));
    }

    /**
     * Verify that when there are several component implementations for the same role/hint then the one with the highest
     * priority wins (ie the smallest integer value).
     */
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void priorities() throws Exception
    {
        ComponentManager componentManager = mock(ComponentManager.class);

        ComponentDescriptor descriptor1 =
            this.loader.getComponentsDescriptors(DeprecatedOverrideRole.class).get(0);
        ComponentDescriptor descriptor2 =
            this.loader.getComponentsDescriptors(RootComponentManager.class).get(0);
        ComponentDescriptor descriptor3 = this.loader.getComponentsDescriptors(OverrideRole.class).get(0);
        ComponentDescriptor descriptor4 =
            this.loader.getComponentsDescriptors(EmbeddableComponentManagerFactory.class).get(0);
        ComponentDescriptor descriptor5 =
            this.loader.getComponentsDescriptors(DefaultComponentManagerManager.class).get(0);
        ComponentDescriptor descriptor6 =
            this.loader.getComponentsDescriptors(ContextComponentManagerProvider.class).get(0);
        ComponentDescriptor descriptor7 =
            this.loader.getComponentsDescriptors(DefaultNamespaceValidator.class).get(0);

        ComponentDescriptor descriptor8 =
            this.loader.getComponentsDescriptors(ProviderIntegrationTest.TestProvider1.class).get(0);
        ComponentDescriptor descriptor9 =
            this.loader.getComponentsDescriptors(ProviderIntegrationTest.TestProvider12.class).get(0);
        ComponentDescriptor descriptor10 =
            this.loader.getComponentsDescriptors(ProviderIntegrationTest.TestProvider2.class).get(0);
        ComponentDescriptor descriptor11 =
            this.loader.getComponentsDescriptors(ProviderIntegrationTest.TestComponentWithProviders.class).get(0);
        ComponentDescriptor descriptor12 =
            this.loader.getComponentsDescriptors(ProviderIntegrationTest.TestProviderWithExceptionInInitialize.class).get(0);
        ComponentDescriptor descriptor13 =
            this.loader.getComponentsDescriptors(ProviderIntegrationTest.TestComponentWithProviderInException.class).get(0);

        this.loader.initialize(componentManager, this.getClass().getClassLoader());

        // This is the test, we verify that registerComponent() is called for each of the descriptor we're expecting
        // to be discovered through annotations by the call to initialize() below.
        verify(componentManager, times(1)).registerComponent(descriptor1);
        verify(componentManager, times(1)).registerComponent(descriptor2);
        verify(componentManager, times(1)).registerComponent(descriptor3);
        verify(componentManager, times(1)).registerComponent(descriptor4);
        verify(componentManager, times(1)).registerComponent(descriptor5);
        verify(componentManager, times(1)).registerComponent(descriptor6);
        verify(componentManager, times(1)).registerComponent(descriptor7);
        verify(componentManager, times(1)).registerComponent(descriptor8);
        verify(componentManager, times(1)).registerComponent(descriptor9);
        verify(componentManager, times(1)).registerComponent(descriptor10);
        verify(componentManager, times(1)).registerComponent(descriptor11);
        verify(componentManager, times(1)).registerComponent(descriptor12);
        verify(componentManager, times(1)).registerComponent(descriptor13);
    }

    @Test
    void findComponentRoleTypes()
    {
        assertComponentRoleTypes(RoleImpl.class);
    }

    /**
     * Verify that we get the same result when we use a class that extends another class (i.e. inheritance works).
     */
    @Test
    void findComponentRoleTypesWhenClassExtension()
    {
        assertComponentRoleTypes(SuperRoleImpl.class);
    }

    private void assertComponentRoleTypes(Class<?> componentClass)
    {
        Set<Type> type = this.loader.findComponentRoleTypes(componentClass);
        assertEquals(2, type.size());
        assertTrue(type.contains(NotGenericRole.class));
        assertTrue(type.contains(ExtendedRole.class));
    }

    @Test
    void findComponentRoleTypesForProvider()
    {
        Set<Type> types = this.loader.findComponentRoleTypes(ProviderImpl.class);

        assertEquals(1, types.size());
        assertEquals(new DefaultParameterizedType(null, Provider.class, new DefaultParameterizedType(
            ComponentAnnotationLoaderTest.class, NotGenericRole.class, String.class)), types.iterator().next());
    }

    @Test
    void findComponentRoleTypesWithGenericRole()
    {
        Set<Type> types = this.loader.findComponentRoleTypes(GenericComponent.class);

        assertEquals(1, types.size());
        assertEquals(new DefaultParameterizedType(ComponentAnnotationLoaderTest.class, GenericRole.class,
            String.class), types.iterator().next());
    }

    @Test
    void findComponentRoleTypesWithGenericRoleAndNonGenericComponent()
    {
        Set<Type> types = this.loader.findComponentRoleTypes(NonGenericComponent.class);

        assertEquals(1, types.size());
        assertEquals(GenericRole.class, types.iterator().next());
    }

    @Test
    void findComponentRoleTypesWithExtendingGenericRole()
    {
        Set<Type> types = this.loader.findComponentRoleTypes(ExtendingGenericComponent.class);

        assertEquals(1, types.size());
        assertEquals(new DefaultParameterizedType(ComponentAnnotationLoaderTest.class, GenericRole.class,
            String.class), types.iterator().next());
    }

    @Test
    void findComponentRoleTypesWithExtendingNonGenericRole()
    {
        Set<Type> types = this.loader.findComponentRoleTypes(ExtendingNonGenericComponent.class);

        assertEquals(1, types.size());
        assertEquals(GenericRole.class, types.iterator().next());
    }
}
