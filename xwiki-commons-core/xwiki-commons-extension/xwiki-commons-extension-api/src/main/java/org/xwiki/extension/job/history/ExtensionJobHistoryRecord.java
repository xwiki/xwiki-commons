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
package org.xwiki.extension.job.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.extension.job.ExtensionRequest;

/**
 * A record in the {@link ExtensionJobHistory}.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
public class ExtensionJobHistoryRecord
{
    private final String jobType;

    private final ExtensionRequest request;

    private final Map<String, QuestionRecorder<Object>> answers;

    private final Date startDate;

    private final Date endDate;

    /**
     * Creates a new unmodifiable history record.
     * 
     * @param jobType the job type (normally the job component hint, e.g. "install")
     * @param request the extension request
     * @param answers the answers that were given by the user to the questions asked by the job specified by this
     *            history record (if the job was interactive); the key in the given map identifies the question type;
     *            the value represents the recorded answers for the corresponding question type
     * @param startDate the date when the job execution started
     * @param endDate the date when the job execution ended
     */
    public ExtensionJobHistoryRecord(String jobType, ExtensionRequest request,
        Map<String, QuestionRecorder<Object>> answers, Date startDate, Date endDate)
    {
        this.jobType = jobType;
        this.request = request;
        this.answers =
            answers != null ? Collections.unmodifiableMap(answers) : Collections
                .<String, QuestionRecorder<Object>>emptyMap();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * @return the job type (normally the job component hint, e.g. "install")
     */
    public String getJobType()
    {
        return jobType;
    }

    /**
     * @return the extension request
     */
    public ExtensionRequest getRequest()
    {
        return request;
    }

    /**
     * @return the answers that were given by the user to the questions asked by the job specified by this history
     *         record (if the job was interactive); the key in the returned map identifies the question type; the value
     *         represents the recorded answers for the corresponding question type
     */
    public Map<String, QuestionRecorder<Object>> getAnswers()
    {
        return answers;
    }

    /**
     * @return the date when the job execution started
     */
    public Date getStartDate()
    {
        return startDate;
    }

    /**
     * @return the date when the job execution ended
     */
    public Date getEndDate()
    {
        return endDate;
    }

    /**
     * @return a string that can be used to identify this record
     */
    public String getId()
    {
        List<Object> parts = new ArrayList<>();
        parts.add(this.endDate.getTime());
        parts.add(this.jobType);
        if (this.request.hasNamespaces()) {
            parts.addAll(this.request.getNamespaces());
        }
        return StringUtils.join(parts, '-');
    }
}
