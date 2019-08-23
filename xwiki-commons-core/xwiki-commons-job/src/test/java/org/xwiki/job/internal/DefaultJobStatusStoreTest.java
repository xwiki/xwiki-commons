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
import org.junit.jupiter.api.Test;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.internal.MapCache;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultJobStatusStore}.
 *
 * @version $Id$
 */
@ComponentTest
public class DefaultJobStatusStoreTest
{
    @InjectMockComponents
    private DefaultJobStatusStore store;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    private JobManagerConfiguration jobManagerConfiguration;

    @MockComponent
    private CacheManager cacheManager;

    @BeforeComponent
    public void before() throws Exception
    {
        FileUtils.deleteDirectory(new File("target/test/jobs/"));
        FileUtils.copyDirectory(new File("src/test/resources/jobs/"), new File("target/test/jobs/"));

        when(this.jobManagerConfiguration.getStorage()).thenReturn(new File("target/test/jobs/status"));
        when(this.jobManagerConfiguration.getJobStatusCacheSize()).thenReturn(100);

        when(this.cacheManager.createNewCache(any())).thenReturn(new MapCache<>());
    }

    @Test
    public void getJobStatusWithNullId()
    {
        JobStatus jobStatus = this.store.getJobStatus(null);

        assertNotNull(jobStatus);
        assertNull(jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        assertSame(jobStatus, this.store.getJobStatus(null));
    }

    @Test
    public void getJobStatusWithMultipleId()
    {
        JobStatus jobStatus = this.store.getJobStatus(Arrays.asList("id1", "id2"));

        assertNotNull(jobStatus);
        assertEquals(Arrays.asList("id1", "id2"), jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        assertSame(jobStatus, this.store.getJobStatus(Arrays.asList("id1", "id2")));
    }

    @Test
    public void getJobStatusInOldPlace()
    {
        JobStatus jobStatus = this.store.getJobStatus(Arrays.asList("id1", "id2", "id3"));

        assertNotNull(jobStatus);
        assertEquals(Arrays.asList("id1", "id2", "id3"), jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());
    }

    @Test
    public void getJobStatusInWrongPlaceAndWithInvalidLogArgument()
    {
        JobStatus jobStatus = this.store.getJobStatus(Arrays.asList("invalidlogargument"));

        assertNotNull(jobStatus);
        assertEquals(3, jobStatus.getLog().size());
    }

    @Test
    public void getJobStatusThatDoesNotExist()
    {
        JobStatus jobStatus = this.store.getJobStatus(Arrays.asList("nostatus"));

        assertNull(jobStatus);

        JobStatusSerializer mockSerializer = mock(JobStatusSerializer.class);
        ReflectionUtils.setFieldValue(this.store, "serializer", mockSerializer);

        jobStatus = this.store.getJobStatus(Arrays.asList("nostatus"));
        assertNull(jobStatus);

        verifyNoMoreInteractions(mockSerializer);
    }

    @Test
    public void removeJobStatus()
    {
        List<String> id = null;

        JobStatus jobStatus = this.store.getJobStatus(id);

        assertNotNull(jobStatus);
        assertNull(jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        assertSame(jobStatus, this.store.getJobStatus(id));

        this.store.remove(id);

        assertSame(null, this.store.getJobStatus(id));
    }

    @Test
    public void storeJobStatus()
    {
        List<String> id = Arrays.asList("newstatus");

        DefaultRequest request = new DefaultRequest();
        request.setId(id);
        JobStatus jobStatus = new DefaultJobStatus("type", request, null, null, null);

        this.store.store(jobStatus);

        assertSame(jobStatus, this.store.getJobStatus(id));
    }
}
