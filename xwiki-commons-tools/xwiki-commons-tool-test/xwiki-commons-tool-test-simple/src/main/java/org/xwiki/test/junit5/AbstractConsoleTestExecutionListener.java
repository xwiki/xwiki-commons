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

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;

/**
 * Captures any content sent to stdout/stderr by JUnit5 unit tests and report a failure if the content doesn't follow
 * some rules.
 *
 * @version $Id$
 * @since 11.4RC1
 */
public abstract class AbstractConsoleTestExecutionListener extends AbstractTestExecutionListener
{
    private PrintStream savedOut;

    private PrintStream savedErr;

    private ByteArrayOutputStream collectingContentStream;

    @Override
    protected void executionStartedInternal(TestIdentifier testIdentifier)
    {
        // We save sdtout and stderr in the top level "Test" (i.e. at the junit-jupiter engine level), to be able to
        // verify the full log (and not just logs starting when tests execute)
        if (testIdentifier.getParentId().isPresent()) {
            return;
        }

        this.savedOut = System.out;
        this.savedErr = System.err;
        this.collectingContentStream = new ByteArrayOutputStream();

        // Capture stdout but continue sending data to it at the same time
        System.setOut(new PrintStream(new TeeOutputStream(this.collectingContentStream, this.savedOut)));

        // Capture stderr but continue sending data to it at the same time
        System.setErr(new PrintStream(new TeeOutputStream(this.collectingContentStream, this.savedErr)));
    }

    @Override
    protected void executionFinishedInternal(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult)
    {
        // We verify logs for each test since we need to fail the tests for Maven to report an error. If we
        // fail the jupiter engine only no Maven error is reported and the build carries on.
        // We restore stdout/stderr when the top level engine stops
        if (!testIdentifier.getParentId().isPresent()) {
            // Put back stdout
            System.setOut(this.savedOut);

            // Put back stderr
            System.setOut(this.savedErr);
        } else {
            if (testIdentifier.isTest()) {
                // If there is stdout or stderr content then fail the test, unless there was already some error, in
                // which case we let junit fail in a classical way and we don't interfere...
                if (testExecutionResult.getStatus().equals(TestExecutionResult.Status.SUCCESSFUL)) {
                    String outputContent = this.collectingContentStream.toString();
                    validateOutputForTest(outputContent);
                }
            }
        }
    }

    /**
     * @param outputContent the content to validate. Should throw a {@link AssertionError} in case of validation failure
     */
    protected abstract void validateOutputForTest(String outputContent);
}
