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
package org.xwiki.filter.xml.internal.parser;

import java.io.StringReader;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.xwiki.filter.test.TestFilter;
import org.xwiki.filter.xml.parser.XMLParserFactory;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link DefaultXMLParser}.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class DefaultXMLParserTest
{
    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    private final TestFilter filter = mock();

    private void parse(String xml) throws Exception
    {
        XMLParserFactory parserFactory = this.componentManager.getInstance(XMLParserFactory.class);
        parserFactory.parse(new StreamSource(new StringReader(xml)), this.filter, null);
    }

    @Test
    void severalChildrenInContainer() throws Exception
    {
        parse("<container><child/><child/></container>");

        InOrder inOrder = inOrder(this.filter);
        // The begin event of the container is sent only once, before the first child.
        inOrder.verify(this.filter).beginContainer();
        inOrder.verify(this.filter, times(2)).onChild();
        inOrder.verify(this.filter).endContainer();
        verifyNoMoreInteractions(this.filter);
    }

    @Test
    void childWithContentAsFirstParameter() throws Exception
    {
        parse("<childWithParameters>value</childWithParameters>");

        verify(this.filter).onChildWithParameters("value", 0);
    }

    @Test
    void childWithEmptyContent() throws Exception
    {
        parse("<childWithParameters/>");

        verify(this.filter).onChildWithParameters(null, 0);
    }

    @Test
    void childWithContentIgnoredWhenParametersAreSet() throws Exception
    {
        parse("<childWithParameters param1=\"2\">value</childWithParameters>");

        verify(this.filter).onChildWithParameters(null, 2);
    }

    @Test
    void parametersElementWithTextNodes() throws Exception
    {
        parse("""
            <containerWithMap><p>
              <map><entry><string>key</string><int>1</int></entry></map>
            </p></containerWithMap>""");

        verify(this.filter).beginContainerWithMap(Map.of("key", 1));
        verify(this.filter).endContainerWithMap(Map.of("key", 1));
    }

    @Test
    void parametersElementOfUnknownElement() throws Exception
    {
        parse("<unknown><p><param>value</param></p></unknown>");

        // The filter does not support unknown elements so nothing is sent.
        verifyNoInteractions(this.filter);
        assertEquals("Unknown filter element [unknown]", this.logCapture.getMessage(0));
    }
}
