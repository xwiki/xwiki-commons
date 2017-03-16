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

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.internal.MapCache;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultJobStatusStore}.
 *
 * @version $Id$
 */
public class DefaultJobStatusStoreTest
{
    @Rule
    public final MockitoComponentMockingRule<DefaultJobStatusStore> componentManager =
        new MockitoComponentMockingRule<>(DefaultJobStatusStore.class);

    @Before
    public void before() throws Exception
    {
        JobManagerConfiguration jobManagerConfiguration =
            this.componentManager.getInstance(JobManagerConfiguration.class);

        FileUtils.deleteDirectory(new File("target/test/jobs/"));
        FileUtils.copyDirectory(new File("src/test/resources/jobs/"), new File("target/test/jobs/"));

        when(jobManagerConfiguration.getStorage()).thenReturn(new File("target/test/jobs/status"));
        when(jobManagerConfiguration.getJobStatusCacheSize()).thenReturn(100);

        CacheManager cacheManagerMock = this.componentManager.getInstance(CacheManager.class);
        when(cacheManagerMock.createNewCache(any())).thenReturn(new MapCache<>());
    }

    @Test
    public void getJobStatusWithNullId() throws Exception
    {
        JobStatus jobStatus = this.componentManager.getComponentUnderTest().getJobStatus((List<String>) null);

        Assert.assertNotNull(jobStatus);
        Assert.assertNull(jobStatus.getRequest().getId());
        Assert.assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        Assert.assertSame(jobStatus, this.componentManager.getComponentUnderTest().getJobStatus((List<String>) null));
    }

    @Test
    public void getJobStatusWithMultipleId() throws Exception
    {
        JobStatus jobStatus = this.componentManager.getComponentUnderTest().getJobStatus(Arrays.asList("id1", "id2"));

        Assert.assertNotNull(jobStatus);
        Assert.assertEquals(Arrays.asList("id1", "id2"), jobStatus.getRequest().getId());
        Assert.assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        Assert.assertSame(jobStatus,
            this.componentManager.getComponentUnderTest().getJobStatus(Arrays.asList("id1", "id2")));
    }

    @Test
    public void getJobStatusInOldPlace() throws Exception
    {
        JobStatus jobStatus =
            this.componentManager.getComponentUnderTest().getJobStatus(Arrays.asList("id1", "id2", "id3"));

        Assert.assertNotNull(jobStatus);
        Assert.assertEquals(Arrays.asList("id1", "id2", "id3"), jobStatus.getRequest().getId());
        Assert.assertEquals(JobStatus.State.FINISHED, jobStatus.getState());
    }

    @Test
    public void getJobStatusInWrongPlaceAndWithInvalidLogArgument() throws Exception
    {
        JobStatus jobStatus =
            this.componentManager.getComponentUnderTest().getJobStatus(Arrays.asList("invalidlogargument"));

        Assert.assertEquals(3, jobStatus.getLog().size());
    }

    @Test
    public void getJobStatusThatDoesNotExist() throws Exception
    {
        JobStatus jobStatus = this.componentManager.getComponentUnderTest().getJobStatus(Arrays.asList("nostatus"));

        assertNull(jobStatus);

        JobStatusSerializer mockSerializer = mock(JobStatusSerializer.class);
        ReflectionUtils.setFieldValue(this.componentManager.getComponentUnderTest(), "serializer", mockSerializer);

        jobStatus = this.componentManager.getComponentUnderTest().getJobStatus(Arrays.asList("nostatus"));
        assertNull(jobStatus);

        verifyNoMoreInteractions(mockSerializer);
    }

    @Test
    public void removeJobStatus() throws ComponentLookupException
    {
        List<String> id = null;

        JobStatus jobStatus = this.componentManager.getComponentUnderTest().getJobStatus(id);

        Assert.assertNotNull(jobStatus);
        Assert.assertNull(jobStatus.getRequest().getId());
        Assert.assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        Assert.assertSame(jobStatus, this.componentManager.getComponentUnderTest().getJobStatus(id));

        this.componentManager.getComponentUnderTest().remove(id);

        Assert.assertSame(null, this.componentManager.getComponentUnderTest().getJobStatus(id));
    }

    @Test
    public void storeJobStatus() throws ComponentLookupException
    {
        List<String> id = Arrays.asList("newstatus");

        DefaultRequest request = new DefaultRequest();
        request.setId(id);
        JobStatus jobStatus = new DefaultJobStatus("type", request, null, null, null);

        this.componentManager.getComponentUnderTest().store(jobStatus);

        Assert.assertSame(jobStatus, this.componentManager.getComponentUnderTest().getJobStatus(id));
    }
}
