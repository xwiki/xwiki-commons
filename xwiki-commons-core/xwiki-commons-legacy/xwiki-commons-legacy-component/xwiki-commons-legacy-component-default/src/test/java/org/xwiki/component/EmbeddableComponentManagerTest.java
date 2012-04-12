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
        DefaultComponentDescriptor<Role> cd = new DefaultComponentDescriptor<Role>();
        cd.setRole(Role.class);
        cd.setImplementation(RoleImpl.class);
        cm.registerComponent(cd);

        // Here's the test
        Assert.assertNotNull(cm.lookup(Role.class));
        Assert.assertEquals(1, cm.lookupList(Role.class).size());
        Assert.assertEquals(1, cm.lookupMap(Role.class).size());
    }
}
