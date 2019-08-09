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
package org.xwiki.diff.internal;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.xwiki.diff.Chunk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test of {@link DefaultChunk}.
 */
public class DefaultChunkTest
{
    @Test
    public void isOverlapping()
    {
        Chunk<Integer> chunk1 = new DefaultChunk<>(5, Arrays.asList(1, 2, 3, 4, 5));
        Chunk<Integer> chunk2 = new DefaultChunk<>(5, Arrays.asList(1, 2));

        // [5, 10] intersection [5, 7] != emptyset
        assertTrue(chunk1.isOverlappingWith(chunk2));
        assertTrue(chunk2.isOverlappingWith(chunk1));

        // [5, 10] intersection [3, 6] != emptyset
        chunk2 = new DefaultChunk<>(3, Arrays.asList(1, 2, 3));
        assertTrue(chunk1.isOverlappingWith(chunk2));
        assertTrue(chunk2.isOverlappingWith(chunk1));

        // [5, 10] intersection [2, 4] == emptyset
        chunk2 = new DefaultChunk<>(2, Arrays.asList(1, 2));
        assertFalse(chunk1.isOverlappingWith(chunk2));
        assertFalse(chunk2.isOverlappingWith(chunk1));

        // [5, 10] intersection [2, 5] != emptyset
        chunk2 = new DefaultChunk<>(2, Arrays.asList(1, 2, 3));
        assertTrue(chunk1.isOverlappingWith(chunk2));
        assertTrue(chunk2.isOverlappingWith(chunk1));

        // [5, 10] intersection [6, 8] != emptyset
        chunk2 = new DefaultChunk<>(6, Arrays.asList(1, 2, 3));
        assertTrue(chunk1.isOverlappingWith(chunk2));
        assertTrue(chunk2.isOverlappingWith(chunk1));

        // [5, 10] intersection [10, 10] != emptyset
        chunk2 = new DefaultChunk<>(10, Collections.emptyList());
        assertTrue(chunk1.isOverlappingWith(chunk2));
        assertTrue(chunk2.isOverlappingWith(chunk1));

        // [5, 10] intersection [11, 12] == emptyset
        chunk2 = new DefaultChunk<>(11, Arrays.asList(1, 2));
        assertFalse(chunk1.isOverlappingWith(chunk2));
        assertFalse(chunk2.isOverlappingWith(chunk1));

        // [5, 10] intersection [3, 16] != emptyset
        chunk2 = new DefaultChunk<>(3, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13));
        assertTrue(chunk1.isOverlappingWith(chunk2));
        assertTrue(chunk2.isOverlappingWith(chunk1));
    }
}
