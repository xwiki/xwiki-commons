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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xwiki.job.event.status.JobProgressStep;
import org.xwiki.logging.Message;

/**
 * @version $Id$
 * @since 7.1M2
 */
public class DefaultJobProgressStep implements JobProgressStep
{
    // Not stored data (used only during progress)

    protected final transient DefaultJobProgressStep parent;

    protected transient Object source;

    protected transient Object levelSource;

    // Stored data

    protected final Message message;

    protected final int index;

    protected double offset;

    protected int maximumChildren = -1;

    protected double childSize;

    protected List<DefaultJobProgressStep> children;

    private final long startTime;

    private boolean finished;

    private boolean levelFinished;

    private long elapsedTime;

    /**
     * @param message the message associated to the step
     * @param source who asked to create this new step
     * @param parent the parent step
     */
    public DefaultJobProgressStep(Message message, Object source, DefaultJobProgressStep parent)
    {
        this.message = message;
        this.parent = parent;
        this.source = source;

        if (this.parent != null) {
            this.index = parent.children.size();
            this.startTime = this.index == 0 ? parent.startTime : System.nanoTime();
        } else {
            this.index = 0;
            this.startTime = System.nanoTime();
        }

        this.offset = 0.0D;
    }

    /**
     * @return true if the step is a virtual step (child of an empty level)
     */
    public boolean isVirtual()
    {
        return getParent() != null && getParent().getChildren().isEmpty();
    }

    @Override
    public Message getMessage()
    {
        return this.message;
    }

    @Override
    public DefaultJobProgressStep getParent()
    {
        return this.parent;
    }

    @Override
    public List<DefaultJobProgressStep> getChildren()
    {
        return this.children != null ? this.children : Collections.<DefaultJobProgressStep>emptyList();
    }

    @Override
    public double getOffset()
    {
        return this.offset;
    }

    @Override
    public long getElapsedTime()
    {
        return isFinished() ? this.elapsedTime : System.nanoTime() - this.startTime;
    }

    /**
     * Make sure it's allowed to modify the step.
     */
    private void assertModifiable()
    {
        if (isFinished()) {
            throw new UnsupportedOperationException("Step is closed");
        }
    }

    /**
     * @param stepMessage the message associated to the step
     * @param newStepSource who asked to create this new step
     * @return the new step
     */
    public DefaultJobProgressStep addStep(Message stepMessage, Object newStepSource)
    {
        assertModifiable();

        if (this.children == null) {
            this.children = new ArrayList<DefaultJobProgressStep>();
        }

        DefaultJobProgressStep step = new DefaultJobProgressStep(stepMessage, newStepSource, this);

        this.children.add(step);

        // Update offset if needed
        if (this.maximumChildren <= 0) {
            this.childSize = 1.0D / this.children.size();
            double newOffset = this.childSize * (this.children.size() - 1);
            move(newOffset - this.offset);
        }

        return step;
    }

    /**
     * Add level with unknown number of steps to the step and return a virtual step as child of the level.
     * 
     * @param newLevelSource who asked to create this new level
     * @return the new step
     */
    public DefaultJobProgressStep addLevel(Object newLevelSource)
    {
        return addLevel(0, newLevelSource);
    }

    /**
     * Add children to the step and return the first one.
     * 
     * @param steps the number of step
     * @param newLevelSource who asked to create this new level
     * @return the new step
     */
    public DefaultJobProgressStep addLevel(int steps, Object newLevelSource)
    {
        assertModifiable();

        this.maximumChildren = steps;
        this.levelSource = newLevelSource;

        if (steps > 0) {
            this.childSize = 1.0D / steps;
        }

        if (this.maximumChildren > 0) {
            this.children = new ArrayList<>(this.maximumChildren);
        } else {
            this.children = new ArrayList<>();
        }

        // Create a virtual child
        return new DefaultJobProgressStep(null, newLevelSource, this);
    }

    /**
     * @param size update the offset with the provided size
     */
    public void move(double size)
    {
        assertModifiable();

        if (size != 0D) {
            this.offset += size;

            // Fix size
            double actualSize = size;
            if (this.offset > 1D) {
                actualSize -= this.offset - 1D;
                this.offset = 1D;
            }

            // Update parent offset
            if (this.parent != null && actualSize != 0D) {
                actualSize *= this.parent.childSize;

                this.parent.move(actualSize);
            }
        }
    }

    /**
     * Move to next child step.
     * 
     * @param stepMessage the message associated to the step
     * @param newStepSource who asked to create this new step
     * @return the new step
     */
    public DefaultJobProgressStep nextStep(Message stepMessage, Object newStepSource)
    {
        assertModifiable();

        // Close current step and move to the end
        finishStep();

        // Add new step
        return addStep(stepMessage, newStepSource);
    }

    /**
     * Finish current step.
     */
    public void finishStep()
    {
        // Close step
        if (this.children != null && !this.children.isEmpty()) {
            this.children.get(this.children.size() - 1).finish();
        }
    }

    /**
     * @return true if the step is closed
     */
    public boolean isFinished()
    {
        return this.finished || isVirtual();
    }

    /**
     * @return true if the step level is closed
     */
    public boolean isLevelFinished()
    {
        return this.levelFinished || isVirtual();
    }

    /**
     * Close the step level.
     */
    public void finishLevel()
    {
        if (!isLevelFinished()) {
            // Make sure current children is closed
            finishStep();

            // Move offset to the end of the step (in case some sub-steps were not executed)
            move(1.0D - this.offset);

            this.levelSource = null;

            // Mark it as finished
            this.levelFinished = true;
        }
    }

    /**
     * Close the step.
     */
    public void finish()
    {
        if (!isFinished()) {
            finishLevel();

            // Calculate the elapsed time
            this.elapsedTime = getElapsedTime();

            this.source = null;

            // Mark it as finished
            this.finished = true;
        }
    }
}
