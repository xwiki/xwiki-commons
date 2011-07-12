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
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.event.LogLevel;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;

/**
 * Bridge converting log to events.
 * 
 * @version $Id$
 * @since 3.2M1
 */
@Component
@Singleton
public class LogbackEventGenerator extends AppenderBase<ILoggingEvent> implements EventListener, Initializable
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
     * {@inheritDoc}
     * 
     * @see ch.qos.logback.core.AppenderBase#getName()
     */
    @Override
    public String getName()
    {
        return "LogbackEventGenerator";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#getEvents()
     */
    @Override
    public List<Event> getEvents()
    {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    @Override
    public void initialize() throws InitializationException
    {
        // Register appender
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        setContext(rootLogger.getLoggerContext());
        rootLogger.addAppender(this);
        start();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#onEvent(org.xwiki.observation.event.Event, java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        // do nothing
    }

    /**
     * @return the ObservationManager implementation
     * @throws ComponentLookupException failed to get ObservationManager implementation
     */
    private ObservationManager getObservationManager() throws ComponentLookupException
    {
        return this.componentManager.lookup(ObservationManager.class);
    }

    /**
     * {@inheritDoc}
     * 
     * @see ch.qos.logback.core.AppenderBase#append(java.lang.Object)
     */
    @Override
    protected void append(ILoggingEvent event)
    {
        Throwable throwable = null;
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy instanceof ThrowableProxy) {
            throwable = ((ThrowableProxy) throwableProxy).getThrowable();
        }

        try {
            LogLevel logLevel = LogLevel.valueOf(event.getLevel().toString());

            LogEvent logevent = new LogEvent(logLevel, event.getMessage(), event.getArgumentArray(), throwable);

            getObservationManager().notify(logevent, event.getLoggerName(), null);
        } catch (IllegalArgumentException e) {
            this.logger.debug("Unsupported log level {0}", event.getLevel());
        } catch (ComponentLookupException e) {
            this.logger.error("Can't find any implementation of org.xwiki.observation.ObservationManager", e);
        }
    }
}
