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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.job.ExtensionRequest;
import org.xwiki.extension.job.history.ExtensionJobHistory;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.extension.job.history.ReplayJobStatus;
import org.xwiki.job.Job;
import org.xwiki.job.event.JobFinishedEvent;
import org.xwiki.job.internal.AbstractJobStatus;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * Records the extension jobs that have been executed.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@Component
@Named(ExtensionJobHistoryRecorder.NAME)
@Singleton
public class ExtensionJobHistoryRecorder implements EventListener
{
    /**
     * The name of this event listener (and its component hint at the same time).
     */
    public static final String NAME = "ExtensionJobHistoryRecorder";

    private static final List<Event> EVENTS = Collections.<Event>singletonList(new JobFinishedEvent());

    @Inject
    private ExtensionJobHistory history;

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (data instanceof Throwable) {
            // The job execution has failed.
            return;
        }

        Job job = (Job) source;
        if (!(job.getRequest() instanceof ExtensionRequest)) {
            // This is not an extension job.
            return;
        }

        if (job.getStatus() instanceof AbstractJobStatus && isSubJob((AbstractJobStatus<?>) job.getStatus())) {
            // We record only the jobs that have been triggered explicitly or that are part of a replay.
            return;
        }

        // We assume the job ended at this moment because the actual end date is set on the job status after all the
        // event listeners are called so it's not available right now.
        this.history.addRecord(new ExtensionJobHistoryRecord(job.getType(), (ExtensionRequest) job.getRequest(), null,
            job.getStatus().getStartDate(), new Date()));
    }

    private <T extends AbstractJobStatus<?>> boolean isSubJob(T jobStatus)
    {
        return jobStatus.isSubJob() && !(jobStatus.getParentJobStatus() instanceof ReplayJobStatus);
    }
}
