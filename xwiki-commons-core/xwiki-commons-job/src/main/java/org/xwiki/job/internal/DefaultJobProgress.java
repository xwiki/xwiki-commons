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
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.job.event.status.JobProgress;
import org.xwiki.job.event.status.PopLevelProgressEvent;
import org.xwiki.job.event.status.PushLevelProgressEvent;
import org.xwiki.job.event.status.StepProgressEvent;
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
    private static final List<Event> EVENTS = Arrays.asList(new PushLevelProgressEvent(), new PopLevelProgressEvent(),
        new StepProgressEvent());

    /**
     * The unique name of the current job progress.
     */
    private String name;

    /**
     * The progress stack.
     */
    private Stack<Level> progress = new Stack<Level>();

    /**
     * Flag indicating that the next {@link StepProgressEvent} should be ignored (probably because its progress was
     * already taken into account by a {@link PopLevelProgressEvent}).
     */
    private boolean ignoreNextStepProgressEvent;

    /**
     * A step.
     * 
     * @version $Id$
     */
    static class Level
    {
        /**
         * Global progress between 0 and 1.
         */
        public double globalOffset;

        /**
         * Current level progress between 0 and 1.
         */
        public double levelOffset;

        /**
         * Size of the step between 0 and 1.
         */
        public double globalStepSize;

        /**
         * Size of the step between 0 and 1.
         */
        public double localStepSize;

        /**
         * The current step.
         */
        public int currentStep;

        /**
         * The number of steps.
         */
        public int steps;

        /**
         * @param steps number of steps
         * @param offset the current offset
         * @param parentSize the size of the parent step
         */
        public Level(int steps, double offset, double parentSize)
        {
            this.steps = steps;

            this.globalOffset = offset;
            this.globalStepSize = parentSize / steps;

            this.localStepSize = 1.0D / steps;
        }
    }

    /**
     * Default constructor.
     */
    public DefaultJobProgress()
    {
        this.name = getClass().getName() + '_' + hashCode();

        // Push the root level to be able to distinguish between the case when the progress hasn't started yet and the
        // case when the progress is over. Otherwise we would have an empty progress stack for both cases.
        this.progress.push(new Level(1, 0, 1));
    }

    // EventListener

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    @Override
    public void onEvent(Event event, Object arg1, Object arg2)
    {
        boolean ignoreNextStep = this.ignoreNextStepProgressEvent;
        this.ignoreNextStepProgressEvent = false;
        if (event instanceof PushLevelProgressEvent) {
            onPushLevelProgress((PushLevelProgressEvent) event);
        } else if (event instanceof PopLevelProgressEvent) {
            onPopLevelProgress();
        } else if (event instanceof StepProgressEvent && !ignoreNextStep) {
            onStepProgress();
        }
    }

    /**
     * Adds a new level to the progress stack.
     * 
     * @param event the event that was fired
     */
    private void onPushLevelProgress(PushLevelProgressEvent event)
    {
        this.progress.push(new Level(event.getSteps(), getOffset(), this.progress.peek().globalStepSize));
    }

    /**
     * Move progress to next step.
     */
    private void onStepProgress()
    {
        Level level = this.progress.peek();
        if (level.currentStep++ < level.steps) {
            level.globalOffset += level.globalStepSize;
            level.levelOffset += level.localStepSize;
        } else {
            LOGGER.warn("StepProgressEvent was fired too many times: [{}] instead of [{}]. The number of times"
                + " StepProgressEvent is fired must match the number of steps passed to PushLevelProgressEvent.",
                level.steps, level.currentStep);
        }
    }

    /**
     * Called when a {@link PopLevelProgressEvent} is fired.
     */
    private void onPopLevelProgress()
    {
        // The progress stack must have at least one element: the root level.
        if (this.progress.size() > 1) {
            this.progress.pop();
            onStepProgress();
            // Ignore the next StepProgressEvent because we already updated the progress.
            this.ignoreNextStepProgressEvent = true;
        } else {
            LOGGER.warn("PopLevelProgressEvent was fired too many times. Don't forget "
                + "to match each PopLevelProgressEvent with a PushLevelProgressEvent.");
        }
    }

    // JobProgress

    @Override
    public double getOffset()
    {
        return this.progress.peek().globalOffset;
    }

    @Override
    public double getCurrentLevelOffset()
    {
        return this.progress.peek().levelOffset;
    }
}
