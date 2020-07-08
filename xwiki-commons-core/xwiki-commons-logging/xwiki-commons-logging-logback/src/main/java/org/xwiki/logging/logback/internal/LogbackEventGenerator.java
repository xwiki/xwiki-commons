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
package org.xwiki.logging.logback.internal;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LogUtils;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * Bridge converting log to Observation Events.
 * <p>
 * Note that this class is implemented as an Event Listener only because we needed a way for this component to be
 * initialized early when the system starts and the Observation Manager Component is the first Component loaded in the
 * system and in its own initialization it initializes all Event Listeners... The reason we want this component
 * initialized early is because it adds itself as a Logback Appender in its initialization and thus by having it done
 * early any other component wishing to listen to logs will be able to do so and not "loose" events (there's still a
 * possibility that some logs will not be seen if some Event Listeners do logging in their initialization and it happens
 * that they're initialized before this component...).
 * </p>
 *
 * @version $Id$
 * @since 3.2M1
 */
@Component
@Named("LogbackEventGenerator")
@Singleton
public class LogbackEventGenerator extends UnsynchronizedAppenderBase<ILoggingEvent>
    implements EventListener, Initializable, Disposable
{
    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * The component manager.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Logback utilities.
     */
    private LogbackUtils utils = new LogbackUtils();

    @Override
    public String getName()
    {
        return "LogbackEventGenerator";
    }

    @Override
    public List<Event> getEvents()
    {
        // We don't want to listen to any event. We just want to benefit from the Observation Manager's initialization
        // (see the class documentation above).
        return Collections.emptyList();
    }

    @Override
    public void initialize() throws InitializationException
    {
        // Register appender (see the class documentation above).
        ch.qos.logback.classic.Logger rootLogger = getRootLogger();

        if (rootLogger != null) {
            setContext(rootLogger.getLoggerContext());
            rootLogger.addAppender(this);
            start();
        } else {
            this.logger
                .warn("Could not find any Logback root logger." + " The logging module won't be able to catch logs.");
        }
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        // Do nothing, we don't listen to any event. We just want to benefit from the Observation Manager's
        // initialization (see the class documentation above).
    }

    /**
     * @return the ObservationManager implementation
     * @throws ComponentLookupException failed to get ObservationManager implementation
     */
    private ObservationManager getObservationManager() throws ComponentLookupException
    {
        return this.componentManager.getInstance(ObservationManager.class);
    }

    @Override
    protected void append(ILoggingEvent event)
    {
        Throwable throwable = null;
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy instanceof ThrowableProxy) {
            throwable = ((ThrowableProxy) throwableProxy).getThrowable();
        }

        try {
            LogLevel logLevel = this.utils.toLogLevel(event.getLevel());

            LogEvent logevent = LogUtils.newLogEvent(event.getMarker(), logLevel, event.getMessage(),
                event.getArgumentArray(), throwable, event.getTimeStamp());

            getObservationManager().notify(logevent, event.getLoggerName(), null);
        } catch (IllegalArgumentException e) {
            this.logger.debug("Unsupported log level [{}]", event.getLevel());
        } catch (ComponentLookupException e) {
            this.logger.error("Can't find any implementation of [{}]", ObservationManager.class.getName(), e);
        }
    }

    /**
     * @return the Logback root logger or null if Logback is not available
     */
    protected ch.qos.logback.classic.Logger getRootLogger()
    {
        return this.utils.getRootLogger();
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        stop();

        // Unregister appender
        ch.qos.logback.classic.Logger rootLogger = getRootLogger();

        if (rootLogger != null) {
            rootLogger.detachAppender(this);
        }
    }
}
