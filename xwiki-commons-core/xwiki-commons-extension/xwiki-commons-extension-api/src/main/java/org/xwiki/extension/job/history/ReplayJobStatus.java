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

import org.xwiki.extension.job.history.internal.ReplayJob;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.logging.LoggerManager;
import org.xwiki.observation.ObservationManager;

/**
 * The status of the job that replays records from the extension job history.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
public class ReplayJobStatus extends DefaultJobStatus<ReplayRequest>
{
    private int currentRecordNumber;

    /**
     * Creates a new job status.
     * 
     * @param request the request provided when the job was started
     * @param observationManager the observation manager
     * @param loggerManager the logger manager
     */
    public ReplayJobStatus(ReplayRequest request, ObservationManager observationManager, LoggerManager loggerManager)
    {
        super(ReplayJob.JOB_TYPE, request, null, observationManager, loggerManager);
    }

    /**
     * @return the number of the history record that is currently being replayed
     */
    public int getCurrentRecordNumber()
    {
        return currentRecordNumber;
    }

    /**
     * Sets the number of the history record that is currently being replayed.
     * 
     * @param currentRecordNumber the current record number
     */
    public void setCurrentRecordNumber(int currentRecordNumber)
    {
        this.currentRecordNumber = currentRecordNumber;
    }

    /**
     * @return the record that is currently being replayed
     */
    public ExtensionJobHistoryRecord getCurrentRecord()
    {
        int index = this.currentRecordNumber;
        List<ExtensionJobHistoryRecord> records = getRequest().getRecords();
        if (records != null && index >= 0 && index < records.size()) {
            return records.get(index);
        }
        return null;
    }
}
