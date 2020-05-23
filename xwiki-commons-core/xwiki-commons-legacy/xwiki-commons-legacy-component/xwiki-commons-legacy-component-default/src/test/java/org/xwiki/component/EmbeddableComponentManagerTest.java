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

import org.junit.jupiter.api.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test to prove that we keep backward compatibility for the {@link EmbeddableComponentManager} class.
 *
 * @version $Id$
 * @since 4.1M1
 */
class EmbeddableComponentManagerTest
{
    public static interface Role
    {
    }

    public static class RoleImpl implements Role
    {
    }

    @Test
    void lookupComponent() throws Exception
    {
        ComponentManager cm = new EmbeddableComponentManager();

        // Register without hint
        DefaultComponentDescriptor<Role> cd1 = new DefaultComponentDescriptor<Role>();
        cd1.setRole(Role.class);
        cd1.setImplementation(RoleImpl.class);
        cm.registerComponent(cd1);

        // Register with a hint
        DefaultComponentDescriptor<Role> cd2 = new DefaultComponentDescriptor<Role>();
        cd2.setRole(Role.class);
        cd2.setRoleHint("hint");
        cd2.setImplementation(RoleImpl.class);
        cm.registerComponent(cd2);

        // Here are the tests, calling deprecated APIs
        assertNotNull(cm.lookup(Role.class));
        assertNotNull(cm.lookup(Role.class, "hint"));
        assertEquals(2, cm.lookupList(Role.class).size());
        assertEquals(2, cm.lookupMap(Role.class).size());
        assertTrue(cm.hasComponent(Role.class));
        assertTrue(cm.hasComponent(Role.class, "hint"));
        assertEquals(cd2, cm.getComponentDescriptor(Role.class, "hint"));

        cm.unregisterComponent(Role.class, "hint");
        assertEquals(1, cm.lookupList(Role.class).size());
    }
}
