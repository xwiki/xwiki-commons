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
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.xwiki.diff.xml.XMLDiffMarker;
import org.xwiki.diff.xml.XMLDiffPruner;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.xml.html.HTMLUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link HTMLDiffPruner}.
 * 
 * @version $Id$
 */
@ComponentTest
@AllComponents
public class HTMLDiffPrunerTest extends AbstractHTMLDiffTest
{
    private HTMLDiffMarker htmlDiffMarker;

    private HTMLDiffPruner htmlDiffPruner;

    @BeforeEach
    @Override
    public void configure(MockitoComponentManager componentManager) throws Exception
    {
        super.configure(componentManager);

        componentManager.registerComponent(DefaultXMLDiff.class);
        componentManager.registerComponent(HTMLDiffMarker.class);
        componentManager.registerComponent(HTMLDiffPruner.class);

        this.htmlDiffMarker = componentManager.getInstance(XMLDiffMarker.class, "html");
        this.htmlDiffPruner = componentManager.getInstance(XMLDiffPruner.class, "html");
    }

    @ParameterizedTest
    @MethodSource("getTestFiles")
    public void verifyHTMLDiffPruner(File testFile) throws Exception
    {
        Map<String, String> testData = getTestData(testFile);
        if (testData.containsKey("expected-pruner")) {
            Document left = parseHTML(testData.get("left"));
            assertTrue(this.htmlDiffMarker.markDiff(left, parseHTML(testData.get("right"))));
            this.htmlDiffPruner.prune(left);
            String expectedHTML = testData.get("expected-pruner");
            String actualHTML = HTMLUtils.toString(left, true, true).trim();
            String prefix = "<html><head data-xwiki-html-diff-hidden=\"true\"></head><body>";
            String suffix = "</body></html>";
            assertEquals(prefix + expectedHTML + suffix, actualHTML);
        }
    }
}
