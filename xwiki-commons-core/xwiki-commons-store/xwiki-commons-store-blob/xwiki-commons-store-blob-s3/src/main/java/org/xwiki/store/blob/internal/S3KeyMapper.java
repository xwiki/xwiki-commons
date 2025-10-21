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
package org.xwiki.store.blob.internal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.store.blob.BlobPath;

/**
 * Utility class for converting between BlobPath and S3 keys.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
class S3KeyMapper
{
    private static final Logger LOGGER = LoggerFactory.getLogger(S3KeyMapper.class);

    private static final String PATH_SEPARATOR = "/";

    private final String keyPrefix;

    /**
     * Constructor.
     *
     * @param keyPrefix the key prefix for all objects (can be null or empty)
     */
    S3KeyMapper(String keyPrefix)
    {
        this.keyPrefix = normalizePrefix(keyPrefix);
    }

    private static String normalizePrefix(String prefix)
    {
        if (StringUtils.isBlank(prefix)) {
            return "";
        }
        String normalized = prefix;
        normalized = StringUtils.strip(normalized);
        normalized = StringUtils.strip(normalized, PATH_SEPARATOR);

        return normalized;
    }

    /**
     * Build the S3 key from a BlobPath.
     *
     * @param blobPath the blob path
     * @return the S3 key
     */
    String buildS3Key(BlobPath blobPath)
    {
        String pathStr = blobPath.toString();
        if (StringUtils.isNotBlank(this.keyPrefix)) {
            return this.keyPrefix + PATH_SEPARATOR + pathStr;
        }
        return pathStr;
    }

    /**
     * Get the S3 key prefix for a given blob path (with trailing separator).
     *
     * @param path the blob path
     * @return the S3 key prefix
     */
    String getS3KeyPrefix(BlobPath path)
    {
        String prefix = buildS3Key(path);
        if (!prefix.endsWith(PATH_SEPARATOR)) {
            prefix += PATH_SEPARATOR;
        }
        return prefix;
    }

    /**
     * Convert an S3 key back to a BlobPath.
     *
     * @param s3Key the S3 key
     * @return the BlobPath, or null if the key doesn't match our prefix
     */
    BlobPath s3KeyToBlobPath(String s3Key)
    {
        String pathStr = s3Key;

        if (StringUtils.isNotBlank(this.keyPrefix)) {
            String expectedPrefix = this.keyPrefix + PATH_SEPARATOR;
            if (s3Key.startsWith(expectedPrefix)) {
                pathStr = s3Key.substring(expectedPrefix.length());
            } else {
                // Key doesn't match our prefix
                return null;
            }
        }

        try {
            return BlobPath.from(pathStr);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid blob path from S3 key: {}", s3Key, e);
            return null;
        }
    }

    /**
     * @return the key prefix
     */
    public String getKeyPrefix()
    {
        return this.keyPrefix;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (!(o instanceof S3KeyMapper that)) {
            return false;
        }

        return new EqualsBuilder().append(this.keyPrefix, that.keyPrefix).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(this.keyPrefix).toHashCode();
    }
}
