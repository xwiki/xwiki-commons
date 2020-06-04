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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.job.event.status.EndStepProgressEvent;
import org.xwiki.job.event.status.JobProgress;
import org.xwiki.job.event.status.PopLevelProgressEvent;
import org.xwiki.job.event.status.PushLevelProgressEvent;
import org.xwiki.job.event.status.StartStepProgressEvent;
import org.xwiki.job.event.status.StepProgressEvent;
import org.xwiki.logging.Message;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class DefaultJobProgress implements EventListener, JobProgress
{
    /**
     * The object used to log messages.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJobProgress.class);

    /**
     * Listened events.
     */
    private static final List<Event> EVENTS =
        Arrays.<Event>asList(new PushLevelProgressEvent(), PopLevelProgressEvent.INSTANCE, StepProgressEvent.INSTANCE,
            StartStepProgressEvent.INSTANCE, EndStepProgressEvent.INSTANCE);

    private final String listenerName;

    private final DefaultJobProgressStep rootStep;

    private transient DefaultJobProgressStep currentStep;

    /**
     * Default constructor.
     */
    public DefaultJobProgress()
    {
        this(null);
    }

    /**
     * @param name the name associated to the job progress
     */
    public DefaultJobProgress(String name)
    {
        this.listenerName = name != null ? name : getClass().getName() + '_' + System.identityHashCode(this);

        this.rootStep =
            new DefaultJobProgressStep(new Message("job.progress", "Progress with name [{}]", name), null, null);
        this.currentStep = this.rootStep;
    }

    // EventListener

    @Override
    public String getName()
    {
        return this.listenerName;
    }

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    @Override
    public void onEvent(Event event, Object source, Object message)
    {
        if (event instanceof PushLevelProgressEvent) {
            onPushLevelProgress(((PushLevelProgressEvent) event).getSteps(), source, false);
        } else if (event instanceof PopLevelProgressEvent) {
            onPopLevelProgress(source);
        } else if (event instanceof StartStepProgressEvent) {
            onStartStepProgress((Message) message, source);
        } else if (event instanceof EndStepProgressEvent) {
            onEndStepProgress(source);
        } else if (event instanceof StepProgressEvent) {
            onStepProgress(source);
        }
    }

    /**
     * Adds a new level to the progress stack.
     */
    private void onPushLevelProgress(int steps, Object source, boolean singlesteplevel)
    {
        if (this.currentStep.isLevelFinished()) {
            // If current step is done move to next one
            this.currentStep = this.currentStep.getParent().nextStep(null, source);
        }

        // Add level
        this.currentStep = this.currentStep.addLevel(steps, source, singlesteplevel);
    }

    /**
     * Close current step.
     */
    private void onEndStepProgress(Object source)
    {
        // Try to find the right step based on the source
        DefaultJobProgressStep step = findStep(this.currentStep, source);

        if (step == null) {
            LOGGER.warn("Could not find any matching step for source [{}]. Ignoring EndStepProgress.",
                source.toString());

            return;
        }

        this.currentStep = step;
        this.currentStep.finish();
    }

    private void onStartStepProgress(Message message, Object source)
    {
        if (this.currentStep.getParent() == null) {
            // If we are still on root node, create a level
            this.currentStep = this.currentStep.addLevel(source);
        } else if (!this.currentStep.isFinished() && this.currentStep.source != source) {
            // If current step is from a different source add a level
            onPushLevelProgress(0, source, true);
        } else if (this.currentStep.getParent().levelStep && this.currentStep.getParent().source == source) {
            // If current step is not part of an explicit level and parent level is asked, go back to it
            this.currentStep = this.currentStep.getParent();
            this.currentStep.finishLevel();
        }

        // Start a new step
        this.currentStep = this.currentStep.getParent().nextStep(message, source);
    }

    /**
     * Move progress to next step.
     * 
     * @deprecated since 7.1M2, use {@link #onStartStepProgress(Message)} instead
     */
    @Deprecated
    private void onStepProgress(Object source)
    {
        onStartStepProgress(null, source);

        // if there is only one step close it and move to the next one
        if (this.currentStep.getParent().getChildren().size() == 1) {
            this.currentStep = this.currentStep.getParent().nextStep(null, source);
        }
    }

    /**
     * Called when a {@link PopLevelProgressEvent} is fired.
     */
    private void onPopLevelProgress(Object source)
    {
        DefaultJobProgressStep parent = this.currentStep.getParent();

        if (parent == null) {
            LOGGER.warn("PopLevelProgressEvent was fired too many times. Don't forget "
                + "to match each PopLevelProgressEvent with a PushLevelProgressEvent.");

            return;
        }

        // Try to find the right level based on the source
        DefaultJobProgressStep level = findLevel(this.currentStep.getParent(), source);

        if (level == null) {
            LOGGER.warn("Could not find any matching step level for source [{}]. Ignoring PopLevelProgressEvent.",
                source.toString());

            return;
        }

        // Move to parent step
        this.currentStep = level;

        // Close the level
        this.currentStep.finishLevel();
    }

    private DefaultJobProgressStep findStep(DefaultJobProgressStep step, Object source)
    {
        DefaultJobProgressStep matchingStep = step;

        do {
            if (matchingStep.source == source) {
                return matchingStep;
            }

            matchingStep = matchingStep.getParent();
        } while (matchingStep != null);

        return null;
    }

    private DefaultJobProgressStep findLevel(DefaultJobProgressStep step, Object levelSource)
    {
        DefaultJobProgressStep matchingStep = step;

        do {
            if (levelSource == null || matchingStep.levelSource == levelSource) {
                return matchingStep;
            }

            matchingStep = matchingStep.getParent();
        } while (matchingStep != null);

        return null;
    }

    // JobProgress

    @Override
    public double getOffset()
    {
        return this.rootStep.getOffset();
    }

    @Override
    public double getCurrentLevelOffset()
    {
        return getCurrentStep().getParent() != null ? getCurrentStep().getParent().getOffset() : getOffset();
    }

    @Override
    public DefaultJobProgressStep getRootStep()
    {
        return this.rootStep;
    }

    @Override
    public DefaultJobProgressStep getCurrentStep()
    {
        // currentStep could be null for unserialized job progress
        return this.currentStep != null ? this.currentStep : this.rootStep;
    }
}
