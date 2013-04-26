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

import org.junit.Assert;

import org.junit.Test;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.embed.EmbeddableComponentManagerTest;
import org.xwiki.component.embed.EmbeddableComponentManagerTest.Role;
import org.xwiki.component.embed.EmbeddableComponentManagerTest.RoleImpl;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentRepositoryException;

/**
 * Validate loading and injection of Providers in a real use case.
 * 
 * @version $Id$
 */
public class ProviderTest
{
    @ComponentRole
    public static interface TestComponentRole
    {

    }

    @Component
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
        public Provider<List<EmbeddableComponentManagerTest.Role>> providerList;

        @Inject
        public Provider<Map<String, EmbeddableComponentManagerTest.Role>> providerMap;
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

    @Test
    public void testLoadAndInjectProviders() throws ComponentLookupException, ComponentRepositoryException
    {
        EmbeddableComponentManager cm = new EmbeddableComponentManager();

        // Register components for the list and map
        DefaultComponentDescriptor<Role> cd1 = new DefaultComponentDescriptor<Role>();
        cd1.setRole(Role.class);
        cd1.setRoleHint("hint1");
        cd1.setImplementation(RoleImpl.class);
        cm.registerComponent(cd1);
        DefaultComponentDescriptor<Role> cd2 = new DefaultComponentDescriptor<Role>();
        cd2.setRole(Role.class);
        cd2.setRoleHint("hint2");
        cd2.setImplementation(RoleImpl.class);
        cm.registerComponent(cd2);

        // Initialize
        cm.initialize(getClass().getClassLoader());

        TestComponentWithProviders component = (TestComponentWithProviders) cm.getInstance(TestComponentRole.class);

        Assert.assertEquals("value", component.provider1.get());
        Assert.assertEquals("another value", component.provider12.get());
        Assert.assertEquals(Integer.valueOf(1), component.provider2.get());

        Assert.assertEquals(2, component.providerList.get().size());
        Assert.assertEquals(2, component.providerMap.get().size());
    }
}
