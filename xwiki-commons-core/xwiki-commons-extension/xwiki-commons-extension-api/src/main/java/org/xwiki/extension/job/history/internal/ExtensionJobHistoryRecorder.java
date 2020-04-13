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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.extension.job.ExtensionRequest;
import org.xwiki.extension.job.history.ExtensionJobHistory;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.extension.job.history.QuestionRecorder;
import org.xwiki.extension.job.history.ReplayJobStatus;
import org.xwiki.job.Job;
import org.xwiki.job.event.JobFinishedEvent;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.job.event.status.QuestionAnsweredEvent;
import org.xwiki.job.internal.AbstractJobStatus;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

/**
 * Records the extension jobs that have been executed.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@Component
@Named(ExtensionJobHistoryRecorder.NAME)
@Singleton
public class ExtensionJobHistoryRecorder extends AbstractEventListener
{
    /**
     * The name of this event listener (and its component hint at the same time).
     */
    public static final String NAME = "ExtensionJobHistoryRecorder";

    @Inject
    private ExtensionJobHistory history;

    @Inject
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    /**
     * The recorded answers. The key is the job id and the value represents the answers grouped by question type.
     */
    private Map<String, Map<String, QuestionRecorder<Object>>> answers = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     */
    public ExtensionJobHistoryRecorder()
    {
        super(NAME, new JobStartedEvent(), new QuestionAnsweredEvent(), new JobFinishedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof JobStartedEvent) {
            onJobStarted((Job) source);
        } else if (event instanceof QuestionAnsweredEvent) {
            onQuestionAnswered((JobStatus) source);
        } else if (event instanceof JobFinishedEvent) {
            onJobFinished((Job) source, data);
        }
    }

    private void onJobStarted(Job job)
    {
        if (!(job.getRequest() instanceof ExtensionRequest)) {
            // This is not an extension job.
            return;
        }

        if (job.getStatus() instanceof AbstractJobStatus && isSubJob((AbstractJobStatus<?>) job.getStatus())) {
            // We record only the jobs that have been triggered explicitly or that are part of a replay.
            return;
        }

        String jobId = StringUtils.join(job.getRequest().getId(), '/');
        if (jobId != null) {
            this.answers.put(jobId, new HashMap<>());
        }
    }

    private <T extends AbstractJobStatus<?>> boolean isSubJob(T jobStatus)
    {
        return jobStatus.isSubJob() && !(jobStatus.getParentJobStatus() instanceof ReplayJobStatus);
    }

    private void onQuestionAnswered(JobStatus jobStatus)
    {
        Object question = jobStatus.getQuestion();
        if (question != null) {
            String jobId = StringUtils.join(getActualJobId(jobStatus), '/');
            Map<String, QuestionRecorder<Object>> jobAnswers = this.answers.get(jobId);
            if (jobAnswers != null) {
                QuestionRecorder<Object> questionRecorder = getQuestionRecorder(question, jobAnswers);
                if (questionRecorder != null) {
                    questionRecorder.record(question);
                }
            }
        }
    }

    private List<String> getActualJobId(JobStatus jobStatus)
    {
        if (jobStatus instanceof ReplayJobStatus) {
            ExtensionJobHistoryRecord currentRecord = ((ReplayJobStatus) jobStatus).getCurrentRecord();
            return currentRecord != null ? currentRecord.getRequest().getId() : null;
        } else {
            return jobStatus.getRequest().getId();
        }
    }

    private QuestionRecorder<Object> getQuestionRecorder(Object question,
        Map<String, QuestionRecorder<Object>> questionRecorders)
    {
        String questionType = question.getClass().getName();
        QuestionRecorder<Object> questionRecorder = questionRecorders.get(questionType);
        if (questionRecorder == null) {
            questionRecorder = getQuestionRecorder(question);
            if (questionRecorder != null) {
                questionRecorders.put(questionType, questionRecorder);
            }
        }
        return questionRecorder;
    }

    private <T> QuestionRecorder<T> getQuestionRecorder(T question)
    {
        ParameterizedType questionRecorderType =
            new DefaultParameterizedType(null, QuestionRecorder.class, question.getClass());
        try {
            return this.contextComponentManagerProvider.get().getInstance(questionRecorderType);
        } catch (ComponentLookupException e) {
            return null;
        }
    }

    private void onJobFinished(Job job, Object data)
    {
        String jobId = StringUtils.join(job.getRequest().getId(), '/');
        Map<String, QuestionRecorder<Object>> jobAnswers = jobId != null ? this.answers.remove(jobId) : null;

        if (data instanceof Throwable || jobAnswers == null) {
            // The job execution has failed or the job has not been recorded.
            return;
        }

        // We assume the job ended at this moment because the actual end date is set on the job status after all the
        // event listeners are called so it's not available right now.
        this.history.addRecord(new ExtensionJobHistoryRecord(job.getType(), (ExtensionRequest) job.getRequest(),
            jobAnswers, job.getStatus().getStartDate(), new Date()));
    }
}
