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
package org.xwiki.job.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.job.event.status.JobProgressManager;
import org.xwiki.job.event.status.PopLevelProgressEvent;
import org.xwiki.job.event.status.PushLevelProgressEvent;
import org.xwiki.job.event.status.StepProgressEvent;
import org.xwiki.observation.ObservationManager;

/**
 * Default implementation of {@link JobProgressManager}.
 * 
 * @version $Id$
 * @since 6.1M1
 */
@Component
@Singleton
public class DefaultJobProgressManager implements JobProgressManager
{
    @Inject
    private ObservationManager observationManager;

    @Override
    public void pushLevelProgress(int steps, Object source)
    {
        this.observationManager.notify(new PushLevelProgressEvent(steps), source);
    }

    @Override
    public void stepPropress(Object source)
    {
        this.observationManager.notify(new StepProgressEvent(), source);
    }

    @Override
    public void popLevelProgress(Object source)
    {
        this.observationManager.notify(new PopLevelProgressEvent(), source);
    }

}
