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
package org.xwiki.extension.job.history.internal;

import java.util.List;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.extension.job.history.ReplayJobStatus;
import org.xwiki.extension.job.history.ReplayRequest;
import org.xwiki.job.Job;
import org.xwiki.job.internal.AbstractJob;

/**
 * A job that can replay records from the extension job history.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@Component
@Named(ReplayJob.JOB_TYPE)
public class ReplayJob extends AbstractJob<ReplayRequest, ReplayJobStatus>
{
    /**
     * The id of the job.
     */
    public static final String JOB_TYPE = "replay";

    @Override
    public String getType()
    {
        return JOB_TYPE;
    }

    @Override
    protected void runInternal() throws Exception
    {
        List<ExtensionJobHistoryRecord> records = this.request.getRecords();
        if (records == null) {
            return;
        }

        this.progressManager.pushLevelProgress(records.size(), this);

        try {
            int currentRecordNumber = 0;
            for (ExtensionJobHistoryRecord record : records) {
                this.progressManager.startStep(this);
                this.status.setCurrentRecordNumber(currentRecordNumber++);
                replay(record);
                this.progressManager.endStep(this);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    private void replay(ExtensionJobHistoryRecord record) throws ComponentLookupException
    {
        Job job = this.componentManager.getInstance(Job.class, record.getJobType());
        job.initialize(record.getRequest());
        job.run();
    }
}
