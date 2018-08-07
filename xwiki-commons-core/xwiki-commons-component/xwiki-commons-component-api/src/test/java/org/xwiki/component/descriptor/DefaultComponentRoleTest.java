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
package org.xwiki.component.descriptor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link DefaultComponentRole}.
 * 
 * @version $Id$
 * @since 3.3M1
 */
public class DefaultComponentRoleTest
{
    private interface Role
    {
    }

    private interface OtherRole
    {
    }

    @Test
    public void equals()
    {
        DefaultComponentRole cr1 = new DefaultComponentRole();
        cr1.setRoleType(Role.class);
        cr1.setRoleHint("hint");

        assertEquals(cr1, cr1);

        DefaultComponentRole cr2 = new DefaultComponentRole();
        cr2.setRoleType(Role.class);
        cr2.setRoleHint("hint");

        assertEquals(cr1, cr2);

        DefaultComponentRole cr3 = new DefaultComponentRole();
        cr3.setRoleType(Role.class);
        cr3.setRoleHint("other");

        assertFalse(cr1.equals(cr3));

        DefaultComponentRole cr4 = new DefaultComponentRole();
        cr4.setRoleType(OtherRole.class);
        cr4.setRoleHint("hint");

        assertFalse(cr1.equals(cr4));
    }
}
