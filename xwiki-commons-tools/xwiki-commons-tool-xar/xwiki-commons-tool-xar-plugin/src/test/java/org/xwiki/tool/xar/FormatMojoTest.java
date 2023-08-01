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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.xwiki.tool.xar.internal.XMLUtils.getSAXReader;

/**
 * Unit tests for {@link FormatMojo}.
 *
 * @version $Id$
 * @since 5.4.1
 */
class FormatMojoTest
{
    @Test
    void defaultLanguageForDefaultDocumentWhenTranslation()
    {
        FormatMojo mojo = new FormatMojo();
        mojo.defaultLanguage = "en";

        File file = new File("Some/Space/Document.xml");
        List<File> files = Arrays.asList(
            new File("Some/Space/Document.xml"),
            new File("Some/Space/Document.fr.xml"));

        assertEquals(Locale.ENGLISH, mojo.guessDefaultLocale(file, files));
    }

    @Test
    void defaultLanguageForNonMatchingContentPage()
    {
        FormatMojo mojo = new FormatMojo();
        mojo.defaultLanguage = "en";
        mojo.contentPages = Arrays.asList("Document\\.xml");
        mojo.initializePatterns();

        File file = new File("Some/Space/Document.xml");

        assertEquals(Locale.ROOT, mojo.guessDefaultLocale(file, Collections.emptyList()));
    }

    @Test
    void defaultLanguageForMatchingContentPage()
    {
        FormatMojo mojo = new FormatMojo();
        mojo.defaultLanguage = "en";
        mojo.contentPages = Arrays.asList(".*/Document\\.xml");
        mojo.initializePatterns();

        File file = new File("Some/Space/Document.xml");

        assertEquals(Locale.ENGLISH, mojo.guessDefaultLocale(file, Collections.emptyList()));
    }

    @Test
    void defaultLanguageForTranslatedDocument()
    {
        FormatMojo mojo = new FormatMojo();
        mojo.defaultLanguage = "en";

        File file = new File("Some/Space/Document.fr.xml");

        assertEquals(Locale.ENGLISH, mojo.guessDefaultLocale(file, Collections.emptyList()));
    }

    @Test
    void defaultLanguageForDocumentWhenNoTranslation()
    {
        FormatMojo mojo = new FormatMojo();
        mojo.defaultLanguage = "en";

        File file = new File("Some/Space/Document.xml");
        List<File> files = Arrays.asList(new File("Some/OtherSpace/Other.xml"));

        assertEquals(Locale.ROOT, mojo.guessDefaultLocale(file, files));
    }

    /**
     * Reproduces issues raised in <a href="https://jira.xwiki.org/browse/XCOMMONS-1833">XCOMMONS-1833</a>.
     */
    @Test
    void defaultLanguageForDocumentWhenNoTranslationButFileWithSameNameInOtherSpace()
    {
        FormatMojo mojo = new FormatMojo();
        mojo.defaultLanguage = "en";

        File file = new File("Space1/Document.xml");
        // Simulate a page with the same name and with a translation but in a different space.
        List<File> files = Arrays.asList(
            new File("Space2/Document.xml"),
            new File("Space2/Document.fr.xml"));

        assertEquals(Locale.ROOT, mojo.guessDefaultLocale(file, files));
    }

    /**
     * Reproduces issue raised in <a href="https://jira.xwiki.org/browse/XCOMMONS-1373">XCOMMONS-1373</a>.
     */
    @Test
    void formatSpecialContentFailingWithXercesFromJDK8() throws Exception
    {
        SAXReader reader = getSAXReader();
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("XWikiSyntaxLinks.it.xml");
        String expectedContent = IOUtils.toString(is, "UTF-8");

        is = Thread.currentThread().getContextClassLoader().getResourceAsStream("XWikiSyntaxLinks.it.xml");
        Document domdoc = reader.read(is);

        XWikiXMLWriter writer;
        OutputFormat format = new OutputFormat("  ", true, "UTF-8");
        format.setExpandEmptyElements(false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer = new XWikiXMLWriter(baos, format);
        writer.setVersion("1.1");
        writer.write(domdoc);
        writer.close();

        assertEquals(expectedContent, baos.toString());
    }
}
