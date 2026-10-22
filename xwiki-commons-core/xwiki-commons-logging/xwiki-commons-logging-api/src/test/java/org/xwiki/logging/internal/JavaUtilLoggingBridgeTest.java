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
package org.xwiki.logging.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link JavaUtilLoggingBridge}.
 *
 * @version $Id$
 */
class JavaUtilLoggingBridgeTest
{
    private static final Logger JUL_LOGGER = Logger.getLogger("org.xwiki.test.jul");

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    private final JavaUtilLoggingBridge bridge = new JavaUtilLoggingBridge();

    private Handler[] originalHandlers;

    private RecordingHandler recordingHandler;

    /**
     * A java.util.logging handler remembering the messages it receives (instead of writing them to the console).
     */
    private static class RecordingHandler extends Handler
    {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord logRecord)
        {
            this.messages.add(logRecord.getMessage());
        }

        @Override
        public void flush()
        {
            // Nothing to flush
        }

        @Override
        public void close()
        {
            // Nothing to close
        }
    }

    @BeforeEach
    void beforeEach()
    {
        // Replace the handlers of the root logger by a handler which does not write to the console
        Logger rootLogger = getRootLogger();
        this.originalHandlers = rootLogger.getHandlers();
        for (Handler handler : this.originalHandlers) {
            rootLogger.removeHandler(handler);
        }
        this.recordingHandler = new RecordingHandler();
        rootLogger.addHandler(this.recordingHandler);
    }

    @AfterEach
    void afterEach()
    {
        this.bridge.uninstall();

        Logger rootLogger = getRootLogger();
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        for (Handler handler : this.originalHandlers) {
            rootLogger.addHandler(handler);
        }
    }

    private Logger getRootLogger()
    {
        return LogManager.getLogManager().getLogger("");
    }

    @Test
    void installAndUninstall()
    {
        this.bridge.install();

        // The logs go to SLF4J, and not to the previous handlers anymore
        Handler[] handlers = getRootLogger().getHandlers();
        assertEquals(1, handlers.length);
        assertInstanceOf(SLF4JBridgeHandler.class, handlers[0]);
        JUL_LOGGER.warning("bridged message");
        assertEquals("bridged message", this.logCapture.getMessage(0));
        assertTrue(this.recordingHandler.messages.isEmpty());

        this.bridge.uninstall();

        // The previous handlers are back
        assertArrayEquals(new Handler[] {this.recordingHandler}, getRootLogger().getHandlers());
        JUL_LOGGER.warning("not bridged message");
        assertEquals(List.of("not bridged message"), this.recordingHandler.messages);
        assertEquals(1, this.logCapture.size());
    }

    @Test
    void installWhenAlreadyInstalled()
    {
        SLF4JBridgeHandler.install();

        try {
            this.bridge.install();

            // The handlers are left untouched
            assertEquals(2, getRootLogger().getHandlers().length);

            // And they are still the same once the bridge is uninstalled, since it did not install anything
            this.bridge.uninstall();
            assertTrue(SLF4JBridgeHandler.isInstalled());
            assertEquals(2, getRootLogger().getHandlers().length);
        } finally {
            SLF4JBridgeHandler.uninstall();
        }

        assertFalse(SLF4JBridgeHandler.isInstalled());
    }
}
