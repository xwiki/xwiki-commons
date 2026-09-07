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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.xwiki.logging.Logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.DynamicConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link XWikiThrowableConverter}.
 *
 * @version $Id$
 */
class XWikiThrowableConverterTest
{
    private static final String CONVERSION_WORD = "xwikiEx";

    private LoggerContext loggerContext;

    private XWikiThrowableConverter converter;

    @BeforeEach
    void beforeEach()
    {
        this.loggerContext = new LoggerContext();

        this.converter = new XWikiThrowableConverter();
        this.converter.setContext(this.loggerContext);
        this.converter.start();
    }

    @AfterEach
    void afterEach()
    {
        this.converter.stop();
        this.loggerContext.stop();
    }

    private LoggingEvent createEvent(Level level, Throwable throwable)
    {
        return new LoggingEvent(getClass().getName(), this.loggerContext.getLogger(getClass()), level, "some message",
            throwable, null);
    }

    private ILoggingEvent createEvent(Level level, Throwable throwable, Marker... markers)
    {
        LoggingEvent event = createEvent(level, throwable);
        for (Marker marker : markers) {
            event.addMarker(marker);
        }

        return event;
    }

    private ILoggingEvent createEvent(String loggerName, Level level, Throwable throwable)
    {
        return new LoggingEvent(getClass().getName(), this.loggerContext.getLogger(loggerName), level, "some message",
            throwable, null);
    }

    /**
     * Restart the converter so that it takes the options and the context properties set by the test into account.
     *
     * @param options the options of the conversion word
     */
    private void restartConverter(String... options)
    {
        this.converter.stop();
        this.converter.setOptionList(List.of(options));
        this.converter.start();
    }

    private Throwable createNestedThrowable()
    {
        return new RuntimeException("failed to save the document",
            new IllegalStateException("connection pool is exhausted"));
    }

    @Test
    void convertWarningPrintsRootCauseMessageOnly()
    {
        String result = this.converter.convert(createEvent(Level.WARN, createNestedThrowable()));

        assertEquals(" Root cause: [java.lang.IllegalStateException: connection pool is exhausted]", result);
    }

    @Test
    void convertWarningWithoutRootCauseMessagePrintsRootCauseClassOnly()
    {
        String result =
            this.converter.convert(createEvent(Level.WARN, new RuntimeException(new NullPointerException())));

        assertEquals(" Root cause: [java.lang.NullPointerException]", result);
    }

    @Test
    void convertWarningWithoutCausePrintsTheThrowableItself()
    {
        String result = this.converter.convert(createEvent(Level.WARN, new RuntimeException("no cause")));

        assertEquals(" Root cause: [java.lang.RuntimeException: no cause]", result);
    }

    @Test
    void convertErrorPrintsFullStackTrace()
    {
        String result = this.converter.convert(createEvent(Level.ERROR, createNestedThrowable()));

        assertTrue(result.contains("java.lang.RuntimeException: failed to save the document"),
            () -> "Unexpected output: " + result);
        assertTrue(result.contains("Caused by: java.lang.IllegalStateException: connection pool is exhausted"),
            () -> "Unexpected output: " + result);
        assertTrue(result.contains("\tat " + getClass().getName()), () -> "Unexpected output: " + result);
    }

    @Test
    void convertDebugPrintsFullStackTrace()
    {
        String result = this.converter.convert(createEvent(Level.DEBUG, createNestedThrowable()));

        assertTrue(result.contains("\tat " + getClass().getName()), () -> "Unexpected output: " + result);
    }

    @Test
    void convertWarningPrintsFullStackTraceWhenPropertyIsEnabled()
    {
        this.loggerContext.putProperty(XWikiThrowableConverter.PROPERTY_WARN_STACKTRACE, "true");

        String result = this.converter.convert(createEvent(Level.WARN, createNestedThrowable()));

        assertTrue(result.contains("\tat " + getClass().getName()), () -> "Unexpected output: " + result);
    }

    @Test
    void convertWarningPrintsFullStackTraceWhenTheStackTraceMarkerIsUsed()
    {
        String result = this.converter
            .convert(createEvent(Level.WARN, createNestedThrowable(), Logger.STACKTRACE_MARKER));

        assertTrue(result.contains("\tat " + getClass().getName()), () -> "Unexpected output: " + result);
    }

    @Test
    void convertWarningPrintsFullStackTraceWhenAMarkerContainsTheStackTraceMarker()
    {
        Marker container = MarkerFactory.getDetachedMarker("xwiki.markers");
        container.add(MarkerFactory.getMarker("some.other.marker"));
        container.add(Logger.STACKTRACE_MARKER);

        String result = this.converter.convert(createEvent(Level.WARN, createNestedThrowable(), container));

        assertTrue(result.contains("\tat " + getClass().getName()), () -> "Unexpected output: " + result);
    }

    @Test
    void convertWarningPrintsRootCauseMessageOnlyWithAnUnrelatedMarker()
    {
        String result = this.converter.convert(
            createEvent(Level.WARN, createNestedThrowable(), MarkerFactory.getMarker("some.other.marker")));

        assertEquals(" Root cause: [java.lang.IllegalStateException: connection pool is exhausted]", result);
    }

    @Test
    void convertWarningPrintsFullStackTraceForALoggerListedInTheOptions()
    {
        restartConverter("full=org.xwiki.contrib");

        String result =
            this.converter.convert(createEvent("org.xwiki.contrib.myapp.MyClass", Level.WARN, createNestedThrowable()));

        assertTrue(result.contains("\tat " + getClass().getName()), () -> "Unexpected output: " + result);
    }

    @Test
    void convertWarningPrintsFullStackTraceForALoggerListedInTheProperty()
    {
        this.loggerContext.putProperty(XWikiThrowableConverter.PROPERTY_WARN_STACKTRACE_LOGGERS,
            " com.acme , org.xwiki.contrib ");
        restartConverter();

        String result = this.converter.convert(createEvent("com.acme.MyClass", Level.WARN, createNestedThrowable()));

        assertTrue(result.contains("\tat " + getClass().getName()), () -> "Unexpected output: " + result);
    }

    @Test
    void convertWarningPrintsRootCauseMessageOnlyForALoggerJustSharingAPrefix()
    {
        restartConverter("full=org.xwiki.contrib");

        String result =
            this.converter.convert(createEvent("org.xwiki.contributions.MyClass", Level.WARN, createNestedThrowable()));

        assertEquals(" Root cause: [java.lang.IllegalStateException: connection pool is exhausted]", result);
    }

    @Test
    void convertPassesTheRemainingOptionsToTheStandardConverter()
    {
        Throwable throwable = createNestedThrowable();
        long fullLines = countStackTraceLines(this.converter.convert(createEvent(Level.ERROR, throwable)));

        // "1" asks the standard converter to print a single stack trace line per throwable, and it is not an option of
        // this converter, so it must have reached the standard one.
        restartConverter("full=org.xwiki.contrib", "1");

        String result = this.converter.convert(createEvent(Level.ERROR, throwable));

        assertTrue(countStackTraceLines(result) < fullLines, () -> "Unexpected output: " + result);
    }

    private long countStackTraceLines(String rendering)
    {
        return rendering.lines().filter(line -> line.startsWith("\tat ")).count();
    }

    @Test
    void convertWithoutThrowableReturnsEmptyString()
    {
        assertEquals("", this.converter.convert(createEvent(Level.WARN, null)));
    }

    /**
     * Verify that the converter can be declared in {@code logback.xml} through a {@code <conversionRule>} element, which
     * registers it in the very map used below, and that it then replaces the stack trace converter Logback appends by
     * default.
     */
    @Test
    void convertInsidePatternLayoutDeclaredAsAConversionRule()
    {
        Map<String, Supplier<DynamicConverter>> ruleRegistry = new HashMap<>();
        ruleRegistry.put(CONVERSION_WORD, XWikiThrowableConverter::new);
        this.loggerContext.putObject(CoreConstants.PATTERN_RULE_REGISTRY_FOR_SUPPLIERS, ruleRegistry);

        PatternLayout layout = new PatternLayout();
        layout.setContext(this.loggerContext);
        layout.setPattern("%-5p %c{1} - %m%" + CONVERSION_WORD);
        layout.start();

        Throwable throwable = createNestedThrowable();

        try {
            String warning = layout.doLayout(createEvent(Level.WARN, throwable));

            assertEquals("WARN  o.x.l.l.i.XWikiThrowableConverterTest - some message Root cause: "
                + "[java.lang.IllegalStateException: connection pool is exhausted]", warning);
            assertFalse(warning.contains("\tat "), () -> "Unexpected output: " + warning);

            String error = layout.doLayout(createEvent(Level.ERROR, throwable));

            assertTrue(error.contains("\tat " + getClass().getName()), () -> "Unexpected output: " + error);
        } finally {
            layout.stop();
        }
    }
}
