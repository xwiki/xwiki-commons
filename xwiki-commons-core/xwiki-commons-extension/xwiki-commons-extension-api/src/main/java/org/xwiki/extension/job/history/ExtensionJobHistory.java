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
package org.xwiki.extension.job.history;

import java.util.List;

import org.apache.commons.collections4.Predicate;
import org.xwiki.component.annotation.Role;

/**
 * The history of extension jobs.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@Role
public interface ExtensionJobHistory
{
    /**
     * Adds a new record to the history.
     * 
     * @param record the record to add
     */
    void addRecord(ExtensionJobHistoryRecord record);

    /**
     * Returns the history records that match the given filter after the specified offset record.
     * 
     * @param filter the predicate used to filter the history records
     * @param offsetRecordId specifies the offset record (where to start from); pass {@code null} to start from the most
     *            recent record in the history
     * @param limit the maximum number of records to return from the specified offset
     * @return a list of history records that match the given filter and are older than the specified offset record
     */
    List<ExtensionJobHistoryRecord> getRecords(Predicate<ExtensionJobHistoryRecord> filter, String offsetRecordId,
        int limit);
}
