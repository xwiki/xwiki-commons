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

import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.job.Request;

/**
 * Base class for {@link org.xwiki.job.Job} implementations.
 *
 * @param <R> the request type associated to the job
 * @param <S> the status type associated to the job
 * @version $Id$
 * @since 5.0M1
 * @deprecated since 7.4M1, use {@link org.xwiki.job.AbstractJob} instead
 */
@Deprecated
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public abstract class AbstractJob<R extends Request, S extends org.xwiki.job.AbstractJobStatus<? super R>>
    extends org.xwiki.job.AbstractJob<R, S>
{
    /**
     * Push new progression level.
     *
     * @param steps number of steps in this new level
     * @deprecated since 7.1M2, use directly {@link #progressManager} instead
     */
    @Deprecated
    protected void notifyPushLevelProgress(int steps)
    {
        this.progressManager.pushLevelProgress(steps, this);
    }

    /**
     * Next step.
     * 
     * @deprecated since 7.1M2, use directly {@link #progressManager} instead
     */
    @Deprecated
    protected void notifyStepPropress()
    {
        this.progressManager.stepPropress(this);
    }

    /**
     * Pop progression level.
     * 
     * @deprecated since 7.1M2, use directly {@link #progressManager} instead
     */
    @Deprecated
    protected void notifyPopLevelProgress()
    {
        this.progressManager.popLevelProgress(this);
    }
}
