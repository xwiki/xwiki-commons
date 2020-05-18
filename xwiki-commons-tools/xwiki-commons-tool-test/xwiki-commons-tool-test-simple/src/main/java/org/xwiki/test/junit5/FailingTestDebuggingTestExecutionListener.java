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

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides environment information when a test fails to execute in XWiki's CI environment (i.e. Jenkins), in order
 * to help understanding why the test failed. This is mostly useful for functional tests but it can also be useful
 * for some integration tests (e.g. Greenmail failing to start because its port is already bound by some leftover
 * process, etc).
 *
 * @version $Id$
 * @since 12.2RC1
 */
public class FailingTestDebuggingTestExecutionListener implements TestExecutionListener
{
    static final String START_MESSAGE = "---- Start of environment debugging information";

    static final String STOP_MESSAGE = "---- End of environment debugging information";

    private static final Logger LOGGER = LoggerFactory.getLogger(FailingTestDebuggingTestExecutionListener.class);

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult)
    {
        if (testExecutionResult.getThrowable().isPresent() && isInCI()) {
            // We display a start and end message so that the CaptureConsoleExtension can filter out this information
            // as we don't want it to fail when it's printed.
            // Otherwise what can happen is (for example): LogCapture verifies that when some logs are not captured,
            // the test fails with an exception but if it does then this debugging listener is triggered, and in turn
            // it would trigger the CaptureConsoleExtension extension since this debugging listener prints something
            // in the logs ;)
            LOGGER.info(START_MESSAGE);
            LOGGER.info(RuntimeUtils.run("top -b -n 1"));
            LOGGER.info(RuntimeUtils.run("lsof -i -P -n"));
            LOGGER.info(RuntimeUtils.run("docker ps -a"));
            LOGGER.info(RuntimeUtils.run("docker run --cap-add=NET_ADMIN --network=host --rm --entrypoint \"/bin/sh\" "
                + "vimagick/iptables -c \"/sbin/iptables -S\""));
            LOGGER.info(RuntimeUtils.run("docker events --since '15m' --until '0m'"));
            LOGGER.info(STOP_MESSAGE);
        }
    }

    private boolean isInCI()
    {
        // On Jenkins Agents, when tests execute with the XWiki Jenkins Pipeline, the NODE_NAME environment variable
        // is supposed to be set and this is how we consider we're running in the CI.
        // We also support a system property ("inCI") so that we can trigger this listener manually from tests or
        // from local machines.
        return System.getenv("NODE_NAME") != null || Boolean.parseBoolean(System.getProperty("inCI"));
    }
}
