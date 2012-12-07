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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.test.AbstractTestCase;

/**
 * Unit tests for {@link ComponentDescriptorFactory}.
 * 
 * @version $Id$
 * @since 1.8.1
 */
public class ComponentDescriptorFactoryTest extends AbstractTestCase
{
    @ComponentRole
    public interface NonGenericFieldRole<T>
    {
    }

    @Role
    public interface GenericFieldRole<T>
    {
    }

    @Component
    public class FieldroleImpl implements NonGenericFieldRole<String>
    {
    }

    @Component("special")
    public class SpecialFieldRoleImpl implements NonGenericFieldRole<String>
    {
    }

    @ComponentRole
    public interface NonGenericRole
    {
    }

    @ComponentRole
    public interface ExtendedRole extends NonGenericRole
    {
    }

    @Component
    public class RoleImpl implements ExtendedRole
    {
        @SuppressWarnings("unused")
        @Inject
        private NonGenericFieldRole<String> fieldRole;

        @SuppressWarnings("unused")
        @Inject
        @Named("special")
        private NonGenericFieldRole<String> specialFieldRole;

        @SuppressWarnings("unused")
        @Inject
        private GenericFieldRole<String> genericFieldRole;

        @SuppressWarnings("unused")
        @Inject
        private GenericFieldRole nonGenericFieldRole;

        /**
         * Inject all implementation of the FieldRole role.
         */
        @SuppressWarnings("unused")
        @Inject
        private List<NonGenericFieldRole<String>> roles;

        /**
         * Inject all implementation of the FieldRole role.
         */
        @SuppressWarnings("unused")
        @Inject
        private Map<String, NonGenericFieldRole<String>> mapRoles;
    }

    @Component
    public class SuperRoleImpl extends RoleImpl
    {
        @SuppressWarnings("unused")
        @Inject
        @Named("other")
        private NonGenericFieldRole<String> fieldRole;
    }

    /**
     * Test that we can have a component implementing several roles.
     */
    @Component(hints = {"hint1", "hint2"})
    public class MultipleRolesImpl implements NonGenericRole
    {
    }

    @Component
    @Singleton
    public class SingletonImpl implements NonGenericRole
    {
    }

    @Component
    @Named("special")
    public class SpecialImpl implements NonGenericRole
    {
    }

    @Test
    public void testCreateComponentDescriptor()
    {
        assertComponentDescriptor(RoleImpl.class, "default");
    }

    /**
     * Verify that we get the same result when we use a class that extends another class (i.e. inheritance works).
     */
    @Test
    public void testCreateComponentDescriptorWhenClassExtension()
    {
        assertComponentDescriptor(SuperRoleImpl.class, "other");
    }

    @Test
    public void testMultipleRolesForComponent()
    {
        ComponentDescriptorFactory factory = new ComponentDescriptorFactory();
        List<ComponentDescriptor> descriptors =
            factory.createComponentDescriptors(MultipleRolesImpl.class, NonGenericRole.class);

        Assert.assertEquals(2, descriptors.size());
        Assert.assertEquals("hint1", descriptors.get(0).getRoleHint());
        Assert.assertEquals("hint2", descriptors.get(1).getRoleHint());
    }

    @Test
    public void testSingletonAnnotationForComponent()
    {
        ComponentDescriptorFactory factory = new ComponentDescriptorFactory();
        List<ComponentDescriptor> descriptors =
            factory.createComponentDescriptors(SingletonImpl.class, NonGenericRole.class);

        Assert.assertEquals(1, descriptors.size());
        Assert.assertEquals(ComponentInstantiationStrategy.SINGLETON, descriptors.get(0).getInstantiationStrategy());
    }

    @Test
    public void testNamedAnnotationForComponent()
    {
        ComponentDescriptorFactory factory = new ComponentDescriptorFactory();
        List<ComponentDescriptor> descriptors =
            factory.createComponentDescriptors(SpecialImpl.class, NonGenericRole.class);

        Assert.assertEquals(1, descriptors.size());
        Assert.assertEquals("special", descriptors.get(0).getRoleHint());
    }

    private void assertComponentDescriptor(Class< ? > componentClass, String fieldRoleName)
    {
        ComponentDescriptorFactory factory = new ComponentDescriptorFactory();
        List<ComponentDescriptor> descriptors = factory.createComponentDescriptors(componentClass, ExtendedRole.class);

        Assert.assertEquals(1, descriptors.size());
        ComponentDescriptor descriptor = descriptors.get(0);

        Assert.assertSame(componentClass, descriptor.getImplementation());
        Assert.assertSame(ExtendedRole.class, descriptor.getRole());
        Assert.assertSame(ExtendedRole.class, descriptor.getRoleType());
        Assert.assertEquals("default", descriptor.getRoleHint());
        Assert.assertEquals(ComponentInstantiationStrategy.SINGLETON, descriptor.getInstantiationStrategy());

        Collection<ComponentDependency<?>> deps = descriptor.getComponentDependencies();
        Assert.assertEquals(6, deps.size());
        Iterator<ComponentDependency<?>> it = deps.iterator();

        // Test the following injection:
        // @Inject
        // private NonGenericFieldRole<String> fieldRole;
        ComponentDependency dep = it.next();
        Assert.assertSame(NonGenericFieldRole.class, dep.getRole());
        Assert.assertSame(NonGenericFieldRole.class, dep.getRoleType());
        Assert.assertEquals(fieldRoleName, dep.getRoleHint());
        Assert.assertSame(NonGenericFieldRole.class, dep.getMappingType());
        Assert.assertEquals("fieldRole", dep.getName());

        // Test the following injection:
        // @Inject
        // @Named("special")
        // private NonGenericFieldRole<String> specialFieldRole;
        dep = it.next();
        Assert.assertSame(NonGenericFieldRole.class, dep.getRole());
        Assert.assertSame(NonGenericFieldRole.class, dep.getRoleType());
        Assert.assertEquals("special", dep.getRoleHint());
        Assert.assertSame(NonGenericFieldRole.class, dep.getMappingType());
        Assert.assertEquals("specialFieldRole", dep.getName());

        // Test the following injection:
        // @Inject
        // private GenericFieldRole<String> genericFieldRole;
        dep = it.next();
        Assert.assertSame(GenericFieldRole.class, dep.getRole());
        Assert.assertEquals(new DefaultParameterizedType(ComponentDescriptorFactoryTest.class, GenericFieldRole.class,
            String.class), dep.getRoleType());
        Assert.assertEquals("default", dep.getRoleHint());
        Assert.assertSame(GenericFieldRole.class, dep.getMappingType());
        Assert.assertEquals("genericFieldRole", dep.getName());

        // Test the following injection:
        // @Inject
        // private GenericFieldRole nonGenericFieldRole;
        dep = it.next();
        Assert.assertSame(GenericFieldRole.class, dep.getRole());
        Assert.assertEquals(GenericFieldRole.class, dep.getRoleType());
        Assert.assertEquals("default", dep.getRoleHint());
        Assert.assertSame(GenericFieldRole.class, dep.getMappingType());
        Assert.assertEquals("nonGenericFieldRole", dep.getName());

        // Test the following injection:
        // @Inject
        // private List<NonGenericFieldRole<String>> roles;
        dep = it.next();
        Assert.assertSame(NonGenericFieldRole.class, dep.getRole());
        Assert.assertEquals("default", dep.getRoleHint());
        Assert.assertSame(List.class, dep.getMappingType());
        Assert.assertEquals("roles", dep.getName());

        // Test the following injection:
        // @Inject
        // private Map<String, NonGenericFieldRole<String>> mapRoles;
        dep = it.next();
        Assert.assertSame(NonGenericFieldRole.class, dep.getRole());
        Assert.assertEquals("default", dep.getRoleHint());
        Assert.assertSame(Map.class, dep.getMappingType());
        Assert.assertEquals("mapRoles", dep.getName());
    }
}
