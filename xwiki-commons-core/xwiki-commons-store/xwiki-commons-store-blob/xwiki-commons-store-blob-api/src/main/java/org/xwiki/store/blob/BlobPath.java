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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.stability.Unstable;

/**
 * A hierarchical identifier for blobs stored inside a {@link BlobStore}. The API mirrors {@link java.nio.file.Path}
 * where it makes sense while keeping the semantics that blob paths are always relative to a {@link BlobStore}
 * instance instead of the underlying file system.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public final class BlobPath implements Comparable<BlobPath>, Iterable<BlobPath>
{
    private static final BlobPath ROOT_INSTANCE = new BlobPath(true, List.of());

    private static final BlobPath EMPTY_RELATIVE = new BlobPath(false, List.of());

    private static final String SEPARATOR = "/";

    private static final String OTHER_CANNOT_BE_NULL = "other must not be null";

    private static final String NAMES_CANNOT_BE_NULL = "names must not be null";

    private static final String RELATIVE_UP = "..";

    private static final String SAME_DIRECTORY = ".";

    private final boolean absolute;

    private final List<String> names;

    private final String canonical;

    private BlobPath(boolean absolute, List<String> names)
    {
        this.absolute = absolute;
        this.names = List.copyOf(names);
        this.canonical = buildCanonical();
    }

    private String buildCanonical()
    {
        if (this.absolute) {
            if (this.names.isEmpty()) {
                return SEPARATOR;
            }
            return SEPARATOR + String.join(SEPARATOR, this.names);
        }
        if (this.names.isEmpty()) {
            return "";
        }
        return String.join(SEPARATOR, this.names);
    }

    /**
     * @return the root path
     */
    public static BlobPath root()
    {
        return ROOT_INSTANCE;
    }

    /**
     * Normalize a list of path segments by resolving dot-segments ("." and "..") according to RFC-3986 rules.
     * For absolute paths, throws if normalization would go above root. For relative paths, preserves only leading "..".
     *
     * @param segments the input segments (already validated for null/empty/illegal separators)
     * @param absolute true if the path is absolute
     * @return normalized list of segments
     */
    private static List<String> normalize(List<String> segments, boolean absolute)
    {
        List<String> stack = new ArrayList<>(segments.size());
        // Invariant: stack contains no "." or ".." except possibly at the start for relative paths.
        for (String seg : segments) {
            if (RELATIVE_UP.equals(seg)) {
                // If we see "..", pop a name off the stack if possible.
                if (!stack.isEmpty() && !stack.getLast().equals(RELATIVE_UP)) {
                    stack.removeLast();
                } else if (absolute) {
                    // Cannot go above root.
                    throw new IllegalArgumentException("Cannot resolve path outside the root");
                } else {
                    // Preserve leading ".." for relative paths.
                    stack.add(seg);
                }
            } else if (!SAME_DIRECTORY.equals(seg)) {
                // skip ".".
                stack.add(seg);
            }
        }

        return stack;
    }

    /**
     * Create an absolute path from validated names. Dot-segments ("." and "..") are not allowed for absolute paths
     * and will cause an {@link IllegalArgumentException}.
     * No normalization is performed for absolute paths.
     *
     * @param names the names to use
     * @return an absolute {@link BlobPath}
     * @throws IllegalArgumentException if a provided name is "." or "..", null/empty, or contains a separator
     */
    public static BlobPath absolute(String... names)
    {
        List<String> nameList = names == null ? List.of() : Arrays.asList(names);
        // Disallow dot segments for absolute paths.
        validateNames(nameList, false);
        return nameList.isEmpty() ? ROOT_INSTANCE : new BlobPath(true, nameList);
    }

    /**
     * Create an absolute path from the given names. Dot-segments ("." and "..") are not allowed for absolute paths
     * and will cause an {@link IllegalArgumentException}.
     * No normalization is performed for absolute paths.
     *
     * @param names the names to use
     * @return an absolute {@link BlobPath}
     * @throws IllegalArgumentException if a provided name is "." or "..", null/empty, or contains a separator
     */
    public static BlobPath absolute(Iterable<String> names)
    {
        Objects.requireNonNull(names, NAMES_CANNOT_BE_NULL);
        List<String> nameList = buildNames(names);
        validateNames(nameList, false);
        return nameList.isEmpty() ? ROOT_INSTANCE : new BlobPath(true, nameList);
    }

    /**
     * Create a relative path from names. Dot-segments ("." and "..") are allowed and will be normalized immediately.
     *
     * @param names the names to use
     * @return a relative {@link BlobPath}
     */
    public static BlobPath relative(String... names)
    {
        List<String> nameList = names == null ? List.of() : Arrays.asList(names);
        validateNames(nameList, true);
        List<String> normalized = normalize(nameList, false);
        return normalized.isEmpty() ? EMPTY_RELATIVE : new BlobPath(false, normalized);
    }

    /**
     * Create a relative path from the given names. Dot-segments ("." and "..") are allowed and will be normalized
     * immediately.
     *
     * @param names the names to use
     * @return a relative {@link BlobPath}
     */
    public static BlobPath relative(Iterable<String> names)
    {
        Objects.requireNonNull(names, NAMES_CANNOT_BE_NULL);
        List<String> nameList = buildNames(names);
        validateNames(nameList, true);
        List<String> normalized = normalize(nameList, false);
        return normalized.isEmpty() ? EMPTY_RELATIVE : new BlobPath(false, normalized);
    }

    /**
     * Parse a slash-delimited path string into a {@link BlobPath}. Paths starting with {@code '/'} are treated as
     * absolute, otherwise the path is relative.
     * For absolute paths, dot-segments ("." and "..") are disallowed and no normalization is performed.
     * For relative paths, dot-segments are allowed and will be normalized immediately.
     *
     * @param path the path to parse
     * @return the parsed {@link BlobPath}
     * @throws IllegalArgumentException if an absolute path contains "." or "..", or if normalization for a relative
     * path would go above root
     */
    public static BlobPath parse(CharSequence path)
    {
        Objects.requireNonNull(path, "path must not be null");
        String value = path.toString();
        if (value.isEmpty()) {
            return relative();
        }
        boolean absolute = value.startsWith(SEPARATOR);

        // Use StringUtils.split to split on the separator character while skipping empty segments ("//").
        String[] parts = StringUtils.split(value, SEPARATOR.charAt(0));
        List<String> names = parts == null ? List.of() : Arrays.asList(parts);

        validateNames(names, !absolute);
        if (absolute) {
            return names.isEmpty() ? ROOT_INSTANCE : new BlobPath(true, names);
        }
        List<String> normalized = normalize(names, false);
        return normalized.isEmpty() ? EMPTY_RELATIVE : new BlobPath(false, normalized);
    }

    /**
     * @return {@code true} if this path is absolute
     */
    public boolean isAbsolute()
    {
        return this.absolute;
    }

    /**
     * @return {@code true} if this path is the root
     */
    public boolean isRoot()
    {
        return this.absolute && this.names.isEmpty();
    }

    /**
     * @return the root path if this path is absolute, otherwise {@code null}
     */
    public BlobPath getRoot()
    {
        return this.absolute ? ROOT_INSTANCE : null;
    }

    /**
     * @return the parent path or {@code null} if there is no parent
     */
    public BlobPath getParent()
    {
        if (this.names.isEmpty()) {
            return null;
        }
        if (this.names.size() == 1) {
            return this.absolute ? ROOT_INSTANCE : null;
        }
        return new BlobPath(this.absolute, this.names.subList(0, this.names.size() - 1));
    }

    /**
     * @return the file name portion of the path or {@code null} if this path has no names
     */
    public BlobPath getFileName()
    {
        if (this.names.isEmpty()) {
            return null;
        }
        String name = this.names.getLast();
        return name.isEmpty() ? EMPTY_RELATIVE : new BlobPath(false, List.of(name));
    }

    /**
     * @return the number of names in this path
     */
    public int getNameCount()
    {
        return this.names.size();
    }

    /**
     * @param index the index of the name to retrieve
     * @return the name at the given index as a {@link BlobPath}
     */
    public BlobPath getName(int index)
    {
        if (index < 0 || index >= this.names.size()) {
            throw new IllegalArgumentException("Index out of bounds: %d".formatted(index));
        }
        return new BlobPath(false, List.of(this.names.get(index)));
    }

    /**
     * @return the names of this path as an immutable list
     */
    public List<String> getNames()
    {
        return this.names;
    }

    /**
     * Resolve the given path against this path. If the other path is relative, segments are concatenated and
     * normalized.
     *
     * @param other the path to resolve
     * @return the resolved path
     * @throws IllegalArgumentException if normalization would go above root
     */
    public BlobPath resolve(BlobPath other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        if (other.isAbsolute()) {
            return other;
        }
        if (other.names.isEmpty()) {
            return this;
        }
        List<String> combined = new ArrayList<>(this.names.size() + other.names.size());
        combined.addAll(this.names);
        combined.addAll(other.names);
        List<String> normalized = normalize(combined, this.absolute);
        if (this.absolute) {
            return normalized.isEmpty() ? ROOT_INSTANCE : new BlobPath(true, normalized);
        }
        return normalized.isEmpty() ? EMPTY_RELATIVE : new BlobPath(false, normalized);
    }

    /**
     * Resolve the given path string against this path.
     *
     * @param other the path string to resolve
     * @return the resolved path
     */
    public BlobPath resolve(String other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        return resolve(parse(other));
    }

    /**
     * Resolve a sibling path.
     *
     * @param other the sibling path to resolve
     * @return the resolved path
     */
    public BlobPath resolveSibling(BlobPath other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        if (other.isAbsolute()) {
            return other;
        }
        BlobPath parent = getParent();
        if (parent == null) {
            return other;
        }
        return parent.resolve(other);
    }

    /**
     * Resolve a sibling path.
     *
     * @param other the sibling path string to resolve
     * @return the resolved path
     */
    public BlobPath resolveSibling(String other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        return resolveSibling(parse(other));
    }

    /**
     * Relativize the given path against this path.
     *
     * @param other the path to relativize
     * @return the relative path from this path to the other path
     */
    public BlobPath relativize(BlobPath other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        if (this.absolute != other.absolute) {
            throw new IllegalArgumentException("Paths must both be absolute or both be relative.");
        }
        if (this.equals(other)) {
            return EMPTY_RELATIVE;
        }
        int commonLength = commonPrefixLength(other);
        List<String> relative = new ArrayList<>();
        for (int i = commonLength; i < this.names.size(); i++) {
            relative.add(RELATIVE_UP);
        }
        relative.addAll(other.names.subList(commonLength, other.names.size()));
        return relative.isEmpty() ? EMPTY_RELATIVE : new BlobPath(false, relative);
    }

    /**
     * Relativize the given path string against this path.
     *
     * @param other the path string to relativize
     * @return the relative path from this path to the other path
     */
    public BlobPath relativize(String other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        return relativize(parse(other));
    }

    /**
     * @param beginIndex the start index, inclusive
     * @param endIndex the end index, exclusive
     * @return the subpath defined by the given range
     */
    public BlobPath subpath(int beginIndex, int endIndex)
    {
        if (beginIndex < 0 || endIndex > this.names.size() || beginIndex >= endIndex) {
            throw new IllegalArgumentException(
                "Invalid subpath range [%d, %d) for path of size %d"
                    .formatted(beginIndex, endIndex, this.names.size()));
        }
        return new BlobPath(false, this.names.subList(beginIndex, endIndex));
    }

    /**
     * @param other the path to test
     * @return {@code true} if this path starts with the other path
     */
    public boolean startsWith(BlobPath other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        if (other.names.size() > this.names.size()) {
            return false;
        }
        if (other.absolute != this.absolute) {
            return false;
        }
        for (int i = 0; i < other.names.size(); i++) {
            if (!this.names.get(i).equals(other.names.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param other the path string to test
     * @return {@code true} if this path starts with the other path
     */
    public boolean startsWith(String other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        return startsWith(parse(other));
    }

    /**
     * @param other the path to test
     * @return {@code true} if this path ends with the other path
     */
    public boolean endsWith(BlobPath other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        if (other.names.size() > this.names.size()) {
            return false;
        }
        if (other.absolute) {
            return this.absolute && this.names.equals(other.names);
        }
        int offset = this.names.size() - other.names.size();
        for (int i = 0; i < other.names.size(); i++) {
            if (!this.names.get(offset + i).equals(other.names.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param other the path string to test
     * @return {@code true} if this path ends with the other path
     */
    public boolean endsWith(String other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        return endsWith(parse(other));
    }

    private static List<String> buildNames(Iterable<String> sources)
    {
        Objects.requireNonNull(sources, NAMES_CANNOT_BE_NULL);
        List<String> names = new ArrayList<>();
        sources.forEach(names::add);
        return names;
    }

    private static void validateNames(List<String> names, boolean allowDotSegments)
    {
        for (int i = 0; i < names.size(); i++) {
            validateName(names.get(i), i, allowDotSegments);
        }
    }

    private static void validateName(String name, int index, boolean allowDotSegments)
    {
        if (name == null) {
            throw new IllegalArgumentException("Name at index %d must not be null".formatted(index));
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name at index %d must not be empty".formatted(index));
        }
        if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) {
            throw new IllegalArgumentException(
                "Name at index %d contains an illegal separator: %s".formatted(index, name));
        }
        if (!allowDotSegments && (SAME_DIRECTORY.equals(name) || RELATIVE_UP.equals(name))) {
            throw new IllegalArgumentException(
                "Name at index %d must not be '.' or '..' for absolute paths".formatted(index));
        }
    }

    private int commonPrefixLength(BlobPath other)
    {
        int count = Math.min(this.names.size(), other.names.size());
        for (int i = 0; i < count; i++) {
            if (!this.names.get(i).equals(other.names.get(i))) {
                return i;
            }
        }
        return count;
    }


    @Override
    public Iterator<BlobPath> iterator()
    {
        return new BlobPathIterator();
    }

    @Override
    public int compareTo(BlobPath other)
    {
        Objects.requireNonNull(other, OTHER_CANNOT_BE_NULL);
        return this.canonical.compareTo(other.canonical);
    }

    @Override
    public String toString()
    {
        return this.canonical;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlobPath other)) {
            return false;
        }
        return this.absolute == other.absolute && this.names.equals(other.names);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.absolute, this.names);
    }

    private final class BlobPathIterator implements Iterator<BlobPath>
    {
        private int index;

        @Override
        public boolean hasNext()
        {
            return this.index < BlobPath.this.names.size();
        }

        @Override
        public BlobPath next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements");
            }

            BlobPath name = getName(this.index);
            this.index++;
            return name;
        }
    }
}
