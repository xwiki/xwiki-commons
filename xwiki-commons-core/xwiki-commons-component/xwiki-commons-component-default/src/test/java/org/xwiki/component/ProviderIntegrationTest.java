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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.embed.EmbeddableComponentManagerTest.Role;
import org.xwiki.component.embed.EmbeddableComponentManagerTest.RoleImpl;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validate loading and injection of Providers in a real use case.
 *
 * @version $Id$
 */
public class ProviderIntegrationTest
{
    @ComponentRole
    public interface TestComponentRole
    {

    }

    @Component
    @Singleton
    public static class TestComponentWithProviders implements TestComponentRole
    {
        @Inject
        public Provider<String> provider1;

        @Inject
        @Named("another")
        public Provider<String> provider12;

        @Inject
        public Provider<Integer> provider2;

        @Inject
        public Provider<List<Role>> providerList;

        @Inject
        public Provider<Map<String, Role>> providerMap;
    }

    public static class TestProvider1 implements Provider<String>
    {
        @Override
        public String get()
        {
            return "value";
        }
    }

    @Named("another")
    public static class TestProvider12 implements Provider<String>
    {
        @Override
        public String get()
        {
            return "another value";
        }
    }

    public static class TestProvider2 implements Provider<Integer>
    {
        @Override
        public Integer get()
        {
            return 1;
        }
    }

    @Component
    @Named("exception")
    @Singleton
    public static class TestComponentWithProviderInException implements TestComponentRole
    {
        @Inject
        @Named("exception")
        public Provider<String> providerWithExceptionInInitialize;
    }

    @Named("exception")
    public static class TestProviderWithExceptionInInitialize implements Provider<String>, Initializable
    {
        @Override
        public void initialize() throws InitializationException
        {
            throw new InitializationException("Some error in init");
        }

        @Override
        public String get()
        {
            throw new RuntimeException("should not be called!");
        }
    }

    @Test
    void loadAndInjectProviders() throws ComponentLookupException, ComponentRepositoryException
    {
        EmbeddableComponentManager cm = new EmbeddableComponentManager();

        // Register components for the list and map
        DefaultComponentDescriptor<Role> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(Role.class);
        cd1.setRoleHint("hint1");
        cd1.setImplementation(RoleImpl.class);
        cm.registerComponent(cd1);
        DefaultComponentDescriptor<Role> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(Role.class);
        cd2.setRoleHint("hint2");
        cd2.setImplementation(RoleImpl.class);
        cm.registerComponent(cd2);

        // Initialize
        cm.initialize(getClass().getClassLoader());

        TestComponentWithProviders component = cm.getInstance(TestComponentRole.class);

        assertEquals("value", component.provider1.get());
        assertEquals("another value", component.provider12.get());
        assertEquals(Integer.valueOf(1), component.provider2.get());

        assertEquals(2, component.providerList.get().size());
        assertEquals(2, component.providerMap.get().size());
    }

    /**
     * Verify that an exception is raised when a Provider implementing {@link Initializable} fails to initialize.
     */
    @Test
    void loadAndInjectProviderWhenExceptionInInitialize()
    {
        EmbeddableComponentManager cm = new EmbeddableComponentManager();
        cm.initialize(getClass().getClassLoader());

        Throwable exception = assertThrows(ComponentLookupException.class,
            () -> cm.getInstance(TestComponentRole.class, "exception"));
        assertEquals("Failed to lookup component "
            + "[org.xwiki.component.ProviderIntegrationTest$TestComponentWithProviderInException] identified by "
            + "type [interface org.xwiki.component.ProviderIntegrationTest$TestComponentRole] and hint [exception]",
            exception.getMessage());
        assertEquals("Failed to lookup component "
            + "[org.xwiki.component.ProviderIntegrationTest$TestProviderWithExceptionInInitialize] identified by "
            + "type [javax.inject.Provider<java.lang.String>] and hint [exception]",
            exception.getCause().getMessage());
        assertEquals("Some error in init", exception.getCause().getCause().getMessage());
    }

    @Test
    void loadAndInjectProviderWhenNoImplementationFound() throws Exception
    {
        EmbeddableComponentManager cm = new EmbeddableComponentManager();

        // Register a bad descriptor (missing an implementation class) to force an error.
        DefaultComponentDescriptor<Role> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(Role.class);
        cd.setRoleHint("hint1");
        cm.registerComponent(cd);

        cm.initialize(getClass().getClassLoader());

        TestComponentWithProviders component = cm.getInstance(TestComponentRole.class);
        Throwable exception = assertThrows(RuntimeException.class,
            () -> component.providerList.get());
        assertEquals("Failed to get [role = "
            + "[java.util.List<org.xwiki.component.embed.EmbeddableComponentManagerTest$Role>] hint = [default]]",
            exception.getMessage());
    }
}
