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

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.internal.DefaultExecution;
import org.xwiki.context.internal.DefaultExecutionContextManager;
import org.xwiki.job.JobStatusStore;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultJobManager}.
 *
 * @version $Id$
 */
@ComponentTest
// @formatter:off
@ComponentList({
    DefaultExecution.class,
    DefaultExecutionContextManager.class
})
// @formatter:on
class DefaultJobManagerTest
{
    @InjectMockComponents
    private DefaultJobManager jobManager;

    @InjectComponentManager
    private ComponentManager componentManager;

    @Test
    void getJobStatusForUnexistingJob() throws Exception
    {
        List<String> jobId = Arrays.asList("unexisting-job");
        JobStatusStore jobStatusStorage = componentManager.getInstance(JobStatusStore.class);
        when(jobStatusStorage.getJobStatus(jobId)).thenReturn(null);

        assertNull(jobManager.getJobStatus(jobId));
    }
}
