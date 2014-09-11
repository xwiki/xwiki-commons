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

import java.util.Stack;

import javax.inject.Inject;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.job.Job;
import org.xwiki.job.JobContext;

/**
 * Default implementation of {@link JobContext}.
 *
 * @version $Id$
 * @since 4.0M2
 */
@Component
public class DefaultJobContext implements JobContext
{
    /**
     * The key associated to the stack of job currently being executed in the {@link ExecutionContext}.
     */
    private static final String KEY_CURRENTJOB = "job.current";

    /**
     * Used to access the current job from the context.
     */
    @Inject
    private Execution execution;

    /**
     * @param create if true create the stack if it does not exists
     * @return the stack containing the current jobs
     */
    private Stack<Job> getJobstack(boolean create)
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null) {
            Stack<Job> stack = (Stack<Job>) context.getProperty(KEY_CURRENTJOB);

            if (stack == null && create) {
                stack = new Stack<Job>();
                context.setProperty(KEY_CURRENTJOB, stack);
            }

            return stack;
        }

        return null;
    }

    @Override
    public Job getCurrentJob()
    {
        Stack<Job> stack = getJobstack(false);

        return stack == null || stack.isEmpty() ? null : stack.peek();
    }

    @Override
    public void pushCurrentJob(Job job)
    {
        getJobstack(true).push(job);
    }

    @Override
    public void popCurrentJob()
    {
        getJobstack(true).pop();
    }
}
