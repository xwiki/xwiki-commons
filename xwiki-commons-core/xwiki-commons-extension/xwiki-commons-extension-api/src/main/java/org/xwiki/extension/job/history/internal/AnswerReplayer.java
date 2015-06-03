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

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.extension.job.history.QuestionRecorder;
import org.xwiki.extension.job.history.ReplayJobStatus;
import org.xwiki.job.event.status.QuestionAskedEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * Listens to {@link QuestionAskedEvent}s and replays a recorded answer, if such an answer is available.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@Component
@Named(AnswerReplayer.NAME)
@Singleton
public class AnswerReplayer implements EventListener
{
    /**
     * The name of this event listener (and its component hint at the same time).
     */
    public static final String NAME = "AnswerReplayer";

    private static final List<Event> EVENTS = Collections.<Event>singletonList(new QuestionAskedEvent());

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (source instanceof ReplayJobStatus) {
            replayAnswer((QuestionAskedEvent) event, (ReplayJobStatus) source);
        }
    }

    private void replayAnswer(QuestionAskedEvent event, ReplayJobStatus replayJobStatus)
    {
        Object question = replayJobStatus.getQuestion();
        ExtensionJobHistoryRecord currentRecord = replayJobStatus.getCurrentRecord();
        if (question != null && currentRecord != null) {
            QuestionRecorder<Object> answers = currentRecord.getAnswers().get(question.getClass().getName());
            if (answers != null && answers.replay(question)) {
                event.answered();
            }
        }
    }
}
