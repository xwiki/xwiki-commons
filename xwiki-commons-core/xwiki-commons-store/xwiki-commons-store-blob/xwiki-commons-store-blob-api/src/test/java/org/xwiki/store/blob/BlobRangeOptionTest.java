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
package org.xwiki.store.blob;

import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link BlobRangeOption}.
 *
 * @version $Id$
 */
class BlobRangeOptionTest
{
    @Test
    void fromOffset()
    {
        BlobRangeOption option = BlobRangeOption.from(100);

        assertEquals(100, option.getStartOffset());
        assertFalse(option.getEndOffset().isPresent());
        assertFalse(option.getLength().isPresent());
    }

    @Test
    void fromOffsetRejectsNegative()
    {
        assertThrows(IllegalArgumentException.class, () -> BlobRangeOption.from(-1));
    }

    @Test
    void withLength()
    {
        BlobRangeOption option = BlobRangeOption.withLength(50, 25);

        assertEquals(50, option.getStartOffset());
        assertEquals(OptionalLong.of(74), option.getEndOffset());
        assertEquals(OptionalLong.of(25), option.getLength());
    }

    @Test
    void between()
    {
        BlobRangeOption option = BlobRangeOption.between(10, 20);

        assertEquals(10, option.getStartOffset());
        assertEquals(OptionalLong.of(20), option.getEndOffset());
        assertEquals(OptionalLong.of(11), option.getLength());
    }

    @Test
    void withLengthRejectsNonPositiveValues()
    {
        assertThrows(IllegalArgumentException.class, () -> BlobRangeOption.withLength(10, 0));
        assertThrows(IllegalArgumentException.class, () -> BlobRangeOption.withLength(10, -5));
    }

    @Test
    void withLengthRejectsOverflow()
    {
        assertThrows(IllegalArgumentException.class,
            () -> BlobRangeOption.withLength(Long.MAX_VALUE - 5, 10));
    }

    @Test
    void betweenRejectsInvalidRange()
    {
        assertThrows(IllegalArgumentException.class, () -> BlobRangeOption.between(10, 5));
    }

    @Test
    void equalsAndHashCode()
    {
        BlobRangeOption first = BlobRangeOption.withLength(5, 5);
        BlobRangeOption second = BlobRangeOption.between(5, 9);
        BlobRangeOption different = BlobRangeOption.from(5);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(different));
    }
}
