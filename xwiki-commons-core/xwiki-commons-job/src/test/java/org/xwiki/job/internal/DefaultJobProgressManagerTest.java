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

import org.junit.jupiter.api.Test;
import org.xwiki.job.event.status.EndStepProgressEvent;
import org.xwiki.job.event.status.PopLevelProgressEvent;
import org.xwiki.job.event.status.PushLevelProgressEvent;
import org.xwiki.job.event.status.StartStepProgressEvent;
import org.xwiki.logging.Message;
import org.xwiki.observation.ObservationManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Validate {@link DefaultJobProgressManager}.
 * 
 * @version $Id$
 */
@ComponentTest
public class DefaultJobProgressManagerTest
{
    @InjectMockComponents
    private DefaultJobProgressManager progress;

    @MockComponent
    private ObservationManager observation;

    @Test
    public void pushLevelProgress()
    {
        this.progress.pushLevelProgress(this);

        verify(this.observation).notify(new PushLevelProgressEvent(), this);

        this.progress.pushLevelProgress(42, this);

        verify(this.observation).notify(new PushLevelProgressEvent(42), this);
    }

    @Test
    public void startStep()
    {
        this.progress.startStep(this);

        verify(this.observation).notify(StartStepProgressEvent.INSTANCE, this, null);

        this.progress.startStep(this, "message");

        verify(this.observation).notify(StartStepProgressEvent.INSTANCE, this, new Message("message"));

        this.progress.startStep(this, new Message("message"));

        verify(this.observation, times(2)).notify(StartStepProgressEvent.INSTANCE, this, new Message("message"));

        this.progress.startStep(this, "translationKey", "message", "arguments");

        verify(this.observation).notify(StartStepProgressEvent.INSTANCE, this,
            new Message("translationKey", "message", "arguments"));
    }

    @Test
    public void endStep()
    {
        this.progress.endStep(this);

        verify(this.observation).notify(EndStepProgressEvent.INSTANCE, this);
    }

    @Test
    public void popLevelProgress()
    {
        this.progress.popLevelProgress(this);

        verify(this.observation).notify(PopLevelProgressEvent.INSTANCE, this);
    }
}
