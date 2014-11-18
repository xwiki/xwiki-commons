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

import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.xwiki.component.ProviderTest;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.component.internal.RootComponentManager;
import org.xwiki.component.internal.embed.EmbeddableComponentManagerFactory;
import org.xwiki.component.internal.multi.DefaultComponentManagerManager;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.test.jmock.JMockRule;

/**
 * Unit tests for {@link ComponentAnnotationLoader}.
 *
 * @version $Id$
 * @since 1.8.1
 */
public class ComponentAnnotationLoaderTest
{
    @Rule
    public final JMockRule mockery = new JMockRule();

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
    public class RoleImpl implements ExtendedRole
    {
    }

    /**
     * Note that even though it's not necessary we implement Role to ensure that the SuperRoleImpl component will only
     * be registered once.
     */
    @Component(staticRegistration = false)
    public class SuperRoleImpl extends RoleImpl implements NotGenericRole<String>
    {
    }

    // Test overrides with priorities (see components.txt file)
    @Component(value = "test")
    public class SimpleRole implements NotGenericRole<String>
    {
    }

    @Component(value = "test")
    public class OverrideRole implements NotGenericRole<String>
    {
    }

    // Verify backward compatibility for deprecated component-overrides.txt file
    @Component(value = "deprecated")
    public class DeprecatedSimpleRole implements NotGenericRole<String>
    {
    }

    @Component(value = "deprecated")
    public class DeprecatedOverrideRole implements NotGenericRole<String>
    {
    }

    @Component(staticRegistration = false)
    @Named("customprovider")
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
    public class GenericComponent implements GenericRole<String>
    {
    }

    @Component(staticRegistration = false)
    @SuppressWarnings("rawtypes")
    public class NonGenericComponent implements GenericRole
    {
    }

    public abstract class AbstractGenericComponent<V> implements GenericRole<V>
    {
    }

    @Component(staticRegistration = false)
    public class ExtendingGenericComponent extends AbstractGenericComponent<String>
    {
    }

    @Component(staticRegistration = false)
    @SuppressWarnings("rawtypes")
    public class ExtendingNonGenericComponent extends AbstractGenericComponent
    {
    }

    private ComponentAnnotationLoader loader;

    private class TestableComponentAnnotationLoader extends ComponentAnnotationLoader
    {
        private Logger logger;

        public TestableComponentAnnotationLoader(Logger logger)
        {
            this.logger = logger;
        }

        @Override
        protected Logger getLogger()
        {
            return this.logger;
        }
    }

    @Before
    public void setupLogger() throws Exception
    {
        // Note: we don't define any expectation on the Logger since we want to be sure that the tests below don't
        // generate any logging at all.
        this.loader = new TestableComponentAnnotationLoader(this.mockery.mock(Logger.class));
    }

    @After
    public void tearDown() throws Exception
    {
        this.mockery.assertIsSatisfied();
    }

    /**
     * Verify that when there are several component implementations for the same role/hint then the one with the highest
     * priority wins (ie the smallest integer value).
     */
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testPriorities() throws Exception
    {
        final ComponentManager mockManager = this.mockery.mock(ComponentManager.class);

        final ComponentDescriptor descriptor1 =
            this.loader.getComponentsDescriptors(DeprecatedOverrideRole.class).get(0);
        final ComponentDescriptor descriptor2 =
            this.loader.getComponentsDescriptors(RootComponentManager.class).get(0);
        final ComponentDescriptor descriptor3 = this.loader.getComponentsDescriptors(OverrideRole.class).get(0);
        final ComponentDescriptor descriptor4 =
            this.loader.getComponentsDescriptors(EmbeddableComponentManagerFactory.class).get(0);
        final ComponentDescriptor descriptor5 =
            this.loader.getComponentsDescriptors(DefaultComponentManagerManager.class).get(0);
        final ComponentDescriptor descriptor6 =
            this.loader.getComponentsDescriptors(ContextComponentManagerProvider.class).get(0);

        final ComponentDescriptor descriptor7 =
            this.loader.getComponentsDescriptors(ProviderTest.TestProvider1.class).get(0);
        final ComponentDescriptor descriptor8 =
            this.loader.getComponentsDescriptors(ProviderTest.TestProvider12.class).get(0);
        final ComponentDescriptor descriptor9 =
            this.loader.getComponentsDescriptors(ProviderTest.TestProvider2.class).get(0);
        final ComponentDescriptor descriptor10 =
            this.loader.getComponentsDescriptors(ProviderTest.TestComponentWithProviders.class).get(0);
        final ComponentDescriptor descriptor11 =
            this.loader.getComponentsDescriptors(ProviderTest.TestProviderWithExceptionInInitialize.class).get(0);
        final ComponentDescriptor descriptor12 =
            this.loader.getComponentsDescriptors(ProviderTest.TestComponentWithProviderInException.class).get(0);

        // This is the test, we verify that registerComponent() is called for each of the descriptor we're expecting
        // to be discovered through annotations by the call to initialize() below.
        this.mockery.checking(new Expectations()
        {
            {
                oneOf(mockManager).registerComponent(descriptor1);
                oneOf(mockManager).registerComponent(descriptor2);
                oneOf(mockManager).registerComponent(descriptor3);
                oneOf(mockManager).registerComponent(descriptor4);
                oneOf(mockManager).registerComponent(descriptor5);
                oneOf(mockManager).registerComponent(descriptor6);
                oneOf(mockManager).registerComponent(descriptor7);
                oneOf(mockManager).registerComponent(descriptor8);
                oneOf(mockManager).registerComponent(descriptor9);
                oneOf(mockManager).registerComponent(descriptor10);
                oneOf(mockManager).registerComponent(descriptor11);
                oneOf(mockManager).registerComponent(descriptor12);
            }
        });

        this.loader.initialize(mockManager, this.getClass().getClassLoader());
    }

    @Test
    public void testFindComponentRoleTypes()
    {
        assertComponentRoleTypes(RoleImpl.class);
    }

    /**
     * Verify that we get the same result when we use a class that extends another class (i.e. inheritance works).
     */
    @Test
    public void testFindComponentRoleTypesWhenClassExtension()
    {
        assertComponentRoleTypes(SuperRoleImpl.class);
    }

    private void assertComponentRoleTypes(Class<?> componentClass)
    {
        Set<Type> type = this.loader.findComponentRoleTypes(componentClass);
        Assert.assertEquals(2, type.size());
        Assert.assertTrue(type.contains(NotGenericRole.class));
        Assert.assertTrue(type.contains(ExtendedRole.class));
    }

    @Test
    public void testFindComponentRoleTypesForProvider()
    {
        Set<Type> types = this.loader.findComponentRoleTypes(ProviderImpl.class);

        Assert.assertEquals(1, types.size());
        Assert.assertEquals(new DefaultParameterizedType(null, Provider.class, new DefaultParameterizedType(
            ComponentAnnotationLoaderTest.class, NotGenericRole.class, String.class)), types.iterator().next());
    }

    @Test
    public void testFindComponentRoleTypesWithGenericRole()
    {
        Set<Type> types = this.loader.findComponentRoleTypes(GenericComponent.class);

        Assert.assertEquals(1, types.size());
        Assert.assertEquals(new DefaultParameterizedType(ComponentAnnotationLoaderTest.class, GenericRole.class,
            String.class), types.iterator().next());
    }

    @Test
    public void testFindComponentRoleTypesWithGenericRoleAndNonGenericComponent()
    {
        Set<Type> types = this.loader.findComponentRoleTypes(NonGenericComponent.class);

        Assert.assertEquals(1, types.size());
        Assert.assertEquals(GenericRole.class, types.iterator().next());
    }

    @Test
    public void testFindComponentRoleTypesWithExtendingGenericRole()
    {
        Set<Type> types = this.loader.findComponentRoleTypes(ExtendingGenericComponent.class);

        Assert.assertEquals(1, types.size());
        Assert.assertEquals(new DefaultParameterizedType(ComponentAnnotationLoaderTest.class, GenericRole.class,
            String.class), types.iterator().next());
    }

    @Test
    public void testFindComponentRoleTypesWithExtendingNonGenericRole()
    {
        Set<Type> types = this.loader.findComponentRoleTypes(ExtendingNonGenericComponent.class);

        Assert.assertEquals(1, types.size());
        Assert.assertEquals(GenericRole.class, types.iterator().next());
    }
}
