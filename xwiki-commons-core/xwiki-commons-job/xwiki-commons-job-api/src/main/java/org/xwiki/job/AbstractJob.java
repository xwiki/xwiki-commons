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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.context.concurrent.ContextStoreManager;
import org.xwiki.job.event.JobFinishedEvent;
import org.xwiki.job.event.JobFinishingEvent;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.job.event.status.JobProgressManager;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.job.event.status.JobStatus.State;
import org.xwiki.job.internal.JobUtils;
import org.xwiki.logging.LoggerManager;
import org.xwiki.logging.marker.BeginTranslationMarker;
import org.xwiki.logging.marker.EndTranslationMarker;
import org.xwiki.logging.marker.TranslationMarker;
import org.xwiki.observation.ObservationManager;

/**
 * Base class for {@link Job} implementations.
 *
 * @param <R> the request type associated to the job
 * @version $Id$
 * @since 7.4M1
 */
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public abstract class AbstractJob<R extends Request, S extends JobStatus> implements Job
{
    private static final BeginTranslationMarker LOG_BEGIN = new BeginTranslationMarker("job.log.begin");

    private static final BeginTranslationMarker LOG_BEGIN_ID = new BeginTranslationMarker("job.log.beginWithId");

    private static final EndTranslationMarker LOG_END = new EndTranslationMarker("job.log.end");

    private static final EndTranslationMarker LOG_END_ID = new EndTranslationMarker("job.log.endWithId");

    private static final TranslationMarker LOG_EXCEPTION = new TranslationMarker("job.log.exception");

    private static final TranslationMarker LOG_STATUS_STORE_FAILED =
        new TranslationMarker("job.log.status.store.failed");

    /**
     * Component manager.
     */
    @Inject
    protected ComponentManager componentManager;

    /**
     * Used to send extensions installation and upgrade related events.
     */
    @Inject
    protected ObservationManager observationManager;

    /**
     * Used to isolate job related log.
     */
    @Inject
    protected LoggerManager loggerManager;

    @Inject
    protected ContextStoreManager contextStore;

    /**
     * Used to store the results of the jobs execution.
     */
    @Inject
    protected JobStatusStore store;

    /**
     * The logger to log.
     */
    @Inject
    protected Logger logger;

    /**
     * Used to set the current context.
     */
    @Inject
    protected JobContext jobContext;

    @Inject
    protected JobProgressManager progressManager;

    /**
     * The job request.
     */
    protected R request;

    /**
     * @see #getStatus()
     */
    protected S status;

    /**
     * Main lock guarding all access.
     */
    protected final ReentrantLock lock = new ReentrantLock();

    /**
     * Condition to wait for finished state.
     */
    protected final Condition finishedCondition = this.lock.newCondition();

    protected boolean initExecutionContext = true;

    /**
     * Used to get the Execution Context.
     */
    @Inject
    private Execution jobExecution;

    /**
     * Used to create a new Execution Context from scratch.
     */
    @Inject
    private Provider<ExecutionContextManager> executionContextManagerProvider;

    @Override
    public R getRequest()
    {
        return this.request;
    }

    @Override
    public S getStatus()
    {
        return this.status;
    }

    @Override
    public void initialize(Request request)
    {
        this.request = castRequest(request);
        this.status = createNewStatus(this.request);

        // Create a filesystem log tail if the status is supposed to be serialized (to avoid creating too much files)
        if (this.status instanceof AbstractJobStatus && JobUtils.isSerializable(this.status)) {
            ((AbstractJobStatus) this.status).setLoggerTail(this.store.createLoggerTail(request.getId(), false));
        }
    }

    @Override
    public void run()
    {
        if (this.initExecutionContext) {
            ExecutionContext previousContext = null;

            // Get context to restore
            Map<String, Serializable> storedContext = getRequest().getContext();

            // Initialize a new context only if there is not already one
            // But if there is a context in the job request then we want to replace the current one with a fresh one
            ExecutionContext context;
            if (storedContext != null || this.jobExecution.getContext() == null) {
                // Create a clean Execution Context
                context = new ExecutionContext();
            } else {
                context = null;
            }

            try {
                if (context != null) {
                    // Remember previous context
                    previousContext = this.jobExecution.getContext();

                    try {
                        this.executionContextManagerProvider.get().initialize(context);
                    } catch (ExecutionContextException e) {
                        throw new RuntimeException("Failed to initialize Job [" + this + "] execution context", e);
                    }

                    // Restore stored context
                    if (MapUtils.isNotEmpty(storedContext)) {
                        try {
                            this.contextStore.restore(storedContext);
                        } catch (ComponentLookupException e) {
                            throw new RuntimeException("Failed to restore context requested for the job [" + this + "]",
                                e);
                        }
                    }
                }

                runInContext();
            } finally {
                if (context != null) {
                    // Get rid of job context
                    this.jobExecution.removeContext();

                    // Restore previous context
                    if (previousContext != null) {
                        this.jobExecution.setContext(previousContext);
                    }
                }
            }
        } else {
            runInContext();
        }
    }

    protected void runInContext()
    {
        Throwable error = null;
        try {
            jobStarting();

            runInternal();
        } catch (Throwable t) {
            this.logger.error(LOG_EXCEPTION, "Exception thrown during job execution", t);
            error = t;
        } finally {
            jobFinished(error);
        }
    }

    /**
     * Called when the job is starting.
     */
    protected void jobStarting()
    {
        this.jobContext.pushCurrentJob(this);

        this.observationManager.notify(new JobStartedEvent(getRequest().getId(), getType(), this.request), this);

        if (this.status instanceof AbstractJobStatus) {
            ((AbstractJobStatus<R>) this.status).setStartDate(new Date());
            ((AbstractJobStatus<R>) this.status).setState(JobStatus.State.RUNNING);

            ((AbstractJobStatus) this.status).startListening();
        }

        if (getRequest().isVerbose()) {
            if (getStatus().getRequest().getId() != null) {
                this.logger.info(LOG_BEGIN_ID, "Starting job of type [{}] with identifier [{}]", getType(),
                    getStatus().getRequest().getId());
            } else {
                this.logger.info(LOG_BEGIN, "Starting job of type [{}]", getType());
            }
        }
    }

    /**
     * Called when the job is done.
     *
     * @param error the exception throw during execution of the job
     */
    protected void jobFinished(Throwable error)
    {
        this.lock.lock();

        try {
            if (this.status instanceof AbstractJobStatus) {
                // Store error
                ((AbstractJobStatus) this.status).setError(error);
            }

            // Give a chance to any listener to do custom action associated to the job
            this.observationManager.notify(new JobFinishingEvent(getRequest().getId(), getType(), this.request), this,
                error);

            if (getRequest().isVerbose()) {
                if (getStatus().getRequest().getId() != null) {
                    this.logger.info(LOG_END_ID, "Finished job of type [{}] with identifier [{}]", getType(),
                        getStatus().getRequest().getId());
                } else {
                    this.logger.info(LOG_END, "Finished job of type [{}]", getType());
                }
            }

            if (this.status instanceof AbstractJobStatus) {
                // Indicate when the job ended
                ((AbstractJobStatus) this.status).setEndDate(new Date());

                // Stop updating job status (progress, log, etc.)
                ((AbstractJobStatus) this.status).stopListening();

                // Update job state
                ((AbstractJobStatus) this.status).setState(JobStatus.State.FINISHED);
            }

            // Release threads waiting for job being done
            this.finishedCondition.signalAll();

            // Remove the job from the current jobs context
            this.jobContext.popCurrentJob();

            // Store the job status
            try {
                if (this.request.getId() != null) {
                    this.store.storeAsync(this.status);
                }
            } catch (Throwable t) {
                this.logger.warn(LOG_STATUS_STORE_FAILED, "Failed to store job status [{}]", this.status, t);
            }
        } finally {
            this.lock.unlock();

            // Notify listener that job is fully finished
            this.observationManager.notify(new JobFinishedEvent(getRequest().getId(), getType(), this.request), this,
                error);
        }
    }

    /**
     * Should be overridden if R is not Request.
     *
     * @param request the request
     * @return the request in the proper extended type
     */
    @SuppressWarnings("unchecked")
    protected R castRequest(Request request)
    {
        // Get the type of the request
        ParameterizedType genericType = (ParameterizedType) ReflectionUtils.resolveType(AbstractJob.class, getClass());
        Class<R> propertiesType = ReflectionUtils.getTypeClass(genericType.getActualTypeArguments()[0]);

        // Make sure the passed request is compatible with the expected type, if not try to convert it
        if (!propertiesType.isInstance(request)) {
            try {
                Constructor<R> constructor = propertiesType.getConstructor(Request.class);

                return constructor.newInstance(request);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert the input request [" + request + "] into the expected ["
                    + propertiesType + "] type.");
            }
        }

        return (R) request;
    }

    /**
     * Create a new standard {@link JobStatus}. Jobs requiring a custom {@link JobStatus} should overwrite this method.
     * 
     * @param request contains information related to the job to execute
     * @return the status of the job
     */
    protected S createNewStatus(R request)
    {
        Job currentJob = this.jobContext.getCurrentJob();
        JobStatus currentJobStatus = currentJob != null ? currentJob.getStatus() : null;
        return (S) new DefaultJobStatus<>(getType(), request, currentJobStatus, this.observationManager,
            this.loggerManager);
    }

    /**
     * Should be implemented by {@link Job} implementations.
     *
     * @throws Exception errors during job execution
     */
    protected abstract void runInternal() throws Exception;

    @Override
    public void join() throws InterruptedException
    {
        this.lock.lockInterruptibly();

        try {
            if (getStatus() == null || getStatus().getState() != State.FINISHED) {
                this.finishedCondition.await();
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean join(long time, TimeUnit unit) throws InterruptedException
    {
        this.lock.lockInterruptibly();

        try {
            if (getStatus().getState() != State.FINISHED) {
                return this.finishedCondition.await(time, unit);
            }
        } finally {
            this.lock.unlock();
        }

        return true;
    }

    // Deprecated

    @Override
    @Deprecated
    public void start(Request request)
    {
        initialize(request);
        run();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(super.toString());

        if (getRequest() != null) {
            builder.append('(');
            builder.append(getRequest().getId());
            builder.append(')');
        }

        return builder.toString();
    }
}
