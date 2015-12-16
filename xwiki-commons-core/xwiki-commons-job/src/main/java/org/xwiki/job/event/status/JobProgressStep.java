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
package org.xwiki.job.event.status;

import java.util.List;

import org.xwiki.logging.Message;

/**
 * Represents one step in the progress tree.
 * 
 * @version $Id$
 * @since 7.1M2
 */
public interface JobProgressStep
{
    /**
     * @return the message associated to the step, can be null
     */
    Message getMessage();

    /**
     * @return the parent step
     */
    JobProgressStep getParent();

    /**
     * @param <S> the type of the step
     * @return the children steps
     */
    <S extends JobProgressStep> List<S> getChildren();

    /**
     * @return progress of the current step between 0 and 1
     */
    double getOffset();

    /**
     * @return the time spend executing the step, in nanoseconds
     */
    long getElapsedTime();
}
