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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Unit tests for {@link CaptureConsoleTestExecutionListener}.
 *
 * @version $Id$
 */
public class CaptureConsoleTestExecutionListenerTest
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

        @Test
        void outputToConsole()
        {
            LOGGER.info("In outputToConsole");
        }

        @Test
        void dontOutputToConsole()
        {
        }
    }

    @Test
    void verifyTestExecutionListener()
    {
        PrintStream savedOut = System.out;
        try {
            // Capture output since the CaptureConsoleTestExecutionListener is automatically enabled and would fail
            // this test since it output things to stdout!
            System.setOut(new PrintStream(new ByteArrayOutputStream()));

            // Programmatically launch Jupiter Engine with our CaptureConsoleTestExecutionListener registered in it.
            // Also register a SummaryGeneratingListener to capture the test output so that we can assert it.
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(SampleTestCase.class))
                .build();
            Launcher launcher = LauncherFactory.create();
            SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
            CaptureConsoleTestExecutionListener captureListener = new CaptureConsoleTestExecutionListener();
            launcher.execute(request, captureListener, summaryListener);

            TestExecutionSummary summary = summaryListener.getSummary();
            assertResult(summary, captureListener);
        } finally {
            System.setOut(savedOut);
        }
    }


    private void assertResult(TestExecutionSummary summary, CaptureConsoleTestExecutionListener captureListener)
    {
        // Since it's possible to skip the capture listener in the build by passing some system property (as it's done
        // for example on our CI, we also need to take this into account in this test since when the capture listener
        // is off, we won't get an failures! :)
        if (captureListener.shouldSkip()) {
            assertEquals(0, summary.getFailures().size());
        } else {
            assertEquals(1, summary.getFailures().size());
            assertTrue(summary.getFailures().get(0).getException().getMessage().matches(
                "There should be no content output to the console by the test! Instead we got \\[.* \\[main\\] "
                    + "INFO  o\\.x\\.t\\.j\\.CaptureConsoleTestExecutionListenerTest\\$SampleTestCase - In beforeAll\n"
                    + ".* \\[main\\] INFO  o\\.x\\.t\\.j\\.CaptureConsoleTestExecutionListenerTest\\$SampleTestCase - "
                    + "In outputToConsole\n"
                    + "\\]"
            ));
        }
    }
}
