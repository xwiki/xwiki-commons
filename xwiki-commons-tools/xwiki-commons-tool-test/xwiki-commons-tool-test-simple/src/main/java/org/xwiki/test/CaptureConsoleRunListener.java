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
package org.xwiki.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Captures any content sent to stdout/stderr by the unit tests and report a failure if the content is not empty.
 *
 * @version $Id$
 * @since 7.0M1
 */
public class CaptureConsoleRunListener extends RunListener
{
    private static final boolean SKIP =
        Boolean.parseBoolean(System.getProperty("xwiki.surefire.captureconsole.skip", "false"));

    private PrintStream savedOut;

    private PrintStream savedErr;

    private ByteArrayOutputStream collectingContentStream;

    private boolean isInErrorAlready;

    @Override
    public void testFailure(Failure failure) throws Exception
    {
        // There was a failure, skip the check at the end since Junit will stop already!
        this.isInErrorAlready = true;
    }

    @Override
    public void testAssumptionFailure(Failure failure)
    {
        // There was a failure, skip the check at the end since Junit will stop already!
        this.isInErrorAlready = true;
    }

    @Override
    public void testRunStarted(Description description) throws Exception
    {
        if (SKIP) {
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
    public void testRunFinished(Result result) throws Exception
    {
        if (SKIP) {
            return;
        }

        // Put back stdout
        System.setOut(this.savedOut);

        // Put back stderr
        System.setOut(this.savedErr);

        // If there is stdout or stderr content then fail the test, unless there was already some error, in which
        // case we let junit fail in a classical way and we don't interfere...
        String outputContent = this.collectingContentStream.toString();
        if (!outputContent.isEmpty() && !this.isInErrorAlready) {
            throw new AssertionError("There should be no content output to the console by the test! Instead we got ["
                + outputContent + "]");
        }
    }
}
