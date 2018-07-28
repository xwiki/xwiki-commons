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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Ignore;
import org.junit.Test;
import org.xwiki.tool.xar.internal.XWikiDocument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration tests for the Instrument Coverage Mojo.
 *
 * @version $Id$
 * @since 10.7RC1
 */
public class InstrumentCoverageMojoTest extends AbstractMojoTest
{
    @Test
    public void instrumentWhenValid() throws Exception
    {
        Verifier verifier = createVerifier("/instrumentValid");

        verifier.executeGoals(Arrays.asList("clean", "xar:instrument", "package"));

        File xmlOutput = new File(verifier.getBasedir(), "target/velocity-instrumented/Main/Activity.xml");
        List<String> allLines = Files.readAllLines(xmlOutput.toPath());

        assertTrue(allLines.contains("$services.logging.getLogger('org.xwiki.XARVelocityCoverage').info('Filename [Activity.xml]')"));
        assertTrue(allLines.contains("$services.logging.getLogger('org.xwiki.XARVelocityCoverage').info('COV')"));
        assertTrue(allLines.contains("$services.logging.getLogger('org.xwiki.XARVelocityCoverage').info('Counter Velocity [481]')"));

        File tempDir = new File(verifier.getBasedir(), "target/temp");
        tempDir.mkdirs();

        File xarFile = new File(verifier.getBasedir(), "target/xwiki-commons-tool-xar-plugin-test-1.0.xar");
        ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
        unarchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_ERROR, "xar"));
        unarchiver.setDestDirectory(tempDir);
        unarchiver.extract();

        File xmlOutputFromZip = new File(tempDir, "Main/Activity.xml");
        allLines = Files.readAllLines(xmlOutputFromZip.toPath());

        assertTrue(allLines.contains("$services.logging.getLogger('org.xwiki.XARVelocityCoverage').info('Filename [Activity.xml]')"));
        assertTrue(allLines.contains("$services.logging.getLogger('org.xwiki.XARVelocityCoverage').info('COV')"));
        assertTrue(allLines.contains("$services.logging.getLogger('org.xwiki.XARVelocityCoverage').info('Counter Velocity [481]')"));
    }
}
