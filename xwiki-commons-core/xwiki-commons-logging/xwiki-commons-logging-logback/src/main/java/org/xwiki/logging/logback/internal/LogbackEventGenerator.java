package org.xwiki.logging.logback.internal;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
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

@Component
@Singleton
public class LogbackEventGenerator extends AppenderBase<ILoggingEvent> implements EventListener, Initializable
{
    @Inject
    private ObservationManager observation;

    @Inject
    private Logger logger;

    @Override
    public List<Event> getEvents()
    {
        return Collections.emptyList();
    }
    
    @Override
    public void initialize() throws InitializationException
    {
        // Register appender
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        setContext(rootLogger.getLoggerContext());
        rootLogger.addAppender(this);   
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        // do nothing
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
            LogLevel logLevel = LogLevel.valueOf(event.getLevel().toString());

            LogEvent logevent = new LogEvent(logLevel, event.getMessage(), event.getArgumentArray(), throwable);

            this.observation.notify(logevent, event.getLoggerName(), null);
        } catch (IllegalArgumentException e) {
            this.logger.debug("Unsupported log level {0}", event.getLevel());
        }
    }
}
