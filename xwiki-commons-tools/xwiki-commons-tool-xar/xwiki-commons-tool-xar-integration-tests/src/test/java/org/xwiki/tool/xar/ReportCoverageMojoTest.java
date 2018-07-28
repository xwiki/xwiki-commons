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
package org.xwiki.tool.xar;

import org.apache.maven.it.Verifier;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Integration tests for the Instrument Coverage Mojo.
 *
 * @version $Id$
 * @since 10.7RC1
 */
public class ReportCoverageMojoTest extends AbstractMojoTest
{
    @Test
    public void instrumentWhenValid() throws Exception
    {
        Verifier verifier = createVerifier("/reportCoverageValid");
        verifier.executeGoals(Arrays.asList("xar:reportCoverage"));

        File reportOutput = new File(verifier.getBasedir(), "target/report/velocityCoverage.txt");
        assertTrue(reportOutput.exists());


    }
}
