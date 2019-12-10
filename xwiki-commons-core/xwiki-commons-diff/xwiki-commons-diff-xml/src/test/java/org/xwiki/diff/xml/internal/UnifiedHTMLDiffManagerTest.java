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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xwiki.diff.xml.StringSplitter;
import org.xwiki.diff.xml.XMLDiffConfiguration;
import org.xwiki.diff.xml.XMLDiffFilter;
import org.xwiki.diff.xml.XMLDiffManager;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link UnifiedHTMLDiffManager}.
 * 
 * @version $Id$
 */
@ComponentTest
@AllComponents
public class UnifiedHTMLDiffManagerTest
{
    private UnifiedHTMLDiffManager unifiedHTMLDiffManager;

    private XMLDiffConfiguration config;

    private XMLDiffFilter htmlDiffPruner;

    private StringSplitter wordSplitter;

    @MockComponent
    private DataURIConverter dataURIConverter;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    private Map<String, String> images = new HashMap<>();

    @BeforeEach
    public void configure(MockitoComponentManager componentManager) throws Exception
    {
        this.unifiedHTMLDiffManager = componentManager.getInstance(XMLDiffManager.class, "html/unified");
        this.config = componentManager.getInstance(XMLDiffConfiguration.class, "html");
        this.htmlDiffPruner = componentManager.getInstance(XMLDiffFilter.class, "html/pruner");
        this.wordSplitter = componentManager.getInstance(StringSplitter.class, "word");

        this.images.put("alice.png", "data:image/png;base64,ABCD");
        this.images.put("images/alice.png", "data:image/png;base64,ABCD");
        this.images.put("bob.png", "data:image/png;base64,DCBA");
        this.images.put("images/bob.png", "data:image/png;base64,DCBA");

        when(this.dataURIConverter.convert(any(String.class))).thenAnswer(new Answer<String>()
        {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                return images.getOrDefault(invocation.getArgument(0), invocation.getArgument(0));
            }
        });
    }

    @ParameterizedTest
    @MethodSource("getTestFiles")
    public void verifyHTMLDiffMarker(File testFile) throws Exception
    {
        Map<String, String> testData = getTestData(testFile);
        if (testData.containsKey("expected-marker")) {
            this.config.getFilters().remove(this.htmlDiffPruner);
            String expectedHTML = testData.get("expected-marker");
            String actualHTML = this.unifiedHTMLDiffManager.diff(testData.get("left"), testData.get("right"), config);
            assertEquals(expectedHTML, actualHTML);
        }
    }

    @ParameterizedTest
    @MethodSource("getTestFiles")
    public void verifyHTMLDiffMarkerWithWordSplitter(File testFile) throws Exception
    {
        Map<String, String> testData = getTestData(testFile);
        if (testData.containsKey("expected-marker-word")) {
            this.config.getFilters().remove(this.htmlDiffPruner);
            ((DefaultXMLDiffConfiguration) this.config).setSplitterForNodeType(Node.TEXT_NODE, this.wordSplitter);
            String expectedHTML = testData.get("expected-marker-word");
            String actualHTML = this.unifiedHTMLDiffManager.diff(testData.get("left"), testData.get("right"), config);
            assertEquals(expectedHTML, actualHTML);
        }
    }

    @Test
    public void verifyXMLDiffFiltersThrowingExceptions() throws Exception
    {
        XMLDiffFilter alice = mock(XMLDiffFilter.class, "alice");
        doThrow(new RuntimeException("before alice failed!")).when(alice).before(any(Document.class));
        doThrow(new RuntimeException("after alice failed!")).when(alice).after(any(Document.class));

        XMLDiffFilter bob = mock(XMLDiffFilter.class, "bob");
        this.config.getFilters().add(0, alice);
        this.config.getFilters().add(bob);

        this.unifiedHTMLDiffManager.diff("<p>one</p>", "<p>two</p>", this.config);

        assertEquals("Failed to apply filter before diff.", this.logCapture.getMessage(0));
        assertEquals("Failed to apply filter before diff.", this.logCapture.getMessage(1));
        assertEquals("Failed to apply filter after diff.", this.logCapture.getMessage(2));

        verify(bob, times(2)).before(any(Document.class));
        verify(bob).after(any(Document.class));
    }

    @ParameterizedTest
    @MethodSource("getTestFiles")
    public void verifyHTMLDiffPruner(File testFile) throws Exception
    {
        Map<String, String> testData = getTestData(testFile);
        if (testData.containsKey("expected-pruner")) {
            String expectedHTML = testData.get("expected-pruner");
            String actualHTML = this.unifiedHTMLDiffManager.diff(testData.get("left"), testData.get("right"), config);
            assertEquals(expectedHTML, actualHTML);
        }
    }

    public static Stream<File> getTestFiles() throws Exception
    {
        File inputFolder = new File("src/test/resources/html");
        return Stream.of(inputFolder.listFiles(file -> file.getName().endsWith(".test")));
    }

    private Map<String, String> getTestData(File file) throws Exception
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
                if (key != null && key.startsWith("expected-")) {
                    // Remove whitespace at the start of the line (in order to ignore formatting).
                    line = StringUtils.stripStart(line, null);
                    // Remove line ending (in order to ignore formatting).
                    line = StringUtils.chomp(line);
                }
                data.append(line);
            }
        }
        if (key != null) {
            testData.put(key, data.toString());
        }
        return testData;
    }
}
