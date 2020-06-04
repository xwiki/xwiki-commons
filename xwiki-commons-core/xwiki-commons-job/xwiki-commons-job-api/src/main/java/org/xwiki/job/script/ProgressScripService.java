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
package org.xwiki.job.script;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.job.event.status.JobProgressManager;
import org.xwiki.logging.Message;
import org.xwiki.script.service.ScriptService;

/**
 * Some script oriented helpers to impact current progress.
 * 
 * @version $Id$
 * @since 7.1M2
 */
@Component
@Singleton
@Named("progress")
public class ProgressScripService implements ScriptService
{
    @Inject
    private JobProgressManager progress;

    /**
     * Push new progression level with unknown number of steps.
     */
    public void pushLevel()
    {
        this.progress.pushLevelProgress(this);
    }

    /**
     * Push new progression level.
     *
     * @param steps number of steps in this new level
     */
    public void pushLevel(int steps)
    {
        this.progress.pushLevelProgress(steps, this);
    }

    /**
     * Pop current progression level.
     */
    public void popLevel()
    {
        this.progress.popLevelProgress(this);
    }

    /**
     * Close current step if any and move to next one.
     */
    public void startStep()
    {
        this.progress.startStep(this);
    }

    /**
     * Close current step if any and move to next one.
     *
     * @param name the name of the step
     */
    public void startStep(String name)
    {
        this.progress.startStep(this, name);
    }

    /**
     * Close current step if any and move to next one.
     *
     * @param translationKey the key used to find the translation of the step message
     * @param message the default message associated to the step
     * @param arguments the arguments to insert in the step message
     */
    public void startStep(String translationKey, String message, Object... arguments)
    {
        this.progress.startStep(this, new Message(translationKey, message, arguments));
    }

    /**
     * Close current step.
     */
    public void endStep()
    {
        this.progress.endStep(this);
    }
}
