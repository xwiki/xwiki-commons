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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

/**
 * Unit tests for {@link IdGenerator}.
 * 
 * @version $Id$
 */
@ComponentTest
class IdGeneratorTest
{
    @InjectMockComponents
    private IdGenerator idGenerator;

    @Test
    void getRandomHexString()
    {
        String first = this.idGenerator.generateChannelId();
        assertHexString(first, 48);

        String second = this.idGenerator.generateChannelId();
        assertHexString(second, 48);

        assertNotEquals(first, second);
    }

    void assertHexString(String input, int length)
    {
        assertTrue(input.matches("\\p{XDigit}{" + length + "}"));
    }
}
