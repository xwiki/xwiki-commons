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
package org.xwiki.xml.internal.html.filter;

import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.xml.html.DefaultHTMLElementSanitizerComponentList;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link SanitizerFilter}.
 *
 * @version $Id$
 */
@ComponentTest
@DefaultHTMLElementSanitizerComponentList
class SanitizerFilterTest
{
    @InjectMockComponents
    private SanitizerFilter sanitizerFilter;

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        // SVG elements stay SVG elements inside SVG, other elements are removed.
        "<svg><g><circle></circle><p>x</p></g></svg> | <svg><g><circle></circle></g></svg>",
        "<svg><a><title>t</title></a></svg> | <svg><a><title>t</title></a></svg>",
        // Children of SVG HTML integration points are HTML elements, which aren't allowed in SVG.
        "<svg><desc><p>x</p><circle></circle></desc></svg> | <svg><desc></desc></svg>",
        "<svg><title><math></math></title></svg> | <svg><title></title></svg>",
        "<svg><title><svg><circle></circle></svg></title></svg> "
            + "| <svg><title><svg><circle></circle></svg></title></svg>",
        // Children of MathML text integration points are HTML elements, except for mglyph and malignmark.
        "<math><mi><span>x</span><mglyph></mglyph></mi></math> | <math><mi><span>x</span><mglyph></mglyph></mi></math>",
        "<math><mi><mi>x</mi></mi></math> | <math><mi></mi></math>",
        "<math><mtext><svg><circle></circle></svg></mtext></math> "
            + "| <math><mtext><svg><circle></circle></svg></mtext></math>",
        "<math><mi><svg><desc><span>x</span></desc></svg></mi></math> | <math><mi><svg><desc></desc></svg></mi></math>",
        // Other MathML elements don't allow HTML children.
        "<math><mrow><span>x</span><mn>1</mn></mrow></math> | <math><mrow><mn>1</mn></mrow></math>",
        // SVG and MathML elements can't be used in HTML.
        "<div><circle></circle><mi>x</mi>y</div> | <div>y</div>"
    })
    void namespaces(String input, String expected) throws Exception
    {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(new InputSource(new StringReader("<html>" + input + "</html>")));

        this.sanitizerFilter.filter(document, Map.of(HTMLCleanerConfiguration.RESTRICTED, "true"));

        assertEquals("<html>" + expected + "</html>", HTMLUtils.toString(document, true, true).trim());
    }
}
