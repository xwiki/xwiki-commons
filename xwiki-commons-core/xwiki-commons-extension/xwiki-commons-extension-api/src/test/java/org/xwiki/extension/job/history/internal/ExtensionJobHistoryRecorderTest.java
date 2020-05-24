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
package org.xwiki.extension.job.history.internal;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.extension.job.ExtensionRequest;
import org.xwiki.extension.job.history.ExtensionJobHistory;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.extension.job.history.QuestionRecorder;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.Job;
import org.xwiki.job.event.JobFinishedEvent;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.job.event.status.QuestionAnsweredEvent;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExtensionJobHistoryRecorder}.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@ComponentTest
class ExtensionJobHistoryRecorderTest
{
    @InjectMockComponents
    private ExtensionJobHistoryRecorder recorder;

    @MockComponent
    private ExtensionJobHistory history;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @BeforeComponent
    void configure() throws Exception
    {
        Provider<ComponentManager> componentManagerProvider =
            this.componentManager.registerMockComponent(
                new DefaultParameterizedType(null, Provider.class, ComponentManager.class), "context");
        when(componentManagerProvider.get()).thenReturn(this.componentManager);
    }

    @Test
    void record() throws Exception
    {
        ExtensionRequest request = mock(ExtensionRequest.class);
        when(request.getId()).thenReturn(Arrays.asList("job", "id"));

        DefaultJobStatus<ExtensionRequest> status = mock(DefaultJobStatus.class);
        when(status.getRequest()).thenReturn(request);
        when(status.getStartDate()).thenReturn(new Date());
        when(status.getQuestion()).thenReturn("What's up?");

        Job job = mock(Job.class);
        when(job.getType()).thenReturn("jobType");
        when(job.getRequest()).thenReturn(request);
        when(job.getStatus()).thenReturn(status);

        ParameterizedType questionRecorderType =
            new DefaultParameterizedType(null, QuestionRecorder.class, String.class);
        QuestionRecorder<String> questionRecorder = this.componentManager.registerMockComponent(questionRecorderType);

        this.recorder.onEvent(new JobStartedEvent(), job, null);
        this.recorder.onEvent(new QuestionAnsweredEvent(), job.getStatus(), null);
        this.recorder.onEvent(new JobFinishedEvent(), job, null);

        verify(questionRecorder).record((String) status.getQuestion());

        ArgumentCaptor<ExtensionJobHistoryRecord> recordCaptor =
            ArgumentCaptor.forClass(ExtensionJobHistoryRecord.class);
        verify(this.history).addRecord(recordCaptor.capture());

        ExtensionJobHistoryRecord record = recordCaptor.getValue();
        assertEquals(job.getType(), record.getJobType());
        assertSame(job.getRequest(), record.getRequest());
        assertEquals(Collections.singletonMap(String.class.getName(), questionRecorder), record.getAnswers());
        assertEquals(job.getStatus().getStartDate(), record.getStartDate());
    }
}
