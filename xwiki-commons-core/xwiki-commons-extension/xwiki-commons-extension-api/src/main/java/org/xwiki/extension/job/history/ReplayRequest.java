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

import org.xwiki.job.AbstractRequest;

/**
 * The request used for replaying records from the extension job history.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
public class ReplayRequest extends AbstractRequest
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    private static final String PROPERTY_RECORDS = "records";

    /**
     * @return the list of history records that are being replayed
     */
    public List<ExtensionJobHistoryRecord> getRecords()
    {
        return getProperty(PROPERTY_RECORDS);
    }

    /**
     * Sets the list of history records to replay.
     * 
     * @param records the history records to replay
     */
    public void setRecords(List<ExtensionJobHistoryRecord> records)
    {
        setProperty(PROPERTY_RECORDS, records);
    }
}
