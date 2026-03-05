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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.xwiki.test.LogLevel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Unit tests for {@link FailingTestDebuggingTestExecutionListener}.
 *
 * @version $Id$
 */
class FailingTestDebuggingTestExecutionListenerTest
{
    @RegisterExtension
    private static final LogCaptureExtension LOGCAPTURE = new LogCaptureExtension(LogLevel.INFO);

    static class SampleTestCase
    {
        @Test
        @SuppressWarnings("java:S2699")
        void failingTest()
        {
            // Throw an exception to verify that get information about failing tests.
            throw new RuntimeException("error");
        }

        @Test
        void successfulTest()
        {
            assertTrue(true);
        }
    }

    @Test
    void executeWhenInCI()
    {
        // Simulate that we're on the CI
        try {
            System.setProperty("inCI", "true");

            TestExecutionSummary summary = executeJUnit(SampleTestCase.class);

            assertEquals(1, summary.getFailures().size());
            assertEquals(1, summary.getTestsSucceededCount());
            assertEquals("error",
                summary.getFailures().getFirst().getException().getMessage());
            assertEquals("---- Start of environment debugging information", LOGCAPTURE.getMessage(0));
            assertEquals("---- End of environment debugging information",
                LOGCAPTURE.getMessage(LOGCAPTURE.size() - 1));
            LOGCAPTURE.ignoreAllMessages();
        } finally {
            System.setProperty("inCI", "false");
        }
    }

    private TestExecutionSummary executeJUnit(Class<?> testClass)
    {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(testClass))
            .build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        launcher.execute(request, summaryListener);

        return summaryListener.getSummary();
    }
}
