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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for BlobPath.
 *
 * @version $Id$
 */
class BlobPathTest
{
    @Test
    void testRootIsEmptyAndEqualsOfEmpty()
    {
        BlobPath root = BlobPath.ROOT;
        BlobPath empty = BlobPath.of(List.of());
        assertNotNull(root);
        assertEquals("", root.toString());
        assertEquals("", root.getName());
        assertEquals(empty, root);
        assertEquals(empty.hashCode(), root.hashCode());
    }

    @Test
    void testOfCreatesPathAndAccessors()
    {
        // Create a path and verify segments, name, and canonical string.
        BlobPath p = BlobPath.of(List.of("a", "b", "c"));
        assertEquals(List.of("a", "b", "c"), p.getSegments());
        assertEquals("c", p.getName());
        assertEquals("a/b/c", p.toString());
    }

    @Test
    void testFromSplitsPath()
    {
        // From should split slash-delimited strings (ignores empty parts).
        BlobPath p = BlobPath.from("a/b/c");
        assertEquals(BlobPath.of(List.of("a", "b", "c")), p);

        // Consecutive slashes and leading/trailing slashes are handled (no empty segments).
        BlobPath q = BlobPath.from("/a//b/c/");
        assertEquals(BlobPath.of(List.of("a", "b", "c")), q);
    }

    @Test
    void testResolveAppendsSegmentsAndNoOp()
    {
        // Resolve should append new segments and be no-op for empty input.
        BlobPath base = BlobPath.of(List.of("x"));
        BlobPath resolved = base.resolve("y", "z");
        assertEquals("x/y/z", resolved.toString());

        BlobPath same = base.resolve();
        assertSame(base, same);
    }

    @Test
    void testGetParentBehavior()
    {
        // Parent of root and single-segment returns root; multi-segment returns trimmed path.
        assertEquals(BlobPath.ROOT, BlobPath.of(List.of()).getParent());
        assertEquals(BlobPath.ROOT, BlobPath.of(List.of("one")).getParent());

        BlobPath multi = BlobPath.of(List.of("a", "b", "c"));
        assertEquals(BlobPath.of(List.of("a", "b")), multi.getParent());
    }

    @Test
    void testAppendSuffix()
    {
        // AppendSuffix should append to last segment; for empty path it creates a single segment.
        BlobPath p = BlobPath.of(List.of("file"));
        BlobPath suffixed = p.appendSuffix(".bak");
        assertEquals("file.bak", suffixed.getName());
        assertEquals("file.bak", suffixed.toString());

        BlobPath rootWithSuffix = BlobPath.ROOT.appendSuffix("new");
        assertEquals(BlobPath.of(List.of("new")), rootWithSuffix);

        // blank suffix should throw
        assertThrows(IllegalArgumentException.class, () -> p.appendSuffix("  "));
    }

    @Test
    void testValidationNullSegmentsList()
    {
        // of(null) should fail with IllegalArgumentException per constructor check.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> BlobPath.of(null));
        assertTrue(ex.getMessage().contains("segments must not be null"));
    }

    @Test
    void testValidationNullSegmentElement()
    {
        // null element in segments should be rejected with informative message.
        List<String> segments = Arrays.asList("ok", null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> BlobPath.of(segments));
        assertTrue(ex.getMessage().contains("Segment at index 1 is null"));
    }

    @Test
    void testValidationEmptySegment()
    {
        // Empty segment not allowed.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> BlobPath.of(List.of("a", "")));
        assertTrue(ex.getMessage().contains("Segment at index 1 is empty"));
    }

    @Test
    void testValidationDirectoryTraversal()
    {
        // '.' or '..' are rejected.
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> BlobPath.of(List.of("a", ".")));
        assertTrue(ex1.getMessage().contains("is a directory traversal"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> BlobPath.of(List.of("..")));
        assertTrue(ex2.getMessage().contains("is a directory traversal"));
    }

    @Test
    void testValidationIllegalSeparatorCharacters()
    {
        // Segments cannot contain '/' or '\'.
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> BlobPath.of(List.of("bad/seg")));
        assertTrue(ex1.getMessage().contains("contains an illegal path separator"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> BlobPath.of(List.of("bad\\seg")));
        assertTrue(ex2.getMessage().contains("contains an illegal path separator"));
    }

    @Test
    void testFromNullThrows()
    {
        IllegalArgumentException illegalArgumentException =
            assertThrows(IllegalArgumentException.class, () -> BlobPath.from(null));
        assertTrue(illegalArgumentException.getMessage().contains("path must not be null"));
    }

    @Test
    void testFromEmptyIsRoot()
    {
        // From("") should return ROOT.
        BlobPath p = BlobPath.from("");
        assertSame(BlobPath.ROOT, p);
    }

    @Test
    void testResolveWithInvalidSegmentThrows()
    {
        // Resolve should validate new segments via constructor and therefore throw on invalid input.
        BlobPath base = BlobPath.of(List.of("a"));
        assertThrows(IllegalArgumentException.class, () -> base.resolve("ok", ".."));
    }

    @Test
    void testEqualsAndHashCodeConsistency()
    {
        // Equal paths must be equal and have equal hash codes.
        BlobPath a = BlobPath.of(List.of("x", "y"));
        BlobPath b = BlobPath.of(List.of("x", "y"));
        BlobPath c = BlobPath.of(List.of("x", "z"));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
