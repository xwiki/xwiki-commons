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
package org.xwiki.netflux.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Utils}.
 * 
 * @version $Id$
 * @since 13.9RC1
 */
class UtilsTest
{
    @Test
    void getRandomHexString()
    {
        String first = Utils.getRandomHexString(48);
        assertHexString(first, 48);

        String second = Utils.getRandomHexString(48);
        assertHexString(second, 48);

        assertFalse(first.equals(second));
    }

    void assertHexString(String input, int length)
    {
        assertTrue(input.matches("\\p{XDigit}{" + length + "}"));
    }
}
