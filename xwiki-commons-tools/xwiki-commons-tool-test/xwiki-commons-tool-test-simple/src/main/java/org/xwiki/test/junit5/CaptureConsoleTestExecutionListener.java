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
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.pom.ParsedPomFile;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenWorkingSessionContainer;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * Captures any content sent to stdout/stderr by JUnit5 unit tests and report a failure if the content is not empty.
 *
 * @version $Id$
 * @since 11.1RC1
 */
public class CaptureConsoleTestExecutionListener implements TestExecutionListener
{
    private static final String FALSE = "false";

    private static final String CAPTURECONSOLESKIP_PROPERTY = "xwiki.surefire.captureconsole.skip";

    private static final boolean SKIP = Boolean.parseBoolean(System.getProperty(CAPTURECONSOLESKIP_PROPERTY, FALSE));

    private PrintStream savedOut;

    private PrintStream savedErr;

    private ByteArrayOutputStream collectingContentStream;

    private Boolean skip;

    @Override
    public void executionStarted(TestIdentifier testIdentifier)
    {
        if (shouldSkip()) {
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
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult)
    {
        if (shouldSkip()) {
            return;
        }

        // Put back stdout
        System.setOut(this.savedOut);

        // Put back stderr
        System.setOut(this.savedErr);

        // If there is stdout or stderr content then fail the test, unless there was already some error, in which
        // case we let junit fail in a classical way and we don't interfere...
        String outputContent = this.collectingContentStream.toString();
        if (!outputContent.isEmpty() && testExecutionResult.getStatus().equals(TestExecutionResult.Status.SUCCESSFUL)) {
            throw new AssertionError("There should be no content output to the console by the test! Instead we got ["
                + outputContent + "]");
        }
    }

    /**
     * @return true if the check should be skipper or false otherwise. The {@code xwiki.surefire.captureconsole.skip}
     *         System property is checked first and if it doesn't exist, the Maven property of the same name is read
     *         from the current {@code pom.xml}
     */
    private boolean shouldSkip()
    {
        if (SKIP) {
            return true;
        }

        if (this.skip == null) {
            ConfigurableMavenResolverSystem system = Maven.configureResolver().workOffline();
            system.loadPomFromFile("pom.xml");
            // Hack around a bit to get to the internal Maven Model object
            ParsedPomFile parsedPom =
                ((MavenWorkingSessionContainer) system).getMavenWorkingSession().getParsedPomFile();
            this.skip = Boolean.valueOf(parsedPom.getProperties().getProperty(CAPTURECONSOLESKIP_PROPERTY, FALSE));
        }

        return this.skip;
    }
}
