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
package org.xwiki.component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Role;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Verifies that XWiki components using JSR330 annotation can be used with Guice, thus demonstrating that we're kind of
 * spec compliant (even if we do not implement the whole JSR330 specification).
 * 
 * @version $Id$
 * @since 3.1M1
 */
public class GuiceCompatibilityTest
{
    @Role
    public interface FieldRole
    {
    }

    @Component
    @Named("name")
    @Singleton
    public static class FieldRoleImpl1 implements FieldRole
    {
    }

    @Component
    public static class FieldRoleImpl2 implements FieldRole
    {
    }

    @Role
    public interface RoleClass
    {
    }

    @Component
    public static class ProviderImpl implements Provider<FieldRole>
    {
        @Override public FieldRole get()
        {
            return new FieldRoleImpl2();
        }
    }

    @Role
    public interface GenericFieldRole<T>
    {
    }

    @Component
    @Singleton
    public static class GenericFieldRoleImpl<String> implements GenericFieldRole<String>
    {
    }

    @Component
    @Named("whatever")
    public static class RoleImpl implements RoleClass
    {
        @Inject
        @Named("name")
        private FieldRole fieldRole1;

        @Inject
        private Provider<FieldRole> fieldRoleProvider;

        @Inject
        private GenericFieldRole<String> genericFieldRole;

        public FieldRole getFieldRole1()
        {
            return this.fieldRole1;
        }

        public FieldRole getFieldRole2()
        {
            return this.fieldRoleProvider.get();
        }

        public GenericFieldRole<String> getGenericFieldRole()
        {
            return this.genericFieldRole;
        }
    }

    public class TestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(FieldRole.class).annotatedWith(Names.named("name")).to(FieldRoleImpl1.class);
            bind(FieldRole.class).toProvider(ProviderImpl.class);
            bind(new TypeLiteral<GenericFieldRole<String>>(){}).to(new TypeLiteral<GenericFieldRoleImpl<String>>(){});
        }
    }

    @Test
    public void testGuice()
    {
        Injector injector = Guice.createInjector(new TestModule());

        RoleImpl impl1 = injector.getInstance(RoleImpl.class);
        Assert.assertEquals(FieldRoleImpl1.class.getName(), impl1.getFieldRole1().getClass().getName());
        Assert.assertEquals(FieldRoleImpl2.class.getName(), impl1.getFieldRole2().getClass().getName());
        Assert.assertEquals(GenericFieldRoleImpl.class.getName(), impl1.getGenericFieldRole().getClass().getName());

        // Test that FieldRole impl is a singleton
        RoleImpl impl2 = injector.getInstance(RoleImpl.class);
        Assert.assertSame(impl1.getFieldRole1(), impl2.getFieldRole1());
    }
}
