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

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.codehaus.plexus.archiver.zip.ZipFile;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Integration tests for the XAR Mojo.
 *
 * @version $Id$
 * @since 4.2M1
 */
public class XARMojoTest
{
    @Test
    public void invalidPackageXmlThrowsException() throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/invalidPackageFile");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");

        try {
            verifier.executeGoals(Arrays.asList("clean", "package"));
            Assert.fail("Should have raised an exception since the provided package.xml is invalid.");
        } catch (Exception expected) {
            // Expected
        }

        verifier.verifyTextInLog("[ERROR] The existing [package.xml] is invalid.");
    }

    @Test
    public void validPackageXml() throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/validXml");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");
        verifier.executeGoals(Arrays.asList("clean", "package"));
        verifier.verifyErrorFreeLog();

        File tempDir = new File(testDir, "target/temp");
        tempDir.mkdirs();

        // Extract the generated XAR so that we verify its content easily
        File xarFile = new File(testDir, "target/xwiki-commons-tool-xar-plugin-test.xar");
        ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
        unarchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_ERROR, "xar"));
        unarchiver.setDestDirectory(tempDir);
        unarchiver.extract();

        ZipFile zip = new ZipFile(xarFile);
        Enumeration entries = zip.getEntries();
        Assert.assertTrue(entries.hasMoreElements());
        Assert.assertEquals(entries.nextElement().toString(), XARMojo.PACKAGE_XML);

        File classesDir = new File(testDir, "target/classes");
        Collection<String> documentNames = XARMojo.getDocumentNamesFromXML(new File(classesDir, "package.xml"));

        int countEntries = 0;
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().toString();
            ++countEntries;

            File currentFile = new File(tempDir, entryName);
            String documentName = XWikiDocument.getFullName(currentFile);
            if (!documentNames.contains(documentName)) {
                Assert.fail(String.format("Document [%s] cannot be found in the newly created xar archive.",
                    documentName));
            }
        }
        Assert.assertEquals("The newly created xar archive doesn't contain the required documents",
            documentNames.size(), countEntries);
    }

    @Test
    public void noPackageXml() throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/noPackageXml");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");
        verifier.executeGoals(Arrays.asList("clean", "package"));
        verifier.verifyErrorFreeLog();

        File xarFile = new File(testDir, "target/xwiki-commons-tool-xar-plugin-test.xar");
        ZipFile zip = new ZipFile(xarFile);
        Assert.assertNotNull("Package.xml file not found in zip!", zip.getEntry(XARMojo.PACKAGE_XML));

        File tempDir = new File(testDir, "target/temp");
        tempDir.mkdirs();

        // Extract package.xml and extract all the entries one by one and read them as a XWiki Document to verify
        // they're valid.
        ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
        unarchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_ERROR, "xar"));
        unarchiver.setDestDirectory(tempDir);
        unarchiver.extract();

        File classesDir = new File(testDir, "target/classes");
        Collection<String> documentNames = XARMojo.getDocumentNamesFromXML(new File(classesDir, "package.xml"));
        int countEntries = 0;
        Enumeration entries = zip.getEntries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().toString();
            if (!entryName.equals(XARMojo.PACKAGE_XML)) {
                ++countEntries;

                File currentFile = new File(tempDir, entryName);
                String documentName = XWikiDocument.getFullName(currentFile);
                if (!documentNames.contains(documentName)) {
                    Assert.fail(String.format("Document [%s] cannot be found in the newly created xar archive.",
                        documentName));
                }
            }
        }
        Assert.assertEquals("The newly created xar archive doesn't contain the required documents",
            documentNames.size(), countEntries);
    }
}
