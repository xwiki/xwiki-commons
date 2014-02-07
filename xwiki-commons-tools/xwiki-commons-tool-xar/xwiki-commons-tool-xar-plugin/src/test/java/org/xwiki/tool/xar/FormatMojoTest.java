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
import java.util.Collections;
import java.util.List;

import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link org.xwiki.tool.xar.FormatMojo}.
 *
 * @version $Id$
 * @since 5.4.1
 */
public class FormatMojoTest
{
    @Test
    public void defaultLanguageForDefaultDocumentWhenTranslation()
    {
        FormatMojo mojo = new FormatMojo();
        mojo.defaultLanguage = "en";

        File file = new File("Document.xml");
        List<File> files = Arrays.asList(
            new File("Document.xml"),
            new File("Document.fr.xml"));

        assertEquals("en", mojo.guessDefaultLanguage(file, files));
    }

    @Test
    public void defaultLanguageForTranslatedDocument()
    {
        FormatMojo mojo = new FormatMojo();
        mojo.defaultLanguage = "en";

        File file = new File("Document.fr.xml");
        List<File> files = Collections.EMPTY_LIST;

        assertEquals("en", mojo.guessDefaultLanguage(file, files));
    }

    @Test
    public void defaultLanguageForDocumentWhenNoTranslation()
    {
        FormatMojo mojo = new FormatMojo();
        mojo.defaultLanguage = "en";

        File file = new File("Document.xml");
        List<File> files = Arrays.asList(new File("Other.xml"));

        assertEquals("", mojo.guessDefaultLanguage(file, files));
    }
}
