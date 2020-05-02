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
package org.xwiki.component.guice;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Role;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import static com.google.inject.matcher.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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

    @Component(staticRegistration = false)
    @Named("name")
    @Singleton
    public static class FieldRoleImpl1 implements FieldRole
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    public static class FieldRoleImpl2 implements FieldRole
    {
    }

    @Role
    public interface RoleClass
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    public static class ProviderImpl implements Provider<FieldRole>
    {
        @Override
        public FieldRole get()
        {
            return new FieldRoleImpl2();
        }
    }

    @Role
    public interface GenericFieldRole<T>
    {
    }

    @Component(staticRegistration = false)
    @Singleton
    public static class GenericFieldRoleImpl<String> implements GenericFieldRole<String>
    {
    }

    @Component(staticRegistration = false)
    @Named("whatever")
    @Singleton
    public static class RoleImpl implements RoleClass
    {
        // Test a named component injection
        @Inject
        @Named("name")
        private FieldRole fieldRole1;

        // Test a Provider injection
        @Inject
        private Provider<FieldRole> fieldRoleProvider;

        // Test a generics component injection
        @Inject
        private GenericFieldRole<String> genericFieldRole;

        // Test a Logger injection
        @Inject
        private Logger logger;

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

        public Logger getLogger()
        {
            return this.logger;
        }
    }

    public class TestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            // Special binding for SLJ4 Logger injection
            bindListener(any(), new Slf4jInjectionTypeListener());

            // Since XWiki uses the @Inject annotation to inject Loggers too we need to resolve that interface to
            // an implementation for Guice, even though it's going to be overwritten by the Slf4jInjectionTypeListener!
            bind(Logger.class).toInstance(LoggerFactory.getLogger("Not used!"));

            bind(FieldRole.class).annotatedWith(Names.named("name")).to(FieldRoleImpl1.class);
            bind(FieldRole.class).toProvider(ProviderImpl.class);
            bind(new TypeLiteral<GenericFieldRole<String>>(){}).to(new TypeLiteral<GenericFieldRoleImpl<String>>(){});
        }
    }

    @Test
    void guiceInjection()
    {
        Injector injector = Guice.createInjector(new TestModule());

        RoleImpl impl1 = injector.getInstance(RoleImpl.class);
        assertEquals(FieldRoleImpl1.class.getName(), impl1.getFieldRole1().getClass().getName());
        assertEquals(FieldRoleImpl2.class.getName(), impl1.getFieldRole2().getClass().getName());
        assertEquals(GenericFieldRoleImpl.class.getName(), impl1.getGenericFieldRole().getClass().getName());
        assertEquals(RoleImpl.class.getName(), impl1.getLogger().getName());

        // Test that FieldRole impl is a singleton
        RoleImpl impl2 = injector.getInstance(RoleImpl.class);
        assertSame(impl1.getFieldRole1(), impl2.getFieldRole1());
    }
}
