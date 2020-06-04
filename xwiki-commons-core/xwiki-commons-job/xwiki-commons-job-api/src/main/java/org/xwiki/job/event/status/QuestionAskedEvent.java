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
package org.xwiki.job.event.status;

import java.util.List;

/**
 * Event fired when an interactive job asks a question. The event listeners have the chance to answer the question
 * before it reaches the user. The event is send with the following parameters:
 * <ul>
 * <li>source: the related {@link JobStatus} instance that can be used to access the question</li>
 * <li>data: {@code null}</li>
 * </ul>
 * 
 * @version $Id$
 * @since 7.1RC1
 */
public class QuestionAskedEvent extends AbstractQuestionEvent
{
    /**
     * Flag indicating if the question that triggered the event has been answered by one of the event listeners.
     */
    private boolean answered;

    /**
     * Creates a new event.
     */
    public QuestionAskedEvent()
    {
    }

    /**
     * Creates a new event for a question of the specified type.
     * 
     * @param questionType the type of question that has been asked
     */
    public QuestionAskedEvent(String questionType)
    {
        super(questionType);
    }

    /**
     * Creates a new event for a question of the specified type and a job with the given id.
     * 
     * @param questionType the type of question that has been asked
     * @param jobId the id of the job that raised the question that triggered this event
     */
    public QuestionAskedEvent(String questionType, List<String> jobId)
    {
        super(questionType, jobId);
    }

    /**
     * Specify that the question that triggered this event has been answered. This method is normally called by an event
     * listener that answers the question by modifying the object that represents the question.
     */
    public void answered()
    {
        this.answered = true;
    }

    /**
     * @return {@code true} if the question that triggered this event has been answered by one of the event listeners,
     *         {@code false} otherwise
     */
    public boolean isAnswered()
    {
        return this.answered;
    }
}
