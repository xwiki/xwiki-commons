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
package org.xwiki.diff.xml.internal;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.xml.html.HTMLCleaner;

/**
 * Base class for integration tests in this module.
 * 
 * @version $Id$
 */
public abstract class AbstractHTMLDiffTest
{
    private HTMLCleaner htmlCleaner;

    @BeforeEach
    public void configure(MockitoComponentManager componentManager) throws Exception
    {
        this.htmlCleaner = componentManager.getInstance(HTMLCleaner.class);
    }

    public static Stream<File> getTestFiles() throws Exception
    {
        File inputFolder = new File("src/test/resources/html");
        return Stream.of(inputFolder.listFiles(file -> file.getName().endsWith(".test")));
    }

    protected Map<String, String> getTestData(File file) throws Exception
    {
        Map<String, String> testData = new HashMap<>();
        List<String> lines = IOUtils.readLines(new FileReader(file));
        String key = null;
        StringBuilder data = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("## ")) {
                // Store previous data.
                if (key != null) {
                    testData.put(key, data.toString());
                }
                // Prepare next data.
                key = line.substring(3);
                data.delete(0, data.length());
            } else if (!line.startsWith("##-")) {
                data.append(line);
            }
        }
        if (key != null) {
            testData.put(key, data.toString());
        }
        return testData;
    }

    protected Document parseHTML(String html)
    {
        return this.htmlCleaner.clean(new StringReader(xhtmlFragment(html)));
    }

    /**
     * Adds the XHTML envelope to the given XHTML fragment.
     * 
     * @param fragment the content to be placed inside the {@code body} tag
     * @return the given XHTML fragment wrapped in the XHTML envelope
     */
    protected String xhtmlFragment(String fragment)
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
            + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + "<html><head></head><body>" + fragment
            + "</body></html>";
    }
}
