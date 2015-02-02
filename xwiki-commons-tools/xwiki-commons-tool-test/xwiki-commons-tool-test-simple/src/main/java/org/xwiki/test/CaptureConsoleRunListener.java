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

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * Captures any content sent to stdout/stderr by the unit tests and report a failure if the content is not empty.
 *
 * @version $Id$
 * @since 7.0M1
 */
public class CaptureConsoleRunListener extends RunListener
{
    private PrintStream savedOut;

    private PrintStream savedErr;

    private ByteArrayOutputStream collectingContentStream;

    @Override
    public void testStarted(Description description) throws Exception
    {
        this.collectingContentStream = new ByteArrayOutputStream();
        PrintStream collectingPrintStream = new PrintStream(this.collectingContentStream);

        // Capture stdout
        this.savedOut = System.out;
        System.setOut(collectingPrintStream);

        // Capture stderr
        this.savedErr = System.err;
        System.setErr(collectingPrintStream);
    }

    @Override
    public void testFinished(Description description) throws Exception
    {
        // Put back stdout
        System.setOut(this.savedOut);

        // Put back stderr
        System.setOut(this.savedErr);

        // If there is stdout or stderr content then fail the test
        String outputContent = this.collectingContentStream.toString();
        if (!outputContent.isEmpty()) {
            throw new AssertionError("There should be no content output to the console by the test! Instead we got ["
                + outputContent + "]");
        }
    }
}
