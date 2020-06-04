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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Common implementation for job question events.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
public abstract class AbstractQuestionEvent implements QuestionEvent
{
    /**
     * The type of question that triggered this event.
     */
    private final String questionType;

    /**
     * The id of the job that raised the question that triggered this event.
     */
    private final List<String> jobId;

    /**
     * Creates an event that can be triggered by any type of question.
     */
    public AbstractQuestionEvent()
    {
        this(null);
    }

    /**
     * Creates an event that can be triggered by a question of the specified type.
     * 
     * @param questionType the type of question that can trigger this event
     */
    public AbstractQuestionEvent(String questionType)
    {
        this(questionType, null);
    }

    /**
     * Creates an event that can be triggered by a question of the specified type when asked by a job with the specified
     * id.
     * 
     * @param questionType the type of question that can trigger this event
     * @param jobId the id of the job that raised the question that triggered this event
     */
    public AbstractQuestionEvent(String questionType, List<String> jobId)
    {
        this.questionType = questionType;
        this.jobId = jobId;
    }

    @Override
    public String getQuestionType()
    {
        return this.questionType;
    }

    @Override
    public List<String> getJobId()
    {
        return this.jobId;
    }

    @Override
    public boolean matches(Object event)
    {
        return this.getClass() == event.getClass() && matchesQuestionType(((QuestionEvent) event).getQuestionType())
            && matchesJobId(((QuestionEvent) event).getJobId());
    }

    private boolean matchesQuestionType(String questionType)
    {
        return questionType == null || this.questionType == null || this.questionType.equals(questionType);
    }

    private boolean matchesJobId(List<String> jobId)
    {
        return jobId == null || this.jobId == null || this.jobId.equals(jobId);
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(7, 11).append(this.jobId).append(this.questionType).build();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null) {
            return false;
        }

        if (object == this) {
            return true;
        }

        if (object.getClass() != getClass()) {
            return false;
        }

        QuestionEvent event = (QuestionEvent) object;
        return new EqualsBuilder().append(this.jobId, event.getJobId())
            .append(this.questionType, event.getQuestionType()).build();
    }
}
