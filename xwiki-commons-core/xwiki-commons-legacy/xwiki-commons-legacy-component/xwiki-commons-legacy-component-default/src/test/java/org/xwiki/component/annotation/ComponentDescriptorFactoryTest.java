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

import javax.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.xwiki.component.descriptor.ComponentDependency;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ComponentDescriptorFactory} but using depecated
 * {@link org.xwiki.component.annotation.Requirement} annotations.
 * 
 * @version $Id$
 * @since 3.2RC1
 */
class ComponentDescriptorFactoryTest
{
    @ComponentRole
    public interface FieldRole
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    public class FieldroleImpl implements FieldRole
    {
    }
    
    @Component(value = "special", staticRegistration = false)
    @Singleton
    public class SpecialFieldRoleImpl implements FieldRole
    {
    }

    @ComponentRole
    public interface Role
    {
    }

    @ComponentRole
    public interface ExtendedRole extends Role
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    public class RoleImpl implements ExtendedRole
    {
        @SuppressWarnings("unused")
        @Requirement
        private FieldRole fieldRole;

        @SuppressWarnings("unused")
        @Requirement("special")
        private FieldRole specialFieldRole;

        /**
         * Inject all implementation of the FieldRole role. 
         */
        @SuppressWarnings("unused")
        @Requirement
        private List<FieldRole> roles;

        /**
         * Only inject FieldRole implementation with a "special" hint.
         */
        @SuppressWarnings("unused")
        @Requirement(hints = {"special"})
        private List<FieldRole> specialRoles;
        
        /**
         * Inject all implementation of the FieldRole role. 
         */
        @SuppressWarnings("unused")
        @Requirement
        private Map<String, FieldRole> mapRoles;
    }

    @Component(staticRegistration = false)
    @Singleton
    public class SuperRoleImpl extends RoleImpl
    {
        @SuppressWarnings("unused")
        @Requirement("other")
        private FieldRole fieldRole;
    }

    @Component(staticRegistration = false)
    @InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
    public class SingletonImpl implements Role
    {
    }

    @Test
    void createComponentDescriptor()
    {
        assertComponentDescriptor(RoleImpl.class, "default");
    }
    
    /**
     * Verify that we get the same result when we use a class that extends another class (i.e. inheritance
     * works).
     */
    @Test
    void createComponentDescriptorWhenClassExtension()
    {
        assertComponentDescriptor(SuperRoleImpl.class, "other");
    }

    @Test
    void singletonAnnotationForComponent()
    {
        ComponentDescriptorFactory factory = new ComponentDescriptorFactory();
        List<ComponentDescriptor> descriptors =
            factory.createComponentDescriptors(SingletonImpl.class, Role.class);

        assertEquals(1, descriptors.size());
        assertEquals(ComponentInstantiationStrategy.SINGLETON, descriptors.get(0).getInstantiationStrategy());
    }

    private void assertComponentDescriptor(Class< ? > componentClass, String fieldRoleName)
    {
        ComponentDescriptorFactory factory = new ComponentDescriptorFactory();
        List<ComponentDescriptor> descriptors = factory.createComponentDescriptors(componentClass, ExtendedRole.class);
        
        assertEquals(1, descriptors.size());
        ComponentDescriptor descriptor = descriptors.get(0);
        
        assertEquals(componentClass.getName(), descriptor.getImplementation().getName());
        assertEquals(ExtendedRole.class.getName(), descriptor.getRole().getName());
        assertEquals("default", descriptor.getRoleHint());
        assertEquals(ComponentInstantiationStrategy.SINGLETON, descriptor.getInstantiationStrategy());

        Collection<ComponentDependency> deps = descriptor.getComponentDependencies(); 
        assertEquals(5, deps.size());
        Iterator<ComponentDependency> it = deps.iterator();

        // Test the following injection:
        //   @Requirement
        //   private FieldRole fieldRole;
        ComponentDependency dep = it.next();
        assertEquals(FieldRole.class.getName(), dep.getRole().getName());
        assertEquals(fieldRoleName, dep.getRoleHint());
        assertEquals(FieldRole.class.getName(), dep.getMappingType().getName());
        assertEquals("fieldRole", dep.getName());

        // Test the following injection:
        //   @Requirement("special")
        //   private FieldRole specialFieldRole;
        dep = it.next();
        assertEquals(FieldRole.class.getName(), dep.getRole().getName());
        assertEquals("special", dep.getRoleHint());
        assertEquals(FieldRole.class.getName(), dep.getMappingType().getName());
        assertEquals("specialFieldRole", dep.getName());

        // Test the following injection:
        //   @Requirement
        //   private List<FieldRole> roles;
        dep = it.next();
        assertEquals(FieldRole.class.getName(), dep.getRole().getName());
        assertEquals("default", dep.getRoleHint());
        assertEquals(List.class.getName(), dep.getMappingType().getName());
        assertEquals("roles", dep.getName());

        // Test the following injection:
        //   @Requirement(hints = {"special"})
        //   private List<FieldRole> specialRoles;
        dep = it.next();
        assertEquals(FieldRole.class.getName(), dep.getRole().getName());
        assertEquals("default", dep.getRoleHint());
        assertEquals(List.class.getName(), dep.getMappingType().getName());
        assertEquals("specialRoles", dep.getName());
        
        // Test the following injection:
        //   @Requirement
        //   private Map<String, FieldRole> mapRoles;
        dep = it.next();
        assertEquals(FieldRole.class.getName(), dep.getRole().getName());
        assertEquals("default", dep.getRoleHint());
        assertEquals(Map.class.getName(), dep.getMappingType().getName());
        assertEquals("mapRoles", dep.getName());
    }
}
