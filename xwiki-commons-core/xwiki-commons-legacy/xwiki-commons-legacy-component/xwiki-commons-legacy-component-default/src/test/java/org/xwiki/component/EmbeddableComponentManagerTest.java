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

import org.junit.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentManager;

import junit.framework.Assert;

/**
 * Unit test to prove that we keep backward compatibility for the {@link EmbeddableComponentManager} class.
 *
 * @version $Id$
 * @since 4.1M1
 */
public class EmbeddableComponentManagerTest
{
    public static interface Role
    {
    }

    public static class RoleImpl implements Role
    {
    }

    @Test
    public void lookupComponent() throws Exception
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
        Assert.assertNotNull(cm.lookup(Role.class));
        Assert.assertNotNull(cm.lookup(Role.class, "hint"));
        Assert.assertEquals(2, cm.lookupList(Role.class).size());
        Assert.assertEquals(2, cm.lookupMap(Role.class).size());
        Assert.assertTrue(cm.hasComponent(Role.class));
        Assert.assertTrue(cm.hasComponent(Role.class, "hint"));
        Assert.assertEquals(cd2, cm.getComponentDescriptor(Role.class, "hint"));

        cm.unregisterComponent(Role.class, "hint");
        Assert.assertEquals(1, cm.lookupList(Role.class).size());
    }
}
