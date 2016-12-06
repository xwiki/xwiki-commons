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
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.archiver.zip.ZipEntry;
import org.codehaus.plexus.archiver.zip.ZipFile;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Ignore;
import org.junit.Test;
import org.xwiki.tool.xar.internal.XWikiDocument;

import static org.junit.Assert.*;

/**
 * Integration tests for the XAR Mojo.
 * 
 * @version $Id$
 * @since 4.2M1
 */
public class XARMojoTest extends AbstractMojoTest {
    @Test
    public void invalidPackageXmlThrowsException() throws Exception {
        Verifier verifier = createVerifier("/invalidPackageFile");

        try {
            verifier.executeGoals(Arrays.asList("clean", "package"));
            fail("Should have raised an exception since the provided package.xml is invalid.");
        } catch (Exception expected) {
            // Expected
        }

        verifier.verifyTextInLog("[ERROR] The existing [package.xml] is invalid.");
    }

    @Test
    public void validPackageXml() throws Exception {
        Verifier verifier = createVerifier("/validXml");
        verifier.executeGoals(Arrays.asList("clean", "package"));
        verifier.verifyErrorFreeLog();

        File tempDir = new File(verifier.getBasedir(), "target/temp");
        tempDir.mkdirs();

        // Extract the generated XAR so that we verify its content easily
        File xarFile = new File(verifier.getBasedir(), "target/xwiki-commons-tool-xar-plugin-test-1.0.xar");
        ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
        unarchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_ERROR, "xar"));
        unarchiver.setDestDirectory(tempDir);
        unarchiver.extract();

        ZipFile zip = new ZipFile(xarFile);
        Enumeration<ZipEntry> entries = zip.getEntries();
        assertTrue(entries.hasMoreElements());
        assertEquals(entries.nextElement().toString(), XARMojo.PACKAGE_XML);

        File classesDir = new File(verifier.getBasedir(), "target/classes");
        Collection<String> documentNames = XARMojo.getDocumentNamesFromXML(new File(classesDir, "package.xml"));

        int countEntries = 0;
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().toString();
            ++countEntries;

            File currentFile = new File(tempDir, entryName);
            String documentName = XWikiDocument.getReference(currentFile);
            if (!documentNames.contains(documentName)) {
                fail(String.format("Document [%s] cannot be found in the newly created xar archive.", documentName));
            }
        }
        assertEquals("The newly created xar archive doesn't contain the required documents", documentNames.size(),
                countEntries);
    }

    @Test
    public void noPackageXml() throws Exception {
        Verifier verifier = createVerifier("/noPackageXml");
        verifier.executeGoals(Arrays.asList("clean", "package"));
        verifier.verifyErrorFreeLog();

        File xarFile = new File(verifier.getBasedir(), "target/xwiki-commons-tool-xar-plugin-test-1.0.xar");
        ZipFile zip = new ZipFile(xarFile);
        assertNotNull("Package.xml file not found in zip!", zip.getEntry(XARMojo.PACKAGE_XML));

        File tempDir = new File(verifier.getBasedir(), "target/temp");
        tempDir.mkdirs();

        // Extract package.xml and extract all the entries one by one and read them as a XWiki Document to verify
        // they're valid.
        ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
        unarchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_ERROR, "xar"));
        unarchiver.setDestDirectory(tempDir);
        unarchiver.extract();

        File classesDir = new File(verifier.getBasedir(), "target/classes");
        Collection<String> documentNames = XARMojo.getDocumentNamesFromXML(new File(classesDir, "package.xml"));
        int countEntries = 0;
        Enumeration<ZipEntry> entries = zip.getEntries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().toString();
            if (!entryName.equals(XARMojo.PACKAGE_XML)) {
                ++countEntries;

                File currentFile = new File(tempDir, entryName);
                String documentName = XWikiDocument.getReference(currentFile);
                if (!documentNames.contains(documentName)) {
                    fail(String.format("Document [%s] cannot be found in the newly created xar archive.",
                            documentName));
                }
            }
        }
        assertEquals("The newly created xar archive doesn't contain the required documents", documentNames.size(),
                countEntries);
    }

    @Test
    public void nestedSpacesXml() throws Exception {
        Verifier verifier = createVerifier("/nestedSpaces");
        verifier.executeGoals(Arrays.asList("clean", "package"));
        verifier.verifyErrorFreeLog();

        File tempDir = new File(verifier.getBasedir(), "target/temp");
        tempDir.mkdirs();

        // Extract the generated XAR so that we verify its content easily
        File xarFile = new File(verifier.getBasedir(), "target/xwiki-commons-tool-xar-plugin-test-1.0.xar");
        ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
        unarchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_ERROR, "xar"));
        unarchiver.setDestDirectory(tempDir);
        unarchiver.extract();

        File classesDir = new File(verifier.getBasedir(), "target/classes");
        Collection<String> documentNames = XARMojo.getDocumentNamesFromXML(new File(classesDir, "package.xml"));

        assertEquals("The newly created xar archive doesn't contain the required documents", 1, documentNames.size());

        assertEquals("Page reference not properly serialized in the package.xml", "1.2.page", documentNames.iterator()
                .next());
    }

    @Test
    @Ignore("Could not make it work, for some reason the plugin configuration is not taken into account!")
    public void transformXML() throws Exception {
        Verifier verifier = createVerifier("/transformedXml");
        verifier.executeGoals(Arrays.asList("clean", "package"));
        verifier.verifyErrorFreeLog();
    }

    @Test
    public void invalidXml() throws Exception
    {
        Verifier verifier = createVerifier("/invalidXml");
        try {
            verifier.executeGoals(Arrays.asList("clean", "package"));
            fail("Should have failed with an exception here!");
        } catch (VerificationException expected) {
            verifier.verifyTextInLog("Error while creating XAR file");
            verifier.verifyTextInLog("Content doesn't point to valid wiki page XML");
            verifier.verifyTextInLog("Failed to parse");
        }
    }
}