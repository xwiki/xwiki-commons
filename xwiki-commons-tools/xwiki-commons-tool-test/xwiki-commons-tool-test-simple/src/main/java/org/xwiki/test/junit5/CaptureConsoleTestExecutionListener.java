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

import org.junit.platform.launcher.TestIdentifier;

/**
 * Captures any content sent to stdout/stderr by JUnit5 unit tests and report a failure if the content is not empty.
 *
 * @version $Id$
 * @since 11.1RC1
 */
public class CaptureConsoleTestExecutionListener extends AbstractConsoleTestExecutionListener
{
    private static final String CAPTURECONSOLESKIP_PROPERTY = "xwiki.surefire.captureconsole.skip";

    @Override
    protected String getSkipSystemPropertyKey()
    {
        return CAPTURECONSOLESKIP_PROPERTY;
    }

    @Override
    protected void validateOutputForTest(String outputContent, TestIdentifier testIdentifier)
    {
        if (!outputContent.isEmpty()) {
            throw new AssertionError(String.format("There should be no content output to the console by the test! "
                + "Instead we got [%s]", outputContent));
        }
    }
}
