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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.job.event.status.*;
import org.xwiki.logging.Message;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.internal.DefaultObservationManager;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ComponentTest
@ComponentList(DefaultObservationManager.class)
public class DefaultJobProgressTest
{
    @InjectComponentManager
    private ComponentManager componentManager;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    private ObservationManager observation;

    private DefaultJobProgress progress;

    @BeforeEach
    public void before() throws Exception
    {
        this.observation = this.componentManager.getInstance(ObservationManager.class);
        this.progress = new DefaultJobProgress();
        this.observation.addListener(this.progress);
    }

    @Test
    public void progressSteps()
    {
        assertEquals(0, Double.compare(0D, this.progress.getOffset()));
        assertEquals(0, Double.compare(0D, this.progress.getCurrentLevelOffset()));

        this.observation.notify(new PushLevelProgressEvent(4), null, null);

        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0.25D, this.progress.getOffset(), 0D);
        assertEquals(0.25D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new PushLevelProgressEvent(2), null, null);

        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0.25D, this.progress.getOffset(), 0D);
        assertEquals(0.0D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0.375D, this.progress.getOffset(), 0D);
        assertEquals(0.5D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new PopLevelProgressEvent(), null, null);

        assertEquals(0.5D, this.progress.getOffset(), 0D);
        assertEquals(0.5D, this.progress.getCurrentLevelOffset(), 0D);
    }

    @Test
    public void stepProgressEvent()
    {
        assertEquals(0, Double.compare(0D, this.progress.getOffset()));
        assertEquals(0, Double.compare(0D, this.progress.getCurrentLevelOffset()));

        this.observation.notify(new PushLevelProgressEvent(4), null, null);

        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new StepProgressEvent(), null, null);

        assertEquals(0.25D, this.progress.getOffset(), 0D);
        assertEquals(0.25D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new PushLevelProgressEvent(2), null, null);

        assertEquals(0.25D, this.progress.getOffset(), 0D);
        assertEquals(0.0D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new StepProgressEvent(), null, null);

        assertEquals(0.375D, this.progress.getOffset(), 0D);
        assertEquals(0.5D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new PopLevelProgressEvent(), null, null);

        assertEquals(0.5D, this.progress.getOffset(), 0D);
        assertEquals(0.5D, this.progress.getCurrentLevelOffset(), 0D);
    }

    /**
     * Tests that the offset is 1 when the progress is done.
     */
    @Test
    public void progressDone()
    {
        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new PushLevelProgressEvent(1), null, null);
        this.observation.notify(new PopLevelProgressEvent(), null, null);

        assertEquals(1D, this.progress.getOffset(), 0D);
        assertEquals(1D, this.progress.getCurrentLevelOffset(), 0D);
    }

    @Test
    public void popDontMoveToNextStep()
    {
        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new PushLevelProgressEvent(2), null, null);

        this.observation.notify(new PushLevelProgressEvent(1), null, null);
        this.observation.notify(new StepProgressEvent(), null, null);
        this.observation.notify(new PopLevelProgressEvent(), null, null);

        assertEquals(.5D, this.progress.getOffset(), 0D);
        assertEquals(.5D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new StepProgressEvent(), null, null);

        assertEquals(.5D, this.progress.getOffset(), 0D);
        assertEquals(.5D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new StepProgressEvent(), null, null);

        assertEquals(1D, this.progress.getOffset(), 0D);
        assertEquals(1D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new PopLevelProgressEvent(), null, null);

        assertEquals(1D, this.progress.getOffset(), 0D);
        assertEquals(1D, this.progress.getCurrentLevelOffset(), 0D);
    }

    // Bulletproofing

    @Test
    public void moveToNextStepInRoot()
    {
        assertEquals(0, this.progress.getRootStep().getChildren().size());

        // Move to first step
        this.observation.notify(new StartStepProgressEvent(), null, null);

        // Move to second step
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(2, this.progress.getRootStep().getChildren().size());
    }

    @Test
    public void moreStepsThanExpected()
    {
        // Expect 1 step
        this.observation.notify(new PushLevelProgressEvent(1), null, null);

        // First step
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);
        assertEquals(1, this.progress.getRootStep().getChildren().size());

        // Second step
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(1D, this.progress.getOffset(), 0D);
        assertEquals(1D, this.progress.getCurrentLevelOffset(), 0D);
        assertEquals(2, this.progress.getRootStep().getChildren().size());

        // Third step
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(1D, this.progress.getOffset(), 0D);
        assertEquals(3, this.progress.getRootStep().getChildren().size());

        this.observation.notify(new PopLevelProgressEvent(), null, null);

        assertEquals(1D, this.progress.getOffset(), 0D);
        assertEquals(3, this.progress.getRootStep().getChildren().size());
    }

    @Test
    public void unknownNumberOfSteps()
    {
        // Unknown number of steps
        this.observation.notify(new PushLevelProgressEvent(), null, null);

        // First step
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);
        assertEquals(1, this.progress.getRootStep().getChildren().size());

        // Second step
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0.5D, this.progress.getOffset(), 0D);
        assertEquals(0.5D, this.progress.getCurrentLevelOffset(), 0D);
        assertEquals(2, this.progress.getRootStep().getChildren().size());

        // Third step
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(3, this.progress.getRootStep().getChildren().size());

        // Fourth step
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0.75D, this.progress.getOffset(), 0D);
        assertEquals(0.75D, this.progress.getCurrentLevelOffset(), 0D);
        assertEquals(4, this.progress.getRootStep().getChildren().size());

        this.observation.notify(new PopLevelProgressEvent(), null, null);

        assertEquals(1D, this.progress.getOffset(), 0D);
        assertEquals(4, this.progress.getRootStep().getChildren().size());
    }

    @Test
    public void pushLevelOnClosedStep()
    {
        this.observation.notify(new PushLevelProgressEvent(2), null, null);

        // Finish first step
        this.observation.notify(new PushLevelProgressEvent(1), null, null);
        this.observation.notify(new PopLevelProgressEvent(), null, null);

        assertEquals(1, this.progress.getRootStep().getChildren().size());

        // Forget the StepProgressEvent

        // All sub-steps to second step
        this.observation.notify(new PushLevelProgressEvent(1), null, null);
        this.observation.notify(new PopLevelProgressEvent(), null, null);

        assertEquals(2, this.progress.getRootStep().getChildren().size());
    }

    @Test
    public void popLevelOnWrongSource()
    {
        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);

        this.observation.notify(new PushLevelProgressEvent(1), "source1", null);
        this.observation.notify(new PopLevelProgressEvent(), "source2", null);

        assertEquals("Could not find any matching step level for source [source2]. Ignoring PopLevelProgressEvent.",
            logCapture.getMessage(0));

        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);
    }

    @Test
    public void popLevelOnParentLevelSource()
    {
        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);

        Object source1 = "source1";
        Object source11 = "source11";

        this.observation.notify(new PushLevelProgressEvent(2), source1, null);

        this.observation.notify(new PushLevelProgressEvent(2), source11, null);

        // First step in source11 level
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);

        // Second step in source11 level
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0.25D, this.progress.getOffset(), 0D);
        assertEquals(0.5D, this.progress.getCurrentLevelOffset(), 0D);

        // "Forget" to pop source11 level

        // Automatically close all levels until it matches source1
        this.observation.notify(new PopLevelProgressEvent(), source1, null);

        assertEquals(1D, this.progress.getOffset(), 0D);
        assertEquals(1D, this.progress.getCurrentLevelOffset(), 0D);
    }

    @Test
    public void endStepOnParentStepSource()
    {
        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);

        Object source1 = "source1";
        Object source11 = "source11";

        // Root level
        this.observation.notify(new PushLevelProgressEvent(1), null, null);

        // First step in root level
        this.observation.notify(new StartStepProgressEvent(), source1, null);

        // Level 1
        this.observation.notify(new PushLevelProgressEvent(2), source1, null);

        // Level 11
        this.observation.notify(new PushLevelProgressEvent(2), source11, null);

        // First step in source11 level
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0D, this.progress.getOffset(), 0D);
        assertEquals(0D, this.progress.getCurrentLevelOffset(), 0D);

        // Second step in source11 level
        this.observation.notify(new StartStepProgressEvent(), null, null);

        assertEquals(0.25D, this.progress.getOffset(), 0D);
        assertEquals(0.5D, this.progress.getCurrentLevelOffset(), 0D);

        // "Forget" to pop source11 level

        // Automatically close all levels until it matches source1
        this.observation.notify(new EndStepProgressEvent(), source1, null);

        assertEquals(1D, this.progress.getOffset(), 0D);
        assertEquals(1D, this.progress.getCurrentLevelOffset(), 0D);
    }

    @Test
    public void startStepFromDifferentSource()
    {
        Object source1 = "source1";
        Object source11 = "source11";
        Object source12 = "source12";
        Object source1b = "source1b";

        // Root level
        this.observation.notify(new PushLevelProgressEvent(), source1, null);

        // Start first step in source1 level
        this.observation.notify(new StartStepProgressEvent(), source1, null);

        assertEquals(1, this.progress.getRootStep().getChildren().size());

        // Start first step in source11 level
        this.observation.notify(new StartStepProgressEvent(), source11, null);

        assertEquals(1, this.progress.getRootStep().getChildren().size());
        assertEquals(1, this.progress.getRootStep().getChildren().get(0).getChildren().size());

        // Close the step
        this.observation.notify(new EndStepProgressEvent(), source11, null);

        // Start first step in source12 level
        this.observation.notify(new StartStepProgressEvent(), source12, null);

        assertEquals(1, this.progress.getRootStep().getChildren().size());
        assertEquals(2, this.progress.getRootStep().getChildren().get(0).getChildren().size());

        // Close the step
        this.observation.notify(new EndStepProgressEvent(), source12, null);

        // Start second step in source1 level
        this.observation.notify(new StartStepProgressEvent(), source1, null);

        assertEquals(2, this.progress.getRootStep().getChildren().size());
        assertEquals(2, this.progress.getRootStep().getChildren().get(0).getChildren().size());
        assertEquals(0, this.progress.getRootStep().getChildren().get(1).getChildren().size());

        // Close the step
        this.observation.notify(new EndStepProgressEvent(), source1, null);

        // Start third step in source1 level (but with a different source)
        this.observation.notify(new StartStepProgressEvent(), source1b, null);

        assertEquals(3, this.progress.getRootStep().getChildren().size());
        assertEquals(2, this.progress.getRootStep().getChildren().get(0).getChildren().size());
        assertEquals(0, this.progress.getRootStep().getChildren().get(1).getChildren().size());
        assertEquals(0, this.progress.getRootStep().getChildren().get(2).getChildren().size());
    }

    @Test
    public void addStepWhenFinished()
    {
        DefaultJobProgressStep step = this.progress.getCurrentStep();
        step.finish();

        Throwable exception = assertThrows(UnsupportedOperationException.class, () -> {
            step.addStep(mock(Message.class), mock(Object.class));
        });
        assertEquals("Step is closed", exception.getMessage());
    }
}
