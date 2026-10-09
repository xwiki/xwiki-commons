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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Marker;
import org.xwiki.logging.Logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.Context;

/**
 * Renders the exception associated with a log event, printing only the message of its root cause when the event is a
 * warning, and the full stack trace otherwise.
 * <p>
 * A stack trace is what an administrator needs to fix a broken XWiki, but a warning by definition describes a situation
 * XWiki has recovered from. Printing a full stack trace for each of them fills the console with noise in which the
 * errors that do matter become hard to spot. The exception is still fully part of the log event, so the wiki UI (which
 * displays the root cause and folds the stack trace) and any log collector using its own encoder keep seeing all of it:
 * only the console rendering is shortened.
 * </p>
 * <p>
 * A warning keeps its full stack trace when any of the following holds, so that the shortening never hides a stack
 * trace that somebody deliberately asked for:
 * </p>
 * <ul>
 * <li>the log call carries the {@link Logger#STACKTRACE_MARKER} marker, for the warnings whose exception exists only to
 * record the call site and would say nothing at all once reduced to its root cause;</li>
 * <li>the {@link #PROPERTY_WARN_STACKTRACE} property is set to {@code true} in the Logback context, which restores
 * stack traces for every warning at once;</li>
 * <li>the logger of the event is one of the loggers listed by the {@link #PROPERTY_WARN_STACKTRACE_LOGGERS} property or
 * by a {@code full=} option of the conversion word, which lets an administrator keep full stack traces for the code
 * they are debugging without having to declare a second appender.</li>
 * </ul>
 * <p>
 * To use it, declare the conversion word in {@code logback.xml} and reference it in the pattern of the appenders that
 * should use it:
 * </p>
 *
 * <pre>
 * {@code
 * <property name="xwiki.logging.warn.stacktrace.loggers" value="org.xwiki.contrib"/>
 * <conversionRule conversionWord="xwikiEx"
 *   class="org.xwiki.logging.logback.internal.XWikiThrowableConverter"/>
 * ...
 * <pattern>%d [%t] %-5p %-30.30c{2} - %m%xwikiEx%n</pattern>
 * }
 * </pre>
 * <p>
 * Any other option of the conversion word is passed to the standard converter, so {@code %xwikiEx{short}} or
 * {@code %xwikiEx{2}} keep the meaning they have with {@code %ex}. Since Logback splits the option list on commas, each
 * logger of a {@code full=} option needs its own option: {@code %xwikiEx{full=org.xwiki.contrib,full=com.acme}}.
 * </p>
 * <p>
 * Note that a pattern which contains no throwable conversion word at all gets an
 * {@link ExtendedThrowableProxyConverter} appended automatically by Logback, which is why the standard XWiki pattern
 * prints full stack traces today.
 * </p>
 *
 * @version $Id$
 * @since 18.7.0RC1
 */
public class XWikiThrowableConverter extends ThrowableHandlingConverter
{
    /**
     * Name of the Logback context property which, when set to {@code true}, makes warnings be rendered with their full
     * stack trace as any other level.
     */
    public static final String PROPERTY_WARN_STACKTRACE = "xwiki.logging.warn.stacktrace";

    /**
     * Name of the Logback context property holding the comma-separated list of the loggers whose warnings are rendered
     * with their full stack trace, each entry matching the logger of that exact name and all the loggers below it.
     */
    public static final String PROPERTY_WARN_STACKTRACE_LOGGERS = "xwiki.logging.warn.stacktrace.loggers";

    private static final String ROOT_CAUSE_PREFIX = " Root cause: [";

    private static final String ROOT_CAUSE_SUFFIX = "]";

    private static final String CLASS_MESSAGE_SEPARATOR = ": ";

    private static final String OPTION_FULL = "full=";

    private static final String LOGGER_SEPARATOR = ",";

    /**
     * The standard Logback converter, to which the rendering of full stack traces is delegated.
     */
    private ThrowableHandlingConverter stackTraceConverter;

    /**
     * The loggers whose warnings keep a full stack trace.
     */
    private List<String> fullStackTraceLoggers;

    @Override
    public void start()
    {
        List<String> delegateOptions = new ArrayList<>();
        this.fullStackTraceLoggers = new ArrayList<>();
        parseOptions(delegateOptions, this.fullStackTraceLoggers);
        addLoggers(getContextProperty(PROPERTY_WARN_STACKTRACE_LOGGERS), this.fullStackTraceLoggers);

        // Match the converter Logback itself would have used, so that the output of the levels we don't shorten is
        // exactly the one obtained without this converter.
        this.stackTraceConverter =
            isPackagingDataEnabled() ? new ExtendedThrowableProxyConverter() : new ThrowableProxyConverter();
        // Pass the options we don't consume along so that %xwikiEx{short}, %xwikiEx{2} or an evaluator keep working.
        this.stackTraceConverter.setOptionList(delegateOptions);
        this.stackTraceConverter.setContext(getContext());
        this.stackTraceConverter.start();

        super.start();
    }

    /**
     * Split the options of the conversion word between the ones naming a logger to leave untouched and the ones meant
     * for the standard converter.
     *
     * @param delegateOptions the list to fill with the options to pass to the standard converter
     * @param loggers the list to fill with the loggers whose warnings keep a full stack trace
     */
    private void parseOptions(List<String> delegateOptions, List<String> loggers)
    {
        List<String> options = getOptionList();
        if (options != null) {
            for (String option : options) {
                if (option.startsWith(OPTION_FULL)) {
                    addLoggers(option.substring(OPTION_FULL.length()), loggers);
                } else {
                    delegateOptions.add(option);
                }
            }
        }
    }

    /**
     * @param value a comma-separated list of logger names, possibly null or empty
     * @param loggers the list to fill with the logger names found in the passed value
     */
    private void addLoggers(String value, List<String> loggers)
    {
        if (value != null) {
            for (String logger : value.split(LOGGER_SEPARATOR)) {
                String trimmed = logger.trim();
                if (!trimmed.isEmpty()) {
                    loggers.add(trimmed);
                }
            }
        }
    }

    @Override
    public void stop()
    {
        if (this.stackTraceConverter != null) {
            this.stackTraceConverter.stop();
            this.stackTraceConverter = null;
        }

        super.stop();
    }

    @Override
    public String convert(ILoggingEvent event)
    {
        IThrowableProxy throwable = event.getThrowableProxy();

        if (throwable == null) {
            return "";
        }

        if (isRootCauseOnly(event)) {
            return getRootCauseMessage(throwable);
        }

        return this.stackTraceConverter.convert(event);
    }

    /**
     * @param event the log event to render
     * @return true if only the message of the root cause of the exception should be printed
     */
    private boolean isRootCauseOnly(ILoggingEvent event)
    {
        return Level.WARN.equals(event.getLevel()) && !isWarnStackTraceEnabled() && !isStackTraceRequested(event)
            && !isFullStackTraceLogger(event.getLoggerName());
    }

    /**
     * @return true if warnings must be printed with a full stack trace
     */
    private boolean isWarnStackTraceEnabled()
    {
        return Boolean.parseBoolean(getContextProperty(PROPERTY_WARN_STACKTRACE));
    }

    /**
     * @param event the log event to render
     * @return true if the log call asked for its stack trace to be printed whatever the configuration
     */
    private boolean isStackTraceRequested(ILoggingEvent event)
    {
        List<Marker> markers = event.getMarkerList();
        if (markers != null) {
            for (Marker marker : markers) {
                // Match by name and not by instance so that a marker containing the XWiki one is recognized too.
                if (marker.contains(Logger.STACKTRACE_MARKER.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param loggerName the name of the logger of the log event
     * @return true if that logger has been configured to keep full stack traces for its warnings
     */
    private boolean isFullStackTraceLogger(String loggerName)
    {
        if (loggerName != null) {
            for (String logger : this.fullStackTraceLoggers) {
                if (loggerName.equals(logger) || loggerName.startsWith(logger + '.')) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param name the name of the property
     * @return the value of that property in the Logback context, or null when there is no context or no such property
     */
    private String getContextProperty(String name)
    {
        Context context = getContext();

        return context != null ? context.getProperty(name) : null;
    }

    /**
     * @param throwable the exception of the log event
     * @return the class name and message of the deepest cause of the passed exception
     */
    private String getRootCauseMessage(IThrowableProxy throwable)
    {
        IThrowableProxy rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        StringBuilder builder = new StringBuilder(ROOT_CAUSE_PREFIX);
        builder.append(rootCause.getClassName());
        if (rootCause.getMessage() != null) {
            builder.append(CLASS_MESSAGE_SEPARATOR);
            builder.append(rootCause.getMessage());
        }
        builder.append(ROOT_CAUSE_SUFFIX);

        return builder.toString();
    }

    /**
     * @return true if Logback is configured to compute packaging data
     */
    private boolean isPackagingDataEnabled()
    {
        return getContext() instanceof LoggerContext loggerContext && loggerContext.isPackagingDataEnabled();
    }
}
