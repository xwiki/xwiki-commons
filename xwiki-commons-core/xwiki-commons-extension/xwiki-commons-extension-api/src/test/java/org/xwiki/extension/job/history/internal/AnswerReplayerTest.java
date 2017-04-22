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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.extension.job.history.QuestionRecorder;
import org.xwiki.extension.job.history.ReplayJobStatus;
import org.xwiki.job.event.status.QuestionAskedEvent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AnswerReplayer}.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
public class AnswerReplayerTest
{
    @Test
    public void replayAnswerOK()
    {
        QuestionAskedEvent questionAskedEvent = new QuestionAskedEvent();

        String question = "What's up?";
        QuestionRecorder<Object> questionRecorder = mock(QuestionRecorder.class);
        when(questionRecorder.replay(question)).thenReturn(true);

        Map<String, QuestionRecorder<Object>> answers = new HashMap<>();
        answers.put(question.getClass().getName(), questionRecorder);
        ExtensionJobHistoryRecord currentRecord = new ExtensionJobHistoryRecord(null, null, answers, null, null);

        ReplayJobStatus replayJobStatus = mock(ReplayJobStatus.class);
        when(replayJobStatus.getCurrentRecord()).thenReturn(currentRecord);
        when(replayJobStatus.getQuestion()).thenReturn(question);

        AnswerReplayer answerReplayer = new AnswerReplayer();
        answerReplayer.onEvent(questionAskedEvent, replayJobStatus, null);

        assertTrue(questionAskedEvent.isAnswered());
    }
}
