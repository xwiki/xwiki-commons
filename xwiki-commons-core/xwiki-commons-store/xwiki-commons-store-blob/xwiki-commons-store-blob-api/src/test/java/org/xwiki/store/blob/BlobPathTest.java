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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link BlobPath}.
 *
 * @version $Id$
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
class BlobPathTest
{
    @Test
    void rootHasExpectedProperties()
    {
        BlobPath root = BlobPath.root();
        assertTrue(root.isAbsolute());
        assertTrue(root.isRoot());
        assertEquals("/", root.toString());
        assertNull(root.getParent());
        assertNull(root.getFileName());
        assertEquals(0, root.getNameCount());
    }

    static Stream<Arguments> absoluteFromNamesData()
    {
        return Stream.of(
            Arguments.of(List.of("docs"), "/docs"),
            Arguments.of(List.of("docs", "file.txt"), "/docs/file.txt"),
            Arguments.of(List.of("nested", "dir", "file.bin"), "/nested/dir/file.bin")
        );
    }

    @ParameterizedTest
    @MethodSource("absoluteFromNamesData")
    void absoluteFromNames(List<String> names, String expectedString)
    {
        BlobPath vararg = BlobPath.absolute(names.get(0), names.subList(1, names.size()).toArray(new String[0]));
        BlobPath iterable = BlobPath.absolute(names);

        assertEquals(expectedString, vararg.toString());
        assertEquals(expectedString, iterable.toString());
        assertTrue(vararg.isAbsolute());
        assertTrue(iterable.isAbsolute());
        assertIterableEquals(names, vararg.getNames());
        assertEquals(iterable, BlobPath.parse(expectedString));
    }

    @Test
    void absoluteFromEmptyIterableCreatesRoot()
    {
        BlobPath path = BlobPath.absolute(List.of());
        assertEquals(BlobPath.root(), path);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "..", "seg/ment", "seg\\ment"})
    void absoluteRejectsInvalidNames(String name)
    {
        assertThrows(IllegalArgumentException.class, () -> BlobPath.absolute(name));
    }

    @Test
    void relativeAllowsDotSegments()
    {
        BlobPath relative = BlobPath.relative(List.of("..", "folder", ".", "file.txt"));
        assertFalse(relative.isAbsolute());
        // Now normalized: "../folder/file.txt"
        assertEquals("../folder/file.txt", relative.toString());
        assertIterableEquals(List.of("..", "folder", "file.txt"), relative.getNames());
    }

    @Test
    void relativeFromEmptyIterableCreatesEmptyPath()
    {
        BlobPath path = BlobPath.relative(List.of());
        assertEquals(BlobPath.relative(), path);
        assertEquals("", path.toString());
    }

    static Stream<Arguments> parseValidSamplesData()
    {
        return Stream.of(
            Arguments.of("/", BlobPath.root()),
            Arguments.of("/wiki/spaces/home.txt", BlobPath.absolute("wiki", "spaces", "home.txt")),
            Arguments.of("wiki/spaces/home.txt", BlobPath.relative(List.of("wiki", "spaces", "home.txt"))),
            Arguments.of("../image.png", BlobPath.relative(List.of("..", "image.png"))),
            Arguments.of("", BlobPath.relative())
        );
    }

    @ParameterizedTest
    @MethodSource("parseValidSamplesData")
    void parseValidPaths(String source, BlobPath expected)
    {
        assertEquals(expected, BlobPath.parse(source));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/..", "/../a/b"})
    void parseRejectsInvalidAbsolutePaths(String source)
    {
        assertThrows(IllegalArgumentException.class, () -> BlobPath.parse(source));
    }

    @ParameterizedTest
    @CsvSource({
        "/, ",
        "/attachments, /",
        "/attachments/file.bin, /attachments",
        "attachments, ",
        "attachments/file.bin, attachments",
        "'', "
    })
    void getParentReturnsExpectedPath(String inputPath, String expectedParentPath)
    {
        BlobPath input = BlobPath.parse(inputPath);
        BlobPath expected = expectedParentPath == null ? null : BlobPath.parse(expectedParentPath);
        assertEquals(expected, input.getParent());
    }

    @Test
    void getFileNameExtractsLastName()
    {
        BlobPath path = BlobPath.absolute("a", "b", "file.txt");
        BlobPath fileName = path.getFileName();
        assertEquals("file.txt", fileName.toString());
        assertFalse(fileName.isAbsolute());
    }

    @Test
    void getNameReturnsIndividualName()
    {
        BlobPath path = BlobPath.absolute("a", "b", "c");
        assertEquals("b", path.getName(1).toString());
        assertThrows(IllegalArgumentException.class, () -> path.getName(5));
    }

    @ParameterizedTest
    @CsvSource({
        "/docs, file.txt, /docs/file.txt",
        "docs, file.txt, docs/file.txt",
        "/docs, /other/path, /other/path",
        "/docs, '', /docs",
        "/a/b, ./c, /a/b/c",
        "/a/b, ../c, /a/c"
    })
    void resolveProducesExpectedPath(String basePath, String otherPath, String expectedPath)
    {
        BlobPath base = BlobPath.parse(basePath);
        BlobPath other = BlobPath.parse(otherPath);
        BlobPath expected = BlobPath.parse(expectedPath);
        assertEquals(expected, base.resolve(other));
    }

    @Test
    void resolveAbsoluteReturnsRootWhenApplicable()
    {
        BlobPath result = BlobPath.absolute("docs").resolve(BlobPath.relative(".."));
        assertEquals(BlobPath.root(), result);
    }

    @Test
    void resolveAbsoluteRejectsTraversalOutsideRoot()
    {
        assertThrows(IllegalArgumentException.class,
            () -> BlobPath.root().resolve(BlobPath.relative("..")));
    }

    @Test
    void resolveAbsoluteReportsAccurateInvalidSegmentIndex()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> BlobPath.absolute("a").resolve(BlobPath.relative("..", "bad/name")));
        assertEquals("Name at index 1 contains an illegal separator: bad/name", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "/docs/file.txt, other.txt, /docs/other.txt",
        "/docs, other.txt, /other.txt",
        "docs/file.txt, other.txt, docs/other.txt",
        "file.txt, other.txt, other.txt",
        "/docs/sub/file.txt, ../other.txt, /docs/other.txt",
        "/docs/sub/file.txt, ./temp.txt, /docs/sub/temp.txt"
    })
    void resolveSiblingProducesExpectedPath(String basePath, String otherPath, String expectedPath)
    {
        BlobPath base = BlobPath.parse(basePath);
        BlobPath other = BlobPath.parse(otherPath);
        BlobPath expected = BlobPath.parse(expectedPath);
        assertEquals(expected, base.resolveSibling(other));
    }

    @ParameterizedTest
    @CsvSource({
        "/docs/old, /docs/new, ../new",
        "/docs, /docs/more/file.txt, more/file.txt",
        "docs/old, docs/new, ../new",
        "docs, docs, ''"
    })
    void relativizeProducesExpectedPath(String basePath, String otherPath, String expectedPath)
    {
        BlobPath base = BlobPath.parse(basePath);
        BlobPath other = BlobPath.parse(otherPath);
        BlobPath expected = BlobPath.parse(expectedPath);
        assertEquals(expected, base.relativize(other));
    }

    @Test
    void relativizeRejectsMixedAbsoluteness()
    {
        assertThrows(IllegalArgumentException.class,
            () -> BlobPath.absolute("a").relativize(BlobPath.relative("a")));
    }

    @ParameterizedTest
    @CsvSource({
        "/a/b/c, /a, true",
        "/a/b/c, a, false",
        "a/b/c, a/b, true",
        "a/b/c, a/c, false"
    })
    void startsWithMatchesExpectation(String pathStr, String otherStr, boolean expected)
    {
        BlobPath path = BlobPath.parse(pathStr);
        BlobPath other = BlobPath.parse(otherStr);
        assertEquals(expected, path.startsWith(other));
    }

    @ParameterizedTest
    @CsvSource({
        "/a/b/c, c, true",
        "/a/b/c, /a/b/c, true",
        "/a/b/c, /b/c, false",
        "x/y/z, y/z, true",
        "x/y/z, /x/y/z, false"
    })
    void endsWithMatchesExpectation(String pathStr, String otherStr, boolean expected)
    {
        BlobPath path = BlobPath.parse(pathStr);
        BlobPath other = BlobPath.parse(otherStr);
        assertEquals(expected, path.endsWith(other));
    }

    @Test
    void subpathExtractsPortion()
    {
        BlobPath path = BlobPath.absolute("a", "b", "c", "d");
        BlobPath sub = path.subpath(1, 3);
        assertEquals("b/c", sub.toString());
        assertFalse(sub.isAbsolute());
        assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 2));
    }

    @Test
    void iteratorYieldsNamesInOrder()
    {
        BlobPath path = BlobPath.absolute("a", "b", "c");
        List<String> names = new ArrayList<>();
        for (BlobPath name : path) {
            names.add(name.toString());
        }
        assertEquals(List.of("a", "b", "c"), names);
    }

    @Test
    void compareToSortsByCanonicalOrder()
    {
        List<BlobPath> paths = List.of(
            BlobPath.absolute("b"),
            BlobPath.absolute("a"),
            BlobPath.relative("c"),
            BlobPath.relative("a"),
            BlobPath.root()
        );

        List<BlobPath> sorted = paths.stream().sorted().toList();

        assertEquals(List.of("/", "/a", "/b", "a", "c"),
            sorted.stream().map(BlobPath::toString).toList());
    }

    @ParameterizedTest
    @CsvSource({
        "file.txt, true",
        "., false",
        ".., false",
        "dir/file, false",
        "'', false"
    })
    void isValidNameMatchesExpectations(String name, boolean expected)
    {
        assertEquals(expected, BlobPath.isValidName(name));
    }

    @Test
    void constructorsNormalizeDotSegments()
    {
        // Absolute: "/a/./b/.." -> "/a"
        BlobPath abs = BlobPath.absolute("a", ".", "b", "..", ".");
        assertEquals("/a", abs.toString());
        assertIterableEquals(List.of("a"), abs.getNames());
        // Relative: "..", ".", "a", "..", "b" -> "../b"
        BlobPath rel = BlobPath.relative("..", ".", "a", "..", "b");
        assertEquals("../b", rel.toString());
        assertIterableEquals(List.of("..", "b"), rel.getNames());
    }
    @Test
    void parseNormalizesDotSegments()
    {
        assertEquals("/a", BlobPath.parse("/a/./b/..").toString());
        assertEquals("../b", BlobPath.parse(".././a/../b").toString());
    }
}
