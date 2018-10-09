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

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Integration tests for the Format Mojo.
 *
 * @version $Id$
 * @since 4.0M2
 */
public class FormatMojoTest extends AbstractMojoTest
{
    @Test
    public void formatWhenNoStyle() throws Exception
    {
        Verifier verifier = createVerifier("/format");
        verifier.addCliOption("-Dincludes=**/NoStyle/*.xml");
        verifier.addCliOption("-Dpretty=false");
        verifier.addCliOption("-DformatLicense=true");
        verifier.addCliOption("-Dcommons.version=" + System.getProperty("commons.version"));
        verifier.executeGoal("xar:format");
        verifier.verifyErrorFreeLog();

        String content = FileUtils.fileRead(new File(verifier.getBasedir(), "src/main/resources/NoStyle/Page1.xml"));
        String expected = FileUtils.fileRead(new File(verifier.getBasedir(), "ExpectedNoStylePage1.xml"));
        assertEquals(expected, content);

        // Test with a XML file having a license header
        content = FileUtils.fileRead(new File(verifier.getBasedir(), "src/main/resources/NoStyle/Page2.xml"));
        expected = FileUtils.fileRead(new File(verifier.getBasedir(), "ExpectedNoStylePage2.xml"));
        assertEquals(expected, content);

        // Test the default language
        content = FileUtils.fileRead(new File(verifier.getBasedir(), "src/main/resources/NoStyle/Page3.xml"));
        expected = FileUtils.fileRead(new File(verifier.getBasedir(), "ExpectedNoStylePage3.xml"));
        assertEquals(expected, content);
        content = FileUtils.fileRead(new File(verifier.getBasedir(), "src/main/resources/NoStyle/Page3.fr.xml"));
        expected = FileUtils.fileRead(new File(verifier.getBasedir(), "ExpectedNoStylePage3.fr.xml"));
        assertEquals(expected, content);

        // Test with document dates present
        content = FileUtils.fileRead(new File(verifier.getBasedir(), "src/main/resources/NoStyle/Page4.xml"));
        expected = FileUtils.fileRead(new File(verifier.getBasedir(), "ExpectedNoStylePage4.xml"));
        assertEquals(expected, content);

        // Test that technical pages are set as hidden
        content = FileUtils.fileRead(new File(verifier.getBasedir(), "src/main/resources/NoStyle/Translations.xml"));
        expected = FileUtils.fileRead(new File(verifier.getBasedir(), "ExpectedNoStyleTranslations.xml"));
        assertEquals(expected, content);

        // Verify that not included pages are not formatted
        try {
            verifier.verifyTextInLog("Formatting [Pretty");
            fail("Exception should have been thrown here");
        } catch (VerificationException expectedException) {
            // Passed!
        }
    }

    @Test
    public void formatWhenPrettyPrinting() throws Exception
    {
        Verifier verifier = createVerifier("/format");
        verifier.addCliOption("-Dincludes=**/Pretty/*.xml");
        verifier.addCliOption("-DformatLicense=true");
        verifier.executeGoal("xar:format");
        verifier.verifyErrorFreeLog();

        String content = FileUtils.fileRead(new File(verifier.getBasedir(), "src/main/resources/Pretty/Page1.xml"));
        String expected = FileUtils.fileRead(new File(verifier.getBasedir(), "ExpectedPrettyPage1.xml"));
        assertEquals(expected, content);

        // Test with a XML file having a license header
        content = FileUtils.fileRead(new File(verifier.getBasedir(), "src/main/resources/Pretty/Page2.xml"));
        expected = FileUtils.fileRead(new File(verifier.getBasedir(), "ExpectedPrettyPage2.xml"));
        assertEquals(expected, content);

        // Test with document dates present
        content = FileUtils.fileRead(new File(verifier.getBasedir(), "src/main/resources/Pretty/Page3.xml"));
        expected = FileUtils.fileRead(new File(verifier.getBasedir(), "ExpectedPrettyPage3.xml"));
        assertEquals(expected, content);

        // Verify the right version of XML is set for XAR 1.3
        content = FileUtils.fileRead(new File(verifier.getBasedir(), "src/main/resources/Pretty/Page4.xml"));
        expected = FileUtils.fileRead(new File(verifier.getBasedir(), "ExpectedPrettyPage4.xml"));
        assertEquals(expected, content);
    }
}
