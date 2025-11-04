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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
    void rootIsEmptyAndEqualsOfEmpty()
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
    void ofCreatesPathAndAccessors()
    {
        // Create a path and verify segments, name, and canonical string.
        BlobPath p = BlobPath.of(List.of("a", "b", "c"));
        assertEquals(List.of("a", "b", "c"), p.getSegments());
        assertEquals("c", p.getName());
        assertEquals("a/b/c", p.toString());
    }

    @Test
    void fromSplitsPath()
    {
        // From should split slash-delimited strings (ignores empty parts).
        BlobPath p = BlobPath.from("a/b/c");
        assertEquals(BlobPath.of(List.of("a", "b", "c")), p);

        // Consecutive slashes and leading/trailing slashes are handled (no empty segments).
        BlobPath q = BlobPath.from("/a//b/c/");
        assertEquals(BlobPath.of(List.of("a", "b", "c")), q);
    }

    @Test
    void resolveAppendsSegmentsAndNoOp()
    {
        // Resolve should append new segments and be no-op for empty input.
        BlobPath base = BlobPath.of(List.of("x"));
        BlobPath resolved = base.resolve("y", "z");
        assertEquals("x/y/z", resolved.toString());

        BlobPath same = base.resolve();
        assertSame(base, same);
    }

    @Test
    void getParentBehavior()
    {
        // Parent of root and single-segment returns root; multi-segment returns trimmed path.
        assertEquals(BlobPath.ROOT, BlobPath.of(List.of()).getParent());
        assertEquals(BlobPath.ROOT, BlobPath.of(List.of("one")).getParent());

        BlobPath multi = BlobPath.of(List.of("a", "b", "c"));
        assertEquals(BlobPath.of(List.of("a", "b")), multi.getParent());
    }

    @Test
    void appendSuffix()
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
    void validationNullSegmentsList()
    {
        // of(null) should fail with IllegalArgumentException per constructor check.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> BlobPath.of(null));
        assertTrue(ex.getMessage().contains("segments must not be null"));
    }

    @Test
    void validationNullSegmentElement()
    {
        // null element in segments should be rejected with informative message.
        List<String> segments = Arrays.asList("ok", null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> BlobPath.of(segments));
        assertTrue(ex.getMessage().contains("Segment at index 1 is null"));
    }

    @Test
    void validationEmptySegment()
    {
        // Empty segment not allowed.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> BlobPath.of(List.of("a", "")));
        assertTrue(ex.getMessage().contains("Segment at index 1 is empty"));
    }

    @Test
    void validationDirectoryTraversal()
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
    void validationIllegalSeparatorCharacters()
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
    void fromNullThrows()
    {
        IllegalArgumentException illegalArgumentException =
            assertThrows(IllegalArgumentException.class, () -> BlobPath.from(null));
        assertTrue(illegalArgumentException.getMessage().contains("path must not be null"));
    }

    @Test
    void fromEmptyIsRoot()
    {
        // From("") should return ROOT.
        BlobPath p = BlobPath.from("");
        assertSame(BlobPath.ROOT, p);
    }

    @Test
    void resolveWithInvalidSegmentThrows()
    {
        // Resolve should validate new segments via constructor and therefore throw on invalid input.
        BlobPath base = BlobPath.of(List.of("a"));
        assertThrows(IllegalArgumentException.class, () -> base.resolve("ok", ".."));
    }

    @Test
    void equalsAndHashCodeConsistency()
    {
        // Equal paths must be equal and have equal hash codes.
        BlobPath a = BlobPath.of(List.of("x", "y"));
        BlobPath b = BlobPath.of(List.of("x", "y"));
        BlobPath c = BlobPath.of(List.of("x", "z"));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @ParameterizedTest
    @CsvSource({
        // Root is ancestor of everything including itself
        "'', '', true",
        "'', 'a', true",
        "'', 'a/b', true",
        "'', 'a/b/c', true",

        // Same path equals itself
        "'a', 'a', true",
        "'a/b', 'a/b', true",
        "'a/b/c', 'a/b/c', true",

        // Proper ancestor relationships
        "'a', 'a/b', true",
        "'a', 'a/b/c', true",
        "'a/b', 'a/b/c', true",
        "'a/b', 'a/b/c/d', true",

        // Not ancestors - different branches
        "'a', 'b', false",
        "'a/b', 'a/c', false",
        "'a/b', 'x/y', false",

        // Not ancestors - child cannot be ancestor of parent
        "'a/b', 'a', false",
        "'a/b/c', 'a/b', false",
        "'a/b/c', 'a', false",

        // Not ancestors - partial prefix match
        "'a/b', 'a/bc', false",
        "'ab', 'a/b', false"
    })
    void isAncestorOfOrEquals(String thisPath, String otherPath, boolean expected)
    {
        BlobPath thisBlob = BlobPath.from(thisPath);
        BlobPath otherBlob = BlobPath.from(otherPath);

        assertEquals(expected, thisBlob.isAncestorOfOrEquals(otherBlob),
            String.format("Expected '%s'.isAncestorOfOrEquals('%s') to be %b", thisPath, otherPath, expected));
    }
}
