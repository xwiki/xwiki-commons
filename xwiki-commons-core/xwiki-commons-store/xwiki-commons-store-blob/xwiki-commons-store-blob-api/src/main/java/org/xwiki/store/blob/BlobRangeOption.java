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

import java.util.Objects;
import java.util.OptionalLong;

import org.xwiki.stability.Unstable;

/**
 * An option that requests reading only a specific byte range from a blob.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public final class BlobRangeOption implements BlobOption
{
    private final long startOffset;

    private final Long endOffset;

    private BlobRangeOption(long startOffset, Long endOffset)
    {
        if (startOffset < 0) {
            throw new IllegalArgumentException("The start offset must be greater than or equal to zero.");
        }
        if (endOffset != null && endOffset < startOffset) {
            throw new IllegalArgumentException("The end offset must be greater than or equal to the start offset.");
        }

        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /**
     * Create a range that starts at the given offset and continues until the end of the blob.
     *
     * @param startOffset the first byte to read (inclusive)
     * @return the created option
     */
    public static BlobRangeOption from(long startOffset)
    {
        return new BlobRangeOption(startOffset, null);
    }

    /**
     * Create a range that starts at the given offset and spans for {@code length} bytes.
     *
     * @param startOffset the first byte to read (inclusive)
     * @param length the number of bytes to read
     * @return the created option
     */
    public static BlobRangeOption withLength(long startOffset, long length)
    {
        if (length <= 0) {
            throw new IllegalArgumentException("The length must be greater than zero.");
        }
        try {
            long endOffset = Math.addExact(startOffset, length - 1);
            return new BlobRangeOption(startOffset, endOffset);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("The requested range exceeds the maximum supported offset.", e);
        }
    }

    /**
     * Create a range that starts and ends at the specified offsets (both inclusive).
     *
     * @param startOffset the first byte to read (inclusive)
     * @param endOffset the last byte to read (inclusive)
     * @return the created option
     */
    public static BlobRangeOption between(long startOffset, long endOffset)
    {
        return new BlobRangeOption(startOffset, endOffset);
    }

    /**
     * @return the inclusive start offset
     */
    public long getStartOffset()
    {
        return this.startOffset;
    }

    /**
     * @return the inclusive end offset
     */
    public OptionalLong getEndOffset()
    {
        return this.endOffset == null ? OptionalLong.empty() : OptionalLong.of(this.endOffset);
    }

    /**
     * @return the number of bytes to read, if defined
     */
    public OptionalLong getLength()
    {
        if (this.endOffset == null) {
            return OptionalLong.empty();
        }

        try {
            long difference = Math.subtractExact(this.endOffset, this.startOffset);
            return OptionalLong.of(Math.addExact(difference, 1));
        } catch (ArithmeticException e) {
            return OptionalLong.empty();
        }
    }

    @Override
    public String getDescription()
    {
        if (this.endOffset == null) {
            return "Read from byte %d to the end".formatted(this.startOffset);
        }
        return "Read bytes %d to %d".formatted(this.startOffset, this.endOffset);
    }

    @Override
    public String toString()
    {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlobRangeOption other)) {
            return false;
        }
        return this.startOffset == other.startOffset && Objects.equals(this.endOffset, other.endOffset);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.startOffset, this.endOffset);
    }
}
