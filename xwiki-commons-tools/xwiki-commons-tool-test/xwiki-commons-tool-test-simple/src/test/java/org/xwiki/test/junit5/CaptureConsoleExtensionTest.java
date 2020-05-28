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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.xwiki.test.junit5.FailingTestDebuggingTestExecutionListener.START_MESSAGE;
import static org.xwiki.test.junit5.FailingTestDebuggingTestExecutionListener.STOP_MESSAGE;

/**
 * Unit tests for {@link CaptureConsoleExtension}.
 *
 * @version $Id$
 */
public class CaptureConsoleExtensionTest
{
    public static class SampleTestCase
    {
        private static final Logger LOGGER = LoggerFactory.getLogger(SampleTestCase.class);

        @BeforeAll
        static void setUp()
        {
            // Output before the tests have started
            LOGGER.info("In beforeAll");
        }

        @BeforeEach
        void before()
        {
            LOGGER.info("In beforeEach");
        }

        @Test
        @Order(1)
        void outputToConsole()
        {
            LOGGER.info("In test");
        }

        @Test
        @Order(2)
        void dontOutputToConsole()
        {
        }

        @Test
        @Order(3)
        void skipOutputContent()
        {
            LOGGER.info(START_MESSAGE);
            LOGGER.info("whatever");
            LOGGER.info(STOP_MESSAGE);
            LOGGER.info("after");
        }
    }

    @Test
    void verifyTestExecutionListener()
    {
        PrintStream savedOut = System.out;
        try {
            // Capture output since the CaptureConsoleExtension is automatically enabled and would fail
            // this test since it outputs things to stdout!
            System.setOut(new PrintStream(new ByteArrayOutputStream()));

            // Programmatically launch Jupiter Engine with our CaptureConsoleExtension registered in it.
            // Also register a SummaryGeneratingListener to capture the test output so that we can assert it.

            LauncherDiscoveryRequestBuilder builder =
                LauncherDiscoveryRequestBuilder.request().selectors(selectClass(SampleTestCase.class));

            LauncherDiscoveryRequest request = builder.build();
            Launcher launcher = LauncherFactory.create();
            SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
            launcher.execute(request, summaryListener);

            TestExecutionSummary summary = summaryListener.getSummary();
            assertResult(summary);
        } finally {
            System.setOut(savedOut);
        }
    }

    private void assertResult(TestExecutionSummary summary)
    {
        // Since it's possible to skip the capture listener in the build by passing some system property (as it's done
        // for example on our CI, we also need to take this into account in this test since when the capture listener
        // is off, we won't get failures! :)
        if (CaptureConsoleExtension.shouldSkip()) {
            assertEquals(0, summary.getFailures().size());
        } else {
            assertEquals(4, summary.getFailures().size());
            // Test outputToConsole() has 2 errors: one from the beforeEach and one from itself
            assertThat(summary.getFailures().get(0).getException().getMessage(), matchesPattern(
                "There should be no content output to the console by the test! Instead we got \\[.* \\[main\\] "
                    + "INFO  o\\.x\\.t\\.j\\.CaptureConsoleExtensionTest\\$SampleTestCase - In beforeEach\n"
                    + ".* \\[main\\] "
                    + "INFO  o\\.x\\.t\\.j\\.CaptureConsoleExtensionTest\\$SampleTestCase - In test\n"
                    + "\\]"
            ));
            assertThat(summary.getFailures().get(1).getException().getMessage(), matchesPattern(
                "There should be no content output to the console by the test! Instead we got \\[.* \\[main\\] "
                    + "INFO  o\\.x\\.t\\.j\\.CaptureConsoleExtensionTest\\$SampleTestCase - In beforeEach\n"
                    + "\\]"
            ));
            assertThat(summary.getFailures().get(2).getException().getMessage(), matchesPattern(
                "There should be no content output to the console by the test! Instead we got \\[.* \\[main\\] "
                    + "INFO  o\\.x\\.t\\.j\\.CaptureConsoleExtensionTest\\$SampleTestCase - In beforeEach\n"
                    + ".* \\[main\\] "
                    + "INFO  o\\.x\\.t\\.j\\.CaptureConsoleExtensionTest\\$SampleTestCase - after\n"
                    + "\\]"
            ));
            assertThat(summary.getFailures().get(3).getException().getMessage(), matchesPattern(
                "There should be no content output to the console by the test! Instead we got \\[.* \\[main\\] "
                    + "INFO  o\\.x\\.t\\.j\\.CaptureConsoleExtensionTest\\$SampleTestCase - In beforeAll\n"
                    + "\\]"
            ));
        }
    }
}
