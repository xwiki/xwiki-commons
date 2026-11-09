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
package org.xwiki.job;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.annotation.Component;
import org.xwiki.job.event.status.JobStatus.State;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Validate {@link AbstractJob}.
 *
 * @version $Id$
 */
@ComponentTest
class AbstractJobTest
{
    /**
     * A job which fails with the configured error.
     */
    @Component
    @Named(TestJob.TYPE)
    @Singleton
    public static class TestJob extends AbstractJob<DefaultRequest, DefaultJobStatus<DefaultRequest>>
    {
        public static final String TYPE = "test";

        private Exception error;

        private boolean interruptThread;

        private boolean cancel;

        private boolean cancelable;

        @Override
        public String getType()
        {
            return TYPE;
        }

        @Override
        protected void runInternal() throws Exception
        {
            getStatus().setCancelable(this.cancelable);

            if (this.cancel) {
                getStatus().cancel();
            }

            if (this.interruptThread) {
                Thread.currentThread().interrupt();
            }

            if (this.error != null) {
                throw this.error;
            }
        }
    }

    private static final String ERROR_MESSAGE = "Exception thrown during job execution";

    @InjectMockComponents
    private TestJob job;

    @MockComponent
    private JobStatusStore store;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @AfterEach
    void afterEach()
    {
        // Make sure a test does not leave the thread interrupted for the next one
        Thread.interrupted();
    }

    private DefaultJobStatus<DefaultRequest> run()
    {
        this.job.initialize(new DefaultRequest());
        this.job.run();

        return this.job.getStatus();
    }

    private DefaultJobStatus<DefaultRequest> initialize(String id)
    {
        DefaultRequest request = new DefaultRequest();
        if (id != null) {
            request.setId(id);
        }
        this.job.initialize(request);

        return this.job.getStatus();
    }

    @Test
    void jobFinishedWhenSuccessful()
    {
        DefaultJobStatus<DefaultRequest> status = run();

        assertSame(State.FINISHED, status.getState());
        assertNull(status.getError());
        assertFalse(status.isCanceled());
    }

    @Test
    void jobFinishedWhenSuccessfulAndThreadInterrupted()
    {
        // A job which did its work but ignored the interruption is not canceled
        this.job.interruptThread = true;

        DefaultJobStatus<DefaultRequest> status = run();

        assertSame(State.FINISHED, status.getState());
        assertNull(status.getError());
        assertFalse(status.isCanceled());
    }

    @Test
    void jobFinishedWhenFailed()
    {
        this.job.error = new IOException("failed");

        DefaultJobStatus<DefaultRequest> status = run();

        assertSame(State.FINISHED, status.getState());
        assertSame(this.job.error, status.getError());
        assertFalse(status.isCanceled());

        assertEquals(ERROR_MESSAGE, this.logCapture.getMessage(0));
    }

    @Test
    void jobFinishedWhenInterrupted()
    {
        this.job.cancelable = true;
        this.job.error = new InterruptedException();

        DefaultJobStatus<DefaultRequest> status = run();

        assertSame(State.FINISHED, status.getState());
        assertSame(this.job.error, status.getError());
        assertTrue(status.isCanceled());

        assertEquals(ERROR_MESSAGE, this.logCapture.getMessage(0));
    }

    @Test
    void jobFinishedWhenInterruptedAndNotCancelable()
    {
        this.job.error = new InterruptedException();

        DefaultJobStatus<DefaultRequest> status = run();

        assertSame(State.FINISHED, status.getState());
        assertSame(this.job.error, status.getError());
        // Only cancelable jobs are marked as canceled, the error is the marker for the others
        assertFalse(status.isCanceled());

        assertEquals(ERROR_MESSAGE, this.logCapture.getMessage(0));
    }

    @Test
    void jobFinishedWhenInterruptedCause()
    {
        this.job.cancelable = true;
        this.job.error = new RuntimeException("wrapped", new InterruptedException());

        DefaultJobStatus<DefaultRequest> status = run();

        assertSame(this.job.error, status.getError());
        assertTrue(status.isCanceled());

        assertEquals(ERROR_MESSAGE, this.logCapture.getMessage(0));
    }

    @Test
    void jobFinishedWhenClosedByInterrupt()
    {
        this.job.cancelable = true;
        this.job.error = new ClosedByInterruptException();

        DefaultJobStatus<DefaultRequest> status = run();

        assertSame(this.job.error, status.getError());
        assertTrue(status.isCanceled());

        assertEquals(ERROR_MESSAGE, this.logCapture.getMessage(0));
    }

    @Test
    void jobFinishedWhenFailedAndThreadInterrupted()
    {
        // The interrupt flag alone is not a proof that the failure is caused by the interruption
        this.job.cancelable = true;
        this.job.error = new IOException("failed");
        this.job.interruptThread = true;

        DefaultJobStatus<DefaultRequest> status = run();

        assertSame(this.job.error, status.getError());
        assertFalse(status.isCanceled());

        assertEquals(ERROR_MESSAGE, this.logCapture.getMessage(0));
    }

    @Test
    void abortWhenNotStarted() throws Exception
    {
        DefaultJobStatus<DefaultRequest> status = initialize("id");
        status.setCancelable(true);
        Exception error = new Exception("dropped");

        this.job.abort(error);

        assertSame(State.FINISHED, status.getState());
        assertSame(error, status.getError());
        assertTrue(status.isCanceled());
        assertNotNull(status.getStartDate());
        assertEquals(status.getStartDate(), status.getEndDate());
        // join() returns
        assertTrue(this.job.join(1, TimeUnit.SECONDS));
        // The status is stored synchronously
        verify(this.store).store(status);
    }

    @Test
    void abortWhenNotCancelable()
    {
        DefaultJobStatus<DefaultRequest> status = initialize(null);

        this.job.abort(new Exception("dropped"));

        assertSame(State.FINISHED, status.getState());
        assertFalse(status.isCanceled());
        // No identifier, nothing to store
        verify(this.store, never()).store(status);
    }

    @Test
    void abortWhenStarted()
    {
        DefaultJobStatus<DefaultRequest> status = run();

        this.job.abort(new Exception("dropped"));

        // The job already finished normally: nothing changed
        assertSame(State.FINISHED, status.getState());
        assertNull(status.getError());
        assertFalse(status.isCanceled());
    }

    @Test
    void jobFinishedWhenCanceledAndInterrupted()
    {
        this.job.cancelable = true;
        this.job.cancel = true;
        this.job.error = new InterruptedException();

        DefaultJobStatus<DefaultRequest> status = run();

        assertSame(State.FINISHED, status.getState());
        assertSame(this.job.error, status.getError());
        assertTrue(status.isCanceled());

        assertEquals(ERROR_MESSAGE, this.logCapture.getMessage(0));
    }
}
