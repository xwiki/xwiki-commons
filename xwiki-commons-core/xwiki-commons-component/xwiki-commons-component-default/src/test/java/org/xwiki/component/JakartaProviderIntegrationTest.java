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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Role;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.embed.EmbeddableComponentManagerTest.RoleImpl;
import org.xwiki.component.embed.EmbeddableComponentManagerTest.TestRole;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validate loading and injection of Providers in a real use case.
 *
 * @version $Id$
 */
//This class needs to remain public because some interfaces are reused in other tests
public class JakartaProviderIntegrationTest
{
    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.ERROR);

    @Role
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
        public Provider<List<TestRole>> providerList;

        @Inject
        public Provider<Map<String, TestRole>> providerMap;
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
        DefaultComponentDescriptor<TestRole> cd1 = new DefaultComponentDescriptor<>();
        cd1.setRoleType(TestRole.class);
        cd1.setRoleHint("hint1");
        cd1.setImplementation(RoleImpl.class);
        cm.registerComponent(cd1);
        DefaultComponentDescriptor<TestRole> cd2 = new DefaultComponentDescriptor<>();
        cd2.setRoleType(TestRole.class);
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

        Throwable exception =
            assertThrows(ComponentLookupException.class, () -> cm.getInstance(TestComponentRole.class, "exception"));
        assertEquals("Failed to lookup component "
            + "[org.xwiki.component.JakartaProviderIntegrationTest$TestComponentWithProviderInException] identified by "
            + "type [interface org.xwiki.component.JakartaProviderIntegrationTest$TestComponentRole] and hint [exception]",
            exception.getMessage());
        assertEquals("Failed to lookup component "
            + "[org.xwiki.component.JakartaProviderIntegrationTest$TestProviderWithExceptionInInitialize] identified by "
            + "type [jakarta.inject.Provider<java.lang.String>] and hint [exception]",
            exception.getCause().getMessage());
        assertEquals("Some error in init", exception.getCause().getCause().getMessage());
    }

    @Test
    void loadAndInjectProviderWhenNoImplementationFound() throws Exception
    {
        EmbeddableComponentManager cm = new EmbeddableComponentManager();

        // Register a bad descriptor (missing an implementation class) to force an error.
        DefaultComponentDescriptor<TestRole> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(TestRole.class);
        cd.setRoleHint("hint1");
        cm.registerComponent(cd);

        cm.initialize(getClass().getClassLoader());

        TestComponentWithProviders component = cm.getInstance(TestComponentRole.class);
        List<TestRole> components = component.providerList.get();

        assertEquals("Failed to lookup component with"
            + " type [interface org.xwiki.component.embed.EmbeddableComponentManagerTest$TestRole] and hint [hint1]",
            this.logCapture.getMessage(0));
    }
}
