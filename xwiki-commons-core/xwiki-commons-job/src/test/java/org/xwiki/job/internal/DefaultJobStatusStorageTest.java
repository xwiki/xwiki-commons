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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.Test;
import org.slf4j.Logger;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;

/**
 * Unit tests for {@link DefaultJobStatusStorage}.
 * 
 * @version $Id$
 */
public class DefaultJobStatusStorageTest extends AbstractMockingComponentTestCase
{
    /**
     * The object being tested.
     */
    @MockingRequirement(exceptions = Logger.class)
    private DefaultJobStatusStorage storage;

    @Override
    protected void setupDependencies() throws Exception
    {
        super.setupDependencies();

        final JobManagerConfiguration jobManagerConfiguration =
            getComponentManager().getInstance(JobManagerConfiguration.class);

        getMockery().checking(new Expectations()
        {
            {
                oneOf(jobManagerConfiguration).getStorage();
                will(returnValue(new File("src/test/resources/jobs")));
            }
        });
    }

    @Test
    public void testGetJobStatusForUnexistingJob() throws Exception
    {
        JobStatus jobStatus = this.storage.getJobStatus((List<String>) null);

        Assert.assertNotNull(jobStatus);
        Assert.assertNull(jobStatus.getRequest().getId());
        Assert.assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        jobStatus = this.storage.getJobStatus(Arrays.asList("id1", "id2"));

        Assert.assertNotNull(jobStatus);
        Assert.assertEquals(Arrays.asList("id1", "id2"), jobStatus.getRequest().getId());
        Assert.assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        jobStatus = this.storage.getJobStatus(Arrays.asList("id1", "id2", "id3"));

        Assert.assertNotNull(jobStatus);
        Assert.assertEquals(Arrays.asList("id1", "id2", "id3"), jobStatus.getRequest().getId());
        Assert.assertEquals(JobStatus.State.FINISHED, jobStatus.getState());
    }
}
