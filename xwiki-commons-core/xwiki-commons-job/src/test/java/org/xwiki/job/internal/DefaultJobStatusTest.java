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

import org.junit.Test;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LoggerManager;
import org.xwiki.observation.ObservationManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultJobStatus}.
 * 
 * @version $Id$
 */
public class DefaultJobStatusTest
{
    private ObservationManager observationManager = mock(ObservationManager.class);

    private LoggerManager loggerManager = mock(LoggerManager.class);

    @Test
    public void subJobQuestionIsForwardedToParent() throws Exception
    {
        JobStatus parentJobStatus = mock(JobStatus.class);
        DefaultJobStatus<DefaultRequest> jobStatus =
            new DefaultJobStatus<>(new DefaultRequest(), this.observationManager, this.loggerManager, parentJobStatus);

        String question = "What's up?";
        jobStatus.ask(question);

        assertSame(question, jobStatus.getQuestion());
        verify(parentJobStatus).ask(question);

        jobStatus.answered();

        assertNull(jobStatus.getQuestion());
        verify(parentJobStatus).answered();
    }
}
