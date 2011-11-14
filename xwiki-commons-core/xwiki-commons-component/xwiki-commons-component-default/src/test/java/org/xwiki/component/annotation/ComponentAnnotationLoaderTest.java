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

import java.util.Set;

import javax.inject.Named;
import javax.inject.Provider;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.internal.DefaultComponentManager;
import org.xwiki.component.manager.ComponentManager;

/**
 * Unit tests for {@link ComponentAnnotationLoader}.
 * 
 * @version $Id$
 * @since 1.8.1
 */
public class ComponentAnnotationLoaderTest
{
    @ComponentRole
    public interface Role
    {
    }

    @ComponentRole
    public interface ExtendedRole extends Role
    {
    }

    @Component
    public class RoleImpl implements ExtendedRole
    {
    }

    /**
     * Note that even though it's not necessary we implement Role to ensure that the SuperRoleImpl component
     * will only be registered once.
     */
    @Component
    public class SuperRoleImpl extends RoleImpl implements Role
    {
    }

    // Test overrides with priorities (see components.txt file)
    @Component("test")
    public class SimpleRole implements Role
    {
    }

    @Component("test")
    public class OverrideRole implements Role
    {
    }

    // Verify backward compatibility for deprecated component-overrides.txt file
    @Component("deprecated")
    public class DeprecatedSimpleRole implements Role
    {
    }

    @Component("deprecated")
    public class DeprecatedOverrideRole implements Role
    {
    }

    @Component
    @Named("customprovider")
    public class ProviderImpl implements Provider<Role>
    {
        @Override
        public Role get()
        {
            return new RoleImpl();
        }
    }

    private Mockery context = new Mockery();

    @After
    public void tearDown() throws Exception
    {
        this.context.assertIsSatisfied();
    }

    @Test
    public void testFindComponentRoleClasses()
    {
        assertComponentRoleClasses(RoleImpl.class);
    }

    /**
     * Verify that when there are several component implementations for the same role/hint then the one with the
     * highest priority wins (ie the smallest integer value).
     */
    @Test
    public void testPriorities() throws Exception
    {
    	ComponentAnnotationLoader loader = new ComponentAnnotationLoader();

    	final ComponentManager mockManager = this.context.mock(ComponentManager.class);

        final DefaultComponentDescriptor descriptor1 = new DefaultComponentDescriptor<Role>();
        descriptor1.setImplementation(DeprecatedOverrideRole.class);
        descriptor1.setRole(Role.class);
        descriptor1.setRoleHint("deprecated");

        final DefaultComponentDescriptor descriptor2 = new DefaultComponentDescriptor<Role>();
        descriptor2.setImplementation(DefaultComponentManager.class);
        descriptor2.setRole(ComponentManager.class);

        final DefaultComponentDescriptor descriptor3 = new DefaultComponentDescriptor<Role>();
        descriptor3.setImplementation(OverrideRole.class);
        descriptor3.setRole(Role.class);
        descriptor3.setRoleHint("test");

    	this.context.checking(new Expectations() {{
            oneOf(mockManager).registerComponent(descriptor1);
            oneOf(mockManager).registerComponent(descriptor2);
            oneOf(mockManager).registerComponent(descriptor3);
        }});

    	loader.initialize(mockManager, this.getClass().getClassLoader());
    }

    /**
     * Verify that we get the same result when we use a class that extends another class (i.e. inheritance works).
     */
    @Test
    public void testFindComponentRoleClasseWhenClassExtension()
    {
        assertComponentRoleClasses(SuperRoleImpl.class);
    }

    @Test
    public void testFindComponentRoleClassesForProvider()
    {
        ComponentAnnotationLoader loader = new ComponentAnnotationLoader();
        Set<Class< ? >> classes = loader.findComponentRoleClasses(ProviderImpl.class);

        Assert.assertEquals(1, classes.size());
    }

    private void assertComponentRoleClasses(Class< ? > componentClass)
    {
        ComponentAnnotationLoader loader = new ComponentAnnotationLoader();
        Set<Class< ? >> classes = loader.findComponentRoleClasses(componentClass);
        Assert.assertEquals(2, classes.size());
        Assert.assertTrue(classes.contains(Role.class));
        Assert.assertTrue(classes.contains(ExtendedRole.class));
    }
}
