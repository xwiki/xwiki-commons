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

package org.xwiki.test.junit5;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.test.LogLevel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Unit tests for {@link LogCaptureExtension}.
 *
 * @version $Id$
 */
public class LogCaptureExtensionTest
{
    public static class SampleTestCase
    {
        private static final Logger LOGGER = LoggerFactory.getLogger(SampleTestCase.class);

        @RegisterExtension
        LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

        @Test
        @Order(1)
        void testWhenNotCaptured()
        {
            LOGGER.warn("uncaptured warn");
        }

        @Test
        @Order(2)
        void testWhenCaptured()
        {
            LOGGER.error("captured error");
            assertEquals("captured error", logCapture.getMessage(0));
        }

        @Test
        @Order(3)
        void testWhenLoggerLevelNotCaptured()
        {
            LOGGER.info(("info ignored and not captured"));
        }
    }

    public static class SampleStaticTestCase
    {
        private static final Logger LOGGER = LoggerFactory.getLogger(SampleTestCase.class);

        @RegisterExtension
        static LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

        @BeforeAll
        static void beforeAll()
        {
            LOGGER.warn("static warn before all captured");
            LOGGER.warn("static warn before all not captured");
        }

        @Test
        @Order(1)
        void testWhenNotCaptured()
        {
            LOGGER.warn("uncaptured warn");
        }

        @Test
        @Order(2)
        void testWhenCaptured()
        {
            LOGGER.error("captured error");
            assertEquals("captured error", logCapture.getMessage(3));
        }

        @AfterAll
        static void afterAll()
        {
            assertEquals("static warn before all captured", logCapture.getMessage(0));
        }
    }

    @Test
    void captureLogsWhenNotStatic()
    {
        TestExecutionSummary summary = executeJUnit(SampleTestCase.class);

        // Verify that uncaptured logs are caught and generate an exception
        assertEquals(1, summary.getFailures().size());
        assertEquals("Following messages must be asserted: [uncaptured warn]",
            summary.getFailures().get(0).getException().getMessage());
        assertEquals(2, summary.getTestsSucceededCount());
    }

    @Test
    void captureLogsWhenStatic()
    {
        TestExecutionSummary summary = executeJUnit(SampleStaticTestCase.class);

        // Verify that uncaptured logs are caught and generate an exception
        assertEquals(2, summary.getFailures().size());
        assertEquals("Following messages must be asserted: [uncaptured warn]",
            summary.getFailures().get(0).getException().getMessage());
        assertEquals("Following messages must be asserted: [static warn before all not captured]",
            summary.getFailures().get(1).getException().getMessage());
        assertEquals(1, summary.getTestsSucceededCount());
    }

    private TestExecutionSummary executeJUnit(Class<?> testClass)
    {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(testClass))
            .build();
        // Do not auto load TestExecutionListener since that would load our
        // FailingTestDebuggingTestExecutionListener which would print things in the console since we test test
        // failures in this test class, and in turn it would fail the test since we verify what's printed in the
        // console...
        LauncherConfig config = LauncherConfig.builder().enableTestExecutionListenerAutoRegistration(false).build();
        Launcher launcher = LauncherFactory.create(config);
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        launcher.execute(request, summaryListener);

        return summaryListener.getSummary();
    }
}
