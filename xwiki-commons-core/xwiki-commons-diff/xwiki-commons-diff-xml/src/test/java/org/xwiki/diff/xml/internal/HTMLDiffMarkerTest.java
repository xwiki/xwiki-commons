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
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.xml.html.HTMLUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link HTMLDiffMarker}.
 * 
 * @version $Id$
 */
@ComponentTest
@AllComponents
public class HTMLDiffMarkerTest extends AbstractHTMLDiffTest
{
    private HTMLDiffMarker htmlDiffMarker;

    @BeforeEach
    @Override
    public void configure(MockitoComponentManager componentManager) throws Exception
    {
        super.configure(componentManager);

        componentManager.registerComponent(DefaultXMLDiff.class);
        componentManager.registerComponent(HTMLDiffMarker.class);

        this.htmlDiffMarker = componentManager.getInstance(XMLDiffMarker.class, "html");
    }

    @ParameterizedTest
    @MethodSource("getTestFiles")
    public void verifyHTMLDiffMarker(File testFile) throws Exception
    {
        Map<String, String> testData = getTestData(testFile);
        if (testData.containsKey("expected-marker")) {
            Document left = parseHTML(testData.get("left"));
            assertTrue(this.htmlDiffMarker.markDiff(left, parseHTML(testData.get("right"))));
            String expectedHTML = xhtmlFragment(testData.get("expected-marker"));
            String actualHTML = HTMLUtils.toString(left, false, false).trim();
            assertEquals(expectedHTML, actualHTML);
        }
    }
}
