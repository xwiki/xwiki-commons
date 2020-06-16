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

import org.junit.jupiter.api.Test;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.util.DefaultParameterizedType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link ComponentDescriptorFactory}.
 *
 * @version $Id$
 * @since 1.8.1
 */
public class ComponentDescriptorFactoryTest
{
    @ComponentRole
    public interface NonGenericFieldRole<T>
    {
    }

    @Role
    public interface GenericFieldRole<T>
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    public class FieldroleImpl implements NonGenericFieldRole<String>
    {
    }

    @Component(value = "special", staticRegistration = false)
    @Singleton
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

    @Component(staticRegistration = false)
    @Singleton
    public class RoleImpl implements ExtendedRole
    {
        @Inject
        private NonGenericFieldRole<String> fieldRole;

        @Inject
        @Named("special")
        private NonGenericFieldRole<String> specialFieldRole;

        @Inject
        private GenericFieldRole<String> genericFieldRole;

        @Inject
        private GenericFieldRole nonGenericFieldRole;

        /**
         * Inject all implementation of the FieldRole role.
         */
        @Inject
        private List<NonGenericFieldRole<String>> roles;

        /**
         * Inject all implementation of the FieldRole role.
         */
        @Inject
        private Map<String, NonGenericFieldRole<String>> mapRoles;
    }

    @Component(staticRegistration = false)
    @Singleton
    public class SuperRoleImpl extends RoleImpl
    {
        @Inject
        @Named("other")
        private NonGenericFieldRole<String> fieldRole;
    }

    /**
     * Test that we can have a component implementing several roles.
     */
    @Component(hints = { "hint1", "hint2" }, staticRegistration = false)
    @Singleton
    public class MultipleRolesImpl implements NonGenericRole
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    public class SingletonImpl implements NonGenericRole
    {
    }

    @Component(staticRegistration = false)
    @Named("special")
    @Singleton
    public class SpecialImpl implements NonGenericRole
    {
    }

    @Test
    void tcreateComponentDescriptor()
    {
        assertComponentDescriptor(RoleImpl.class, "default");
    }

    /**
     * Verify that we get the same result when we use a class that extends another class (i.e. inheritance works).
     */
    @Test
    void createComponentDescriptorWhenClassExtension()
    {
        assertComponentDescriptor(SuperRoleImpl.class, "other");
    }

    @Test
    void multipleRolesForComponent()
    {
        ComponentDescriptorFactory factory = new ComponentDescriptorFactory();
        List<ComponentDescriptor<MultipleRolesImpl>> descriptors =
            factory.createComponentDescriptors(MultipleRolesImpl.class, NonGenericRole.class);

        assertEquals(2, descriptors.size());
        assertEquals("hint1", descriptors.get(0).getRoleHint());
        assertEquals("hint2", descriptors.get(1).getRoleHint());
    }

    @Test
    void singletonAnnotationForComponent()
    {
        ComponentDescriptorFactory factory = new ComponentDescriptorFactory();
        List<ComponentDescriptor<SingletonImpl>> descriptors =
            factory.createComponentDescriptors(SingletonImpl.class, NonGenericRole.class);

        assertEquals(1, descriptors.size());
        assertEquals(ComponentInstantiationStrategy.SINGLETON, descriptors.get(0).getInstantiationStrategy());
    }

    @Test
    void namedAnnotationForComponent()
    {
        ComponentDescriptorFactory factory = new ComponentDescriptorFactory();
        List<ComponentDescriptor<SpecialImpl>> descriptors =
            factory.createComponentDescriptors(SpecialImpl.class, NonGenericRole.class);

        assertEquals(1, descriptors.size());
        assertEquals("special", descriptors.get(0).getRoleHint());
    }

    private <T> void assertComponentDescriptor(Class<? extends T> componentClass, String fieldRoleName)
    {
        ComponentDescriptorFactory factory = new ComponentDescriptorFactory();
        List<ComponentDescriptor<T>> descriptors = factory.createComponentDescriptors(componentClass,
            ExtendedRole.class);

        assertEquals(1, descriptors.size());
        ComponentDescriptor descriptor = descriptors.get(0);

        assertSame(componentClass, descriptor.getImplementation());
        assertSame(ExtendedRole.class, descriptor.getRole());
        assertSame(ExtendedRole.class, descriptor.getRoleType());
        assertEquals("default", descriptor.getRoleHint());
        assertEquals(ComponentInstantiationStrategy.SINGLETON, descriptor.getInstantiationStrategy());

        Collection<ComponentDependency<?>> deps = descriptor.getComponentDependencies();
        assertEquals(6, deps.size());
        Iterator<ComponentDependency<?>> it = deps.iterator();

        // Test the following injection:
        // @Inject
        // private NonGenericFieldRole<String> fieldRole;
        ComponentDependency dep = it.next();
        assertSame(NonGenericFieldRole.class, dep.getRole());
        assertSame(NonGenericFieldRole.class, dep.getRoleType());
        assertEquals(fieldRoleName, dep.getRoleHint());
        assertSame(NonGenericFieldRole.class, dep.getMappingType());
        assertEquals("fieldRole", dep.getName());

        // Test the following injection:
        // @Inject
        // @Named("special")
        // private NonGenericFieldRole<String> specialFieldRole;
        dep = it.next();
        assertSame(NonGenericFieldRole.class, dep.getRole());
        assertSame(NonGenericFieldRole.class, dep.getRoleType());
        assertEquals("special", dep.getRoleHint());
        assertSame(NonGenericFieldRole.class, dep.getMappingType());
        assertEquals("specialFieldRole", dep.getName());

        // Test the following injection:
        // @Inject
        // private GenericFieldRole<String> genericFieldRole;
        dep = it.next();
        assertSame(GenericFieldRole.class, dep.getRole());
        assertEquals(new DefaultParameterizedType(ComponentDescriptorFactoryTest.class, GenericFieldRole.class,
            String.class), dep.getRoleType());
        assertEquals("default", dep.getRoleHint());
        assertSame(GenericFieldRole.class, dep.getMappingType());
        assertEquals("genericFieldRole", dep.getName());

        // Test the following injection:
        // @Inject
        // private GenericFieldRole nonGenericFieldRole;
        dep = it.next();
        assertSame(GenericFieldRole.class, dep.getRole());
        assertEquals(GenericFieldRole.class, dep.getRoleType());
        assertEquals("default", dep.getRoleHint());
        assertSame(GenericFieldRole.class, dep.getMappingType());
        assertEquals("nonGenericFieldRole", dep.getName());

        // Test the following injection:
        // @Inject
        // private List<NonGenericFieldRole<String>> roles;
        dep = it.next();
        assertSame(NonGenericFieldRole.class, dep.getRole());
        assertEquals("default", dep.getRoleHint());
        assertSame(List.class, dep.getMappingType());
        assertEquals("roles", dep.getName());

        // Test the following injection:
        // @Inject
        // private Map<String, NonGenericFieldRole<String>> mapRoles;
        dep = it.next();
        assertSame(NonGenericFieldRole.class, dep.getRole());
        assertEquals("default", dep.getRoleHint());
        assertSame(Map.class, dep.getMappingType());
        assertEquals("mapRoles", dep.getName());
    }
}
