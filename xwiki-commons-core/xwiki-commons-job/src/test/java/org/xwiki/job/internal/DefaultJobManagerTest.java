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

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.context.internal.DefaultExecution;
import org.xwiki.context.internal.DefaultExecutionContextManager;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.annotation.MockingRequirement;

/**
 * Unit tests for {@link DefaultJobManager}.
 * 
 * @version $Id$
 */
@ComponentList({
    DefaultExecution.class,
    DefaultExecutionContextManager.class
})
public class DefaultJobManagerTest extends AbstractMockingComponentTestCase
{
    /**
     * The object being tested.
     */
    @MockingRequirement(exceptions = {ExecutionContextManager.class, Execution.class})
    private DefaultJobManager jobManager;

    @Test
    public void testGetJobStatusForUnexistingJob() throws Exception
    {
        final List<String> jobId = Arrays.asList("unexisting-job");
        final JobStatusStorage jobStatusStorage = getComponentManager().getInstance(JobStatusStorage.class);

        getMockery().checking(new Expectations()
        {
            {
                oneOf(jobStatusStorage).getJobStatus(jobId);
                will(returnValue(null));
            }
        });

        Assert.assertNull(jobManager.getJobStatus(jobId));
    }
}
