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

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.job.event.status.EndStepProgressEvent;
import org.xwiki.job.event.status.JobProgressManager;
import org.xwiki.job.event.status.PopLevelProgressEvent;
import org.xwiki.job.event.status.PushLevelProgressEvent;
import org.xwiki.job.event.status.StartStepProgressEvent;
import org.xwiki.job.event.status.StepProgressEvent;
import org.xwiki.logging.Message;
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
    public void pushLevelProgress(Object source)
    {
        this.observationManager.notify(new PushLevelProgressEvent(), source);
    }

    @Override
    public void pushLevelProgress(int steps, Object source)
    {
        this.observationManager.notify(new PushLevelProgressEvent(steps), source);
    }

    @Override
    @Deprecated
    public void stepPropress(Object source)
    {
        this.observationManager.notify(StepProgressEvent.INSTANCE, source);
    }

    @Override
    public void startStep(Object source)
    {
        startStep(source, (Message) null);
    }

    @Override
    public void startStep(Object source, String name)
    {
        startStep(source, toMessage(name));
    }

    @Override
    public void startStep(Object source, Message message)
    {
        this.observationManager.notify(StartStepProgressEvent.INSTANCE, source, message);
    }

    @Override
    public void startStep(Object source, String translationKey, String message, Object... arguments)
    {
        startStep(source, toMessage(translationKey, message, arguments));
    }

    @Override
    public void endStep(Object source)
    {
        this.observationManager.notify(EndStepProgressEvent.INSTANCE, source);
    }

    @Override
    public void popLevelProgress(Object source)
    {
        this.observationManager.notify(PopLevelProgressEvent.INSTANCE, source);
    }

    @Override
    public <T> T call(Callable<T> task, Object source) throws Exception
    {
        pushLevelProgress(source);

        try {
            return task.call();
        } finally {
            popLevelProgress(source);
        }
    }

    @Override
    public <T> T call(Callable<T> task, int steps, Object source) throws Exception
    {
        pushLevelProgress(steps, source);

        try {
            return task.call();
        } finally {
            popLevelProgress(source);
        }
    }

    private Message toMessage(String name)
    {
        return name != null ? new Message(name) : null;
    }

    private Message toMessage(String translationKey, String message, Object[] arguments)
    {
        return new Message(translationKey, message, arguments);
    }
}
