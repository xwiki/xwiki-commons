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
package org.xwiki.job;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.xwiki.logging.tail.LoggerTail;
import org.xwiki.observation.ObservationManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests the MDC handling of {@link DefaultJobStatus}.
 *
 * @version $Id$
 */
class DefaultJobStatusMdcTest
{
    private final ObservationManager observationManager = mock(ObservationManager.class);

    @AfterEach
    void afterEach()
    {
        MDC.clear();
    }

    @Test
    void startListeningTagsLogsWithMdc()
    {
        DefaultRequest request = new DefaultRequest();
        request.setId(Arrays.asList("Parent Job", "Space/Slash"));

        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", request, null, this.observationManager, null);
        jobStatus.setLoggerTail(mock(LoggerTail.class));

        jobStatus.startListening();

        assertEquals("true", MDC.get(JobLogMDC.KEY_JOB));
        assertEquals("type", MDC.get(JobLogMDC.KEY_JOB_TYPE));
        assertEquals("true", MDC.get(JobLogMDC.KEY_JOB_ISOLATED));
        assertEquals("Parent Job/Space/Slash", MDC.get(JobLogMDC.KEY_JOB_ID));
        assertEquals(JobLogMDC.toCleanId(request.getId()), MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));

        jobStatus.stopListening();

        assertNull(MDC.get(JobLogMDC.KEY_JOB));
        assertNull(MDC.get(JobLogMDC.KEY_JOB_TYPE));
        assertNull(MDC.get(JobLogMDC.KEY_JOB_ISOLATED));
        assertNull(MDC.get(JobLogMDC.KEY_JOB_ID));
        assertNull(MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));
    }

    @Test
    void isolatedSubJobTemporarilyMutesParentLogCapture()
    {
        DefaultRequest parentRequest = new DefaultRequest();
        parentRequest.setId(List.of("parent"));

        DefaultJobStatus<DefaultRequest> parentStatus = spy(
            new DefaultJobStatus<>("parentType", parentRequest, null, this.observationManager, null));
        parentStatus.setLoggerTail(mock(LoggerTail.class));
        parentStatus.startListening();

        DefaultRequest childRequest = new DefaultRequest();
        childRequest.setId(List.of("child"));
        childRequest.setStatusLogIsolated(true);

        DefaultJobStatus<DefaultRequest> childStatus =
            new DefaultJobStatus<>("childType", childRequest, parentStatus, this.observationManager, null);
        childStatus.setLoggerTail(mock(LoggerTail.class));

        childStatus.startListening();

        verify(parentStatus).ignoreLogs(true);
        assertEquals("childType", MDC.get(JobLogMDC.KEY_JOB_TYPE));
        assertEquals("true", MDC.get(JobLogMDC.KEY_JOB_ISOLATED));
        assertEquals(JobLogMDC.toCleanId(childRequest.getId()), MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));

        childStatus.stopListening();

        verify(parentStatus).ignoreLogs(false);
        assertEquals("parentType", MDC.get(JobLogMDC.KEY_JOB_TYPE));
        assertEquals(JobLogMDC.toCleanId(parentRequest.getId()), MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));

        parentStatus.stopListening();
    }

    @Test
    void nonIsolatedSubJobKeepsParentLogRoutingContext()
    {
        DefaultRequest parentRequest = new DefaultRequest();
        parentRequest.setId(List.of("parent"));

        DefaultJobStatus<DefaultRequest> parentStatus = spy(
            new DefaultJobStatus<>("parentType", parentRequest, null, this.observationManager, null));
        parentStatus.setLoggerTail(mock(LoggerTail.class));
        parentStatus.startListening();

        String parentCleanJobId = MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID);

        DefaultRequest childRequest = new DefaultRequest();
        childRequest.setId(List.of("child"));
        childRequest.setStatusLogIsolated(false);

        DefaultJobStatus<DefaultRequest> childStatus =
            new DefaultJobStatus<>("childType", childRequest, parentStatus, this.observationManager, null);
        childStatus.setLoggerTail(mock(LoggerTail.class));

        childStatus.startListening();

        verify(parentStatus, never()).ignoreLogs(true);
        assertEquals("childType", MDC.get(JobLogMDC.KEY_JOB_TYPE));
        assertEquals("false", MDC.get(JobLogMDC.KEY_JOB_ISOLATED));
        assertEquals("child", MDC.get(JobLogMDC.KEY_JOB_ID));
        assertEquals(parentCleanJobId, MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));

        childStatus.stopListening();
        parentStatus.stopListening();
    }

    @Test
    void nonIsolatedSubJobWithoutIdKeepsParentJobId()
    {
        DefaultRequest parentRequest = new DefaultRequest();
        parentRequest.setId(List.of("parent"));

        DefaultJobStatus<DefaultRequest> parentStatus = spy(
            new DefaultJobStatus<>("parentType", parentRequest, null, this.observationManager, null));
        parentStatus.setLoggerTail(mock(LoggerTail.class));
        parentStatus.startListening();

        String parentJobId = MDC.get(JobLogMDC.KEY_JOB_ID);
        String parentCleanJobId = MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID);

        DefaultRequest childRequest = new DefaultRequest();
        childRequest.setStatusLogIsolated(false);

        DefaultJobStatus<DefaultRequest> childStatus =
            new DefaultJobStatus<>("childType", childRequest, parentStatus, this.observationManager, null);
        childStatus.setLoggerTail(mock(LoggerTail.class));

        childStatus.startListening();

        assertEquals(parentJobId, MDC.get(JobLogMDC.KEY_JOB_ID));
        assertEquals(parentCleanJobId, MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));

        childStatus.stopListening();
        parentStatus.stopListening();
    }
}
