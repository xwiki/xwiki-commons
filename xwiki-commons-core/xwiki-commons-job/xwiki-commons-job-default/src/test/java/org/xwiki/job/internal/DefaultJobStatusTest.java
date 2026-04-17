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
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.xwiki.job.JobLogMDC;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.job.event.status.JobStatus.State;
import org.xwiki.job.event.status.QuestionAnsweredEvent;
import org.xwiki.job.event.status.QuestionAskedEvent;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.LoggerManager;
import org.xwiki.logging.tail.LoggerTail;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link DefaultJobStatus}.
 *
 * @version $Id$
 */
public class DefaultJobStatusTest
{
    private ObservationManager observationManager = mock(ObservationManager.class);

    private LoggerManager loggerManager = mock(LoggerManager.class);

    @AfterEach
    void afterEach()
    {
        MDC.clear();
    }

    @Test
    void subJobQuestionIsForwardedToParent() throws Exception
    {
        JobStatus parentJobStatus = mock(JobStatus.class);
        DefaultJobStatus<DefaultRequest> jobStatus = new DefaultJobStatus<>("type", new DefaultRequest(),
            parentJobStatus, this.observationManager, this.loggerManager);

        String question = "What's up?";
        jobStatus.ask(question);

        assertSame(question, jobStatus.getQuestion());
        verify(parentJobStatus).ask(question);

        jobStatus.answered();

        assertNull(jobStatus.getQuestion());
        verify(parentJobStatus).answered();

        // Only the parent job status should fire QuestionAsked/Answered events.
        verify(this.observationManager, never()).notify(any(Event.class), any());
        verify(this.observationManager, never()).notify(any(Event.class), any(), any());
    }

    @Test
    void fireQuestionAnsweredOK()
    {
        DefaultRequest request = new DefaultRequest();
        request.setId(Arrays.asList("test", "answered"));

        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", request, null, this.observationManager, this.loggerManager);

        jobStatus.answered();

        // The question type is null because we didn't asked any question.
        verify(this.observationManager).notify(new QuestionAnsweredEvent(null, request.getId()), jobStatus);
    }

    @Test
    void fireQuestionAskedOK() throws Exception
    {
        DefaultRequest request = new DefaultRequest();
        request.setId(Arrays.asList("test", "asked"));

        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", request, null, this.observationManager, this.loggerManager);

        QuestionAskedEvent questionAsked = new QuestionAskedEvent(String.class.getName(), request.getId());
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                QuestionAskedEvent event = (QuestionAskedEvent) invocation.getArguments()[0];
                event.answered();
                return null;
            }
        }).when(this.observationManager).notify(questionAsked, jobStatus);

        jobStatus.ask("What's up?");

        QuestionAnsweredEvent questionAnswered = new QuestionAnsweredEvent(String.class.getName(), request.getId());
        verify(this.observationManager).notify(questionAnswered, jobStatus);
    }

    @Test
    void defaultogQueue()
    {
        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", new DefaultRequest(), null, null, null);

        assertSame(jobStatus.getLog(), jobStatus.getLog());
        assertSame(jobStatus.getLog(), jobStatus.getLoggerTail());
    }

    @Test
    void getLogLevel()
    {
        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", new DefaultRequest(), null, null, null);

        assertEquals(0, jobStatus.getLog(LogLevel.TRACE).size());

        jobStatus.getLog().trace("message");

        assertEquals(1, jobStatus.getLog(LogLevel.TRACE).size());
        assertEquals("message", jobStatus.getLog(LogLevel.TRACE).get(0).getMessage());
    }

    @Test
    void setError()
    {
        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", new DefaultRequest(), null, null, null);

        assertNull(jobStatus.getError());

        Exception error = new Exception();
        jobStatus.setError(error);

        assertSame(error, jobStatus.getError());
    }

    @Test
    void setCancellable()
    {
        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", new DefaultRequest(), null, null, null);

        assertFalse(jobStatus.isCancelable());

        jobStatus.setCancelable(true);

        assertTrue(jobStatus.isCancelable());

        jobStatus.setCancelable(false);

        assertFalse(jobStatus.isCancelable());
    }

    @Test
    void setState()
    {
        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", new DefaultRequest(), null, null, null);

        assertSame(State.NONE, jobStatus.getState());

        jobStatus.setState(State.RUNNING);

        assertSame(State.RUNNING, jobStatus.getState());
    }

    @Test
    void setDates()
    {
        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", new DefaultRequest(), null, null, null);

        assertNull(jobStatus.getStartDate());
        assertNull(jobStatus.getEndDate());

        jobStatus.setStartDate(new Date(0));
        jobStatus.setEndDate(new Date(1));

        assertEquals(new Date(0), jobStatus.getStartDate());
        assertEquals(new Date(1), jobStatus.getEndDate());
    }

    @Test
    void setIsolated()
    {
        DefaultRequest request = new DefaultRequest();
        DefaultJobStatus<DefaultRequest> jobStatus = new DefaultJobStatus<>("type", request, null, null, null);

        assertTrue(jobStatus.isIsolated());

        jobStatus.setIsolated(false);

        assertFalse(jobStatus.isIsolated());

        jobStatus.setIsolated(true);

        assertTrue(jobStatus.isIsolated());

        request.setStatusLogIsolated(false);

        assertFalse(jobStatus.isIsolated());

        request.setStatusLogIsolated(true);

        assertTrue(jobStatus.isIsolated());
    }

    @Test
    void cancel()
    {
        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", new DefaultRequest(), null, null, null);

        assertFalse(jobStatus.isCanceled());

        jobStatus.cancel();

        assertTrue(jobStatus.isCanceled());
    }

    @Test
    void setLoggerTail()
    {
        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", new DefaultRequest(), null, null, null);

        assertNotNull(jobStatus.getLoggerTail());

        LoggerTail loggerTail = new LogQueue();

        jobStatus.setLoggerTail(loggerTail);

        assertSame(loggerTail, jobStatus.getLogTail());
        assertSame(loggerTail, jobStatus.getLog());

        jobStatus = new DefaultJobStatus<>("type", new DefaultRequest(), null, null, null);

        loggerTail = mock(LoggerTail.class);

        jobStatus.setLoggerTail(loggerTail);

        assertSame(loggerTail, jobStatus.getLogTail());
        assertNotSame(loggerTail, jobStatus.getLog());
        assertNotSame(jobStatus.getLog(), jobStatus.getLog());
    }

    @Test
    void startListeningTagsLogsWithMdc()
    {
        DefaultRequest request = new DefaultRequest();
        request.setId(Arrays.asList("Parent Job", "Space/Slash"));

        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>("type", request, null, this.observationManager, this.loggerManager);
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
        parentRequest.setId(Arrays.asList("parent"));

        DefaultJobStatus<DefaultRequest> parentStatus = spy(
            new DefaultJobStatus<>("parentType", parentRequest, null, this.observationManager, this.loggerManager));
        parentStatus.setLoggerTail(mock(LoggerTail.class));
        parentStatus.startListening();

        DefaultRequest childRequest = new DefaultRequest();
        childRequest.setId(Arrays.asList("child"));
        childRequest.setStatusLogIsolated(true);

        DefaultJobStatus<DefaultRequest> childStatus =
            new DefaultJobStatus<>("childType", childRequest, parentStatus, this.observationManager, this.loggerManager);
        childStatus.setLoggerTail(mock(LoggerTail.class));

        childStatus.startListening();

        verify(parentStatus).ignoreLogs(true);
        assertEquals("childType", MDC.get(JobLogMDC.KEY_JOB_TYPE));
        assertEquals("true", MDC.get(JobLogMDC.KEY_JOB_ISOLATED));
        assertEquals(JobLogMDC.toCleanId(childRequest.getId()), MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));

        childStatus.stopListening();

        verify(parentStatus).ignoreLogs(false);
        assertEquals("parentType", MDC.get(JobLogMDC.KEY_JOB_TYPE));
        assertEquals("true", MDC.get(JobLogMDC.KEY_JOB_ISOLATED));
        assertEquals(JobLogMDC.toCleanId(parentRequest.getId()), MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));

        parentStatus.stopListening();
    }

    @Test
    void nonIsolatedSubJobKeepsParentLogRoutingContext()
    {
        DefaultRequest parentRequest = new DefaultRequest();
        parentRequest.setId(Arrays.asList("parent"));

        DefaultJobStatus<DefaultRequest> parentStatus = spy(
            new DefaultJobStatus<>("parentType", parentRequest, null, this.observationManager, this.loggerManager));
        parentStatus.setLoggerTail(mock(LoggerTail.class));
        parentStatus.startListening();

        String parentCleanJobId = MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID);

        DefaultRequest childRequest = new DefaultRequest();
        childRequest.setId(Arrays.asList("child"));
        childRequest.setStatusLogIsolated(false);

        DefaultJobStatus<DefaultRequest> childStatus =
            new DefaultJobStatus<>("childType", childRequest, parentStatus, this.observationManager, this.loggerManager);
        childStatus.setLoggerTail(mock(LoggerTail.class));

        childStatus.startListening();

        verify(parentStatus, never()).ignoreLogs(true);
        assertEquals("childType", MDC.get(JobLogMDC.KEY_JOB_TYPE));
        assertEquals("false", MDC.get(JobLogMDC.KEY_JOB_ISOLATED));
        assertEquals("child", MDC.get(JobLogMDC.KEY_JOB_ID));
        assertEquals(parentCleanJobId, MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));

        childStatus.stopListening();

        assertEquals(parentCleanJobId, MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));

        parentStatus.stopListening();
    }

    @Test
    void nonIsolatedSubJobWithoutIdKeepsParentJobId()
    {
        DefaultRequest parentRequest = new DefaultRequest();
        parentRequest.setId(Arrays.asList("parent"));

        DefaultJobStatus<DefaultRequest> parentStatus = spy(
            new DefaultJobStatus<>("parentType", parentRequest, null, this.observationManager, this.loggerManager));
        parentStatus.setLoggerTail(mock(LoggerTail.class));
        parentStatus.startListening();

        String parentJobId = MDC.get(JobLogMDC.KEY_JOB_ID);
        String parentCleanJobId = MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID);

        DefaultRequest childRequest = new DefaultRequest();
        childRequest.setStatusLogIsolated(false);

        DefaultJobStatus<DefaultRequest> childStatus =
            new DefaultJobStatus<>("childType", childRequest, parentStatus, this.observationManager, this.loggerManager);
        childStatus.setLoggerTail(mock(LoggerTail.class));

        childStatus.startListening();

        assertEquals(parentJobId, MDC.get(JobLogMDC.KEY_JOB_ID));
        assertEquals(parentCleanJobId, MDC.get(JobLogMDC.KEY_JOB_CLEAN_ID));

        childStatus.stopListening();
        parentStatus.stopListening();
    }
}
