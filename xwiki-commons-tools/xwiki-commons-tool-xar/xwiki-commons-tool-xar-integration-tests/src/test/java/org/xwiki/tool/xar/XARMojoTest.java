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
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.maven.executor.ExecutorException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;
import org.xwiki.tool.xar.internal.XWikiDocument;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for the XAR Mojo.
 *
 * @version $Id$
 * @since 4.2M1
 */
class XARMojoTest extends AbstractMojoTest
{
    @Test
    void invalidPackageXmlThrowsException() throws Exception
    {
        MavenBuild build = createBuild("/invalidPackageFile");

        assertThrows(Exception.class, () -> {
            build.execute("clean", "package");
        });

        assertThat(build.getLog(), containsString("[ERROR] The existing [package.xml] is invalid."));
    }

    @Test
    void validPackageXml() throws Exception
    {
        MavenBuild build = createBuild("/validXml");
        build.execute("clean", "package");
        build.verifyErrorFreeLog();

        File tempDir = new File(build.getBasedir(), "target/temp");
        tempDir.mkdirs();

        // Extract the generated XAR so that we verify its content easily
        File xarFile = new File(build.getBasedir(), "target/xwiki-commons-tool-xar-plugin-test-1.0.xar");
        ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
        unarchiver.setDestDirectory(tempDir);
        unarchiver.extract();

        try (ZipFile zip = new ZipFile(xarFile)) {
            ZipArchiveEntry packageEntry = zip.getEntry(XARMojo.PACKAGE_XML);

            Collection<String> documentNames;
            try (InputStream packageStream = zip.getInputStream(packageEntry)) {
                documentNames = XARMojo.getDocumentNamesFromXML(packageStream);
            }

            Enumeration<ZipArchiveEntry> entries = zip.getEntries();

            int countEntries = 0;
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

            assertEquals(documentNames.size(), countEntries,
                "The newly created xar archive doesn't contain the required documents");
        }
    }

    @Test
    void noPackageXml() throws Exception
    {
        MavenBuild build = createBuild("/noPackageXml");
        build.execute("clean", "package");
        build.verifyErrorFreeLog();

        File xarFile = new File(build.getBasedir(), "target/xwiki-commons-tool-xar-plugin-test-1.0.xar");

        try (ZipFile zip = new ZipFile(xarFile)) {
            assertNotNull(zip.getEntry(XARMojo.PACKAGE_XML), "Package.xml file not found in zip!");

            File tempDir = new File(build.getBasedir(), "target/temp");
            tempDir.mkdirs();

            // Extract package.xml and extract all the entries one by one and read them as a XWiki Document to verify
            // they're valid.
            ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
            unarchiver.setDestDirectory(tempDir);
            unarchiver.extract();

            File classesDir = new File(build.getBasedir(), "target/classes");
            Collection<String> documentNames = XARMojo.getDocumentNamesFromXML(new File(classesDir, "package.xml"));
            int countEntries = 0;
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();
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
            assertEquals(documentNames.size(), countEntries,
                "The newly created xar archive doesn't contain the required documents");
        }
    }

    @Test
    void nestedSpacesXml() throws Exception
    {
        MavenBuild build = createBuild("/nestedSpaces");
        build.execute("clean", "package");
        build.verifyErrorFreeLog();

        File tempDir = new File(build.getBasedir(), "target/temp");
        tempDir.mkdirs();

        // Extract the generated XAR so that we verify its content easily
        File xarFile = new File(build.getBasedir(), "target/xwiki-commons-tool-xar-plugin-test-1.0.xar");
        ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
        unarchiver.setDestDirectory(tempDir);
        unarchiver.extract();

        File classesDir = new File(build.getBasedir(), "target/classes");
        Collection<String> documentNames = XARMojo.getDocumentNamesFromXML(new File(classesDir, "package.xml"));

        assertEquals(1, documentNames.size(), "The newly created xar archive doesn't contain the required documents");

        assertEquals("1.2.page", documentNames.iterator().next(),
            "Page reference not properly serialized in the package.xml");
    }

    @Test
    void transformXML() throws Exception
    {
        MavenBuild build = createBuild("/transformedXml");
        build.execute("clean", "package");
        build.verifyErrorFreeLog();

        File tempDir = new File(build.getBasedir(), "target/temp");
        tempDir.mkdirs();

        // Extract the generated XAR so that we verify its content easily
        File xarFile = new File(build.getBasedir(), "target/xwiki-commons-tool-xar-plugin-test-1.0.xar");
        ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
        unarchiver.setDestDirectory(tempDir);
        unarchiver.extract();

        // check that the transformations have taken place
        SAXReader reader = new SAXReader();
        // Avoid triggering Sonarqube rules related to XXE attacks, even though this is a test and thus most likely
        // not subject to XXE attacks.
        reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = reader.read(new File(tempDir, "Blog/WebHome.xml"));
        assertEquals("100", document.selectSingleNode("/xwikidoc/object/property/itemsPerPage").getText(),
            "Transformation of itemsPerPage did not happen?");
        assertEquals("My Blog (The Wiki Blog)", document.selectSingleNode("/xwikidoc/object/property/title").getText(),
            "Transformation of title did not happen?");
        assertEquals("This page will gather my blog.", document.selectSingleNode("/xwikidoc/content").getText().trim(),
            "Insertion of content did not happen?");

        assertTrue(document.selectSingleNode("/xwikidoc/attachment/content") != null,
            "Insertion of attachment did not happen?");
    }

    @Test
    void entities() throws Exception
    {
        MavenBuild build = createBuild("/entries");
        build.execute("clean", "package");
        build.verifyErrorFreeLog();

        File tempDir = new File(build.getBasedir(), "target/temp");
        tempDir.mkdirs();

        // Extract the generated XAR so that we verify its content easily
        File xarFile = new File(build.getBasedir(), "target/xwiki-commons-tool-xar-plugin-test-1.0.xar");
        ZipUnArchiver unarchiver = new ZipUnArchiver(xarFile);
        unarchiver.setDestDirectory(tempDir);
        unarchiver.extract();

        File classesDir = new File(build.getBasedir(), "target/classes");
        Map<String, XAREntry> entries = XARMojo.getXarEntriesMapFromXML(new File(classesDir, "package.xml"));

        assertEquals(3, entries.size(), "The newly created xar archive doesn't contain the required documents");

        assertEquals("home", entries.get("Type.home").getType(), "Not the right type");
        assertEquals("configuration", entries.get("Type.configuration").getType(), "Not the right type");
        assertEquals("custom", entries.get("Type.custom").getType(), "Not the right type");
    }

    @Test
    void invalidXml() throws Exception
    {
        MavenBuild build = createBuild("/invalidXml");

        assertThrows(ExecutorException.class, () -> {
            build.execute("clean", "package");
        });

        assertThat(build.getLog(), containsString("Error while creating XAR file"));
        assertThat(build.getLog(), containsString("Content doesn't point to valid wiki page XML"));
        assertThat(build.getLog(), containsString("Failed to parse"));
    }
}
