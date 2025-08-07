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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.stability.Unstable;

/**
 * An opaque identifier for blobs within a BlobStore. Segments are joined with '/' but implementations are free to
 * interpret them as needed (e.g. S3 keys).
 *
 * @version $Id$
 * @since 17.7.0RC1
 */
@Unstable
public final class BlobPath
{
    private final List<String> segments;

    private final String raw;

    private BlobPath(List<String> segments)
    {
        this.segments = List.copyOf(segments);
        this.raw = String.join("/", segments);
    }

    /**
     * Create a BlobPath from individual segments.
     *
     * @param segments the segments of the path
     * @return a BlobPath constructed from the segments
     */
    public static BlobPath of(List<String> segments)
    {
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("At least one segment required");
        }
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
        String[] parts = StringUtils.split(path, '/');
        List<String> nonEmpty = Arrays.stream(parts)
            .filter(s -> !s.isEmpty())
            .toList();
        if (nonEmpty.isEmpty()) {
            throw new IllegalArgumentException("Path must contain at least one non-empty segment");
        }
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
     * @return the parent BlobPath, or empty if this is a single-segment path.
     */
    public Optional<BlobPath> getParent()
    {
        if (this.segments.size() <= 1) {
            return Optional.empty();
        }
        List<String> parentSegments = this.segments.subList(0, this.segments.size() - 1);
        return Optional.of(new BlobPath(parentSegments));
    }

    /**
     * @return the segments of this BlobPath as a list
     */
    public List<String> getSegments()
    {
        return this.segments;
    }

    @Override
    public String toString()
    {
        return this.raw;
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
        return this.raw.equals(other.raw);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.raw);
    }
}
