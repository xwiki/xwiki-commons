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

import java.util.List;
import java.util.stream.Collectors;

import org.xwiki.stability.Unstable;

/**
 * Exception thrown when a write condition is not satisfied.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public class WriteConditionFailedException extends BlobStoreException
{
    private final List<WriteCondition> conditions;

    private final BlobPath blobPath;

    /**
     * Constructor.
     *
     * @param blobPath the path of the blob for which the conditions failed
     * @param conditions the conditions that were not satisfied
     */
    public WriteConditionFailedException(BlobPath blobPath, List<WriteCondition> conditions)
    {
        super(formatWriteConditionError(blobPath, conditions));
        this.blobPath = blobPath;
        this.conditions = List.copyOf(conditions);
    }

    /**
     * Constructor.
     *
     * @param blobPath the path of the blob for which the conditions failed
     * @param conditions the conditions that were not satisfied
     * @param cause the underlying cause
     */
    public WriteConditionFailedException(BlobPath blobPath, List<WriteCondition> conditions, Throwable cause)
    {
        super(formatWriteConditionError(blobPath, conditions), cause);
        this.blobPath = blobPath;
        this.conditions = List.copyOf(conditions);
    }

    /**
     * Get the blob path for which the conditions failed.
     *
     * @return the blob path
     */
    public BlobPath getBlobPath()
    {
        return this.blobPath;
    }

    /**
     * Get the conditions that were not satisfied.
     *
     * @return the write conditions
     */
    public List<WriteCondition> getConditions()
    {
        return this.conditions;
    }

    private static String formatWriteConditionError(BlobPath blobPath, List<WriteCondition> conditions)
    {
        return "Write conditions failed for blob " + blobPath + ": "
            + conditions.stream().map(WriteCondition::getDescription).collect(Collectors.joining(", "));
    }
}
