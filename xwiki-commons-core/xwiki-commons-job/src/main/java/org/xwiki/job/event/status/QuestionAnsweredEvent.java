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
 * Event fired after a question raised by an interactive job is answered. The event is send with the following
 * parameters:
 * <ul>
 * <li>source: the related {@link JobStatus} instance that can be used to access the question</li>
 * <li>data: {@code null}</li>
 * </ul>
 * 
 * @version $Id$
 * @since 7.1RC1
 */
public class QuestionAnsweredEvent extends AbstractQuestionEvent
{
    /**
     * Creates a new event.
     */
    public QuestionAnsweredEvent()
    {
    }

    /**
     * Creates a new event for a question of the specified type.
     * 
     * @param questionType the type of question that has been answered
     */
    public QuestionAnsweredEvent(String questionType)
    {
        super(questionType);
    }

    /**
     * Creates a new event for a question of the specified type and a job with the given id.
     * 
     * @param questionType the type of question that has been answered
     * @param jobId the id of the job that raised the question that triggered this event
     */
    public QuestionAnsweredEvent(String questionType, List<String> jobId)
    {
        super(questionType, jobId);
    }
}
