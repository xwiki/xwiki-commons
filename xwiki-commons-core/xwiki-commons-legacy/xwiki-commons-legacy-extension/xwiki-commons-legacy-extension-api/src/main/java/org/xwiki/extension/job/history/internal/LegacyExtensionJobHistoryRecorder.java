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

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.job.history.ReplayJobStatus;
import org.xwiki.job.Job;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.job.internal.AbstractJobStatus;
import org.xwiki.observation.event.Event;

/**
 * Legacy implementation of {@link ExtensionJobHistoryRecorder}.
 *
 * @version $Id$
 * @since 15.7RC1
 */
@Component
@Named(ExtensionJobHistoryRecorder.NAME)
@Singleton
@Deprecated
public class LegacyExtensionJobHistoryRecorder extends ExtensionJobHistoryRecorder
{
    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        boolean ignore = false;
        if (event instanceof JobStartedEvent) {
            Job job = (Job) source;
            if (job.getStatus() instanceof org.xwiki.job.AbstractJobStatus
                && isSubJob((AbstractJobStatus<?>) job.getStatus())) {
                // We record only the jobs that have been triggered explicitly or that are part of a replay.
                ignore = true;
            }
        }
        if (!ignore) {
            super.onEvent(event, source, data);
        }
    }

    private <T extends AbstractJobStatus<?>> boolean isSubJob(T jobStatus)
    {
        return jobStatus.isSubJob() && !(jobStatus.getParentJobStatus() instanceof ReplayJobStatus);
    }
}
