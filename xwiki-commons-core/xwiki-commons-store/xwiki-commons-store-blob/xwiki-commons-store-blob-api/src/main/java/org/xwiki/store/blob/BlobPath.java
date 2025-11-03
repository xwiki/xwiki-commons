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

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.stability.Unstable;

/**
 * An opaque identifier for blobs within a BlobStore. Segments are joined with '/' but implementations are free to
 * interpret them as needed (e.g. S3 keys). BlobPaths are immutable.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public final class BlobPath implements Serializable
{
    /**
     * The root BlobPath with no segments.
     */
    public static final BlobPath ROOT = new BlobPath(List.of());

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<String> segments;

    private final String canonical;

    private BlobPath(List<String> segments)
    {
        // Validate segments to ensure each is a single file system component and disallow directory traversal.
        if (segments == null) {
            throw new IllegalArgumentException("segments must not be null");
        }
        for (int i = 0; i < segments.size(); i++) {
            String s = segments.get(i);
            validateSegment(s, i);
        }
        this.segments = List.copyOf(segments);
        this.canonical = String.join("/", segments);
    }

    private static void validateSegment(String s, int i)
    {
        if (s == null) {
            throw new IllegalArgumentException("Segment at index %d is null".formatted(i));
        }
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Segment at index %d is empty".formatted(i));
        }
        if (s.equals(".") || s.equals("..")) {
            throw new IllegalArgumentException(
                "Segment at index %d is a directory traversal component: %s".formatted(i, s));
        }
        if (s.indexOf('/') >= 0 || s.indexOf('\\') >= 0) {
            throw new IllegalArgumentException(
                "Segment at index %d contains an illegal path separator: %s".formatted(i, s));
        }
    }

    /**
     * Create a BlobPath from individual segments.
     *
     * @param segments the segments of the path
     * @return a BlobPath constructed from the segments
     */
    public static BlobPath of(List<String> segments)
    {
        return new BlobPath(segments);
    }

    /**
     * Create a BlobPath by splitting a slash-delimited string.
     *
     * @param path the slash-delimited path string
     * @return a BlobPath constructed from the segments in the string
     */
    public static BlobPath from(String path)
    {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        if (path.isEmpty()) {
            return BlobPath.ROOT;
        }
        String[] parts = StringUtils.split(path, '/');
        List<String> nonEmpty = Arrays.stream(parts)
            .filter(s -> !s.isEmpty())
            .toList();

        return new BlobPath(nonEmpty);
    }

    /**
     * Return a new BlobPath by appending additional segments.
     *
     * @param moreSegments the segments to append to this path
     * @return a new BlobPath with the additional segments appended
     */
    public BlobPath resolve(String... moreSegments)
    {
        if (moreSegments.length == 0) {
            return this;
        }
        List<String> combined =
            Stream.concat(this.segments.stream(), Arrays.stream(moreSegments))
                .toList();
        return new BlobPath(combined);
    }

    /**
     * @return the parent BlobPath.
     */
    public BlobPath getParent()
    {
        if (this.segments.size() <= 1) {
            return BlobPath.of(List.of());
        }
        List<String> parentSegments = this.segments.subList(0, this.segments.size() - 1);
        return new BlobPath(parentSegments);
    }

    /**
     * @param suffix the suffix to append to the filename
     * @return a new BlobPath with the suffix appended to the filename (last segment)
     */
    public BlobPath appendSuffix(String suffix)
    {
        if (StringUtils.isBlank(suffix)) {
            throw new IllegalArgumentException("Suffix must not be empty");
        }

        String lastSegment = getName() + suffix;
        List<String> newSegments = Stream.concat(
                this.segments.stream().limit(this.segments.isEmpty() ? 0 : this.segments.size() - 1L),
                Stream.of(lastSegment))
            .toList();
        return new BlobPath(newSegments);
    }

    /**
     * Check if this path is an ancestor of another path or equals it.
     *
     * @param other the other path to compare against
     * @return true if this path is an ancestor of the other path or equals it, false otherwise
     */
    public boolean isAncestorOfOrEquals(BlobPath other)
    {
        if (this.segments.size() > other.segments.size()) {
            return false;
        }

        for (int i = 0; i < this.segments.size(); i++) {
            if (!this.segments.get(i).equals(other.segments.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return the segments of this BlobPath as a list
     */
    public List<String> getSegments()
    {
        return this.segments;
    }

    /**
     * @return the name of the final segment of this BlobPath
     */
    public String getName()
    {
        if (this.segments.isEmpty()) {
            return "";
        }
        return this.segments.get(this.segments.size() - 1);
    }

    @Override
    public String toString()
    {
        return this.canonical;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlobPath other)) {
            return false;
        }
        return this.canonical.equals(other.canonical);
    }

    @Override
    public int hashCode()
    {
        return this.canonical.hashCode();
    }
}
