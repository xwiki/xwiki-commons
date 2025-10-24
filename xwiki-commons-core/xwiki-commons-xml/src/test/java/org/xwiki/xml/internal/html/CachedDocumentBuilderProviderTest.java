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

package org.xwiki.xml.internal.html;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CachedDocumentBuilderProvider}.
 *
 * @version $Id$
 */
@ComponentTest
class CachedDocumentBuilderProviderTest
{
    @InjectMockComponents
    private CachedDocumentBuilderProvider provider;

    @MockComponent
    private Execution execution;

    @Test
    void getAvailableDocumentBuilderWithExecutionContextCachesBuilder() throws Exception
    {
        ExecutionContext econtext = new ExecutionContext();
        when(this.execution.getContext()).thenReturn(econtext);

        DocumentBuilder first = this.provider.getAvailableDocumentBuilder();
        assertNotNull(first);
        Object prop = econtext.getProperty(DocumentBuilder.class.getName());
        assertSame(first, prop);

        DocumentBuilder second = this.provider.getAvailableDocumentBuilder();
        assertSame(first, second);
    }

    @Test
    void getAvailableDocumentBuilderWithExistingBuilderReturnsSame() throws Exception
    {
        ExecutionContext econtext = new ExecutionContext();
        DocumentBuilder mockBuilder = mock(DocumentBuilder.class);
        econtext.setProperty(DocumentBuilder.class.getName(), mockBuilder);
        when(this.execution.getContext()).thenReturn(econtext);

        DocumentBuilder got = this.provider.getAvailableDocumentBuilder();
        assertSame(mockBuilder, got);
    }

    @Test
    void getAvailableDocumentBuilderWithoutExecutionContextReturnsNewEachCall() throws Exception
    {
        when(this.execution.getContext()).thenReturn(null);

        DocumentBuilder a = this.provider.getAvailableDocumentBuilder();
        DocumentBuilder b = this.provider.getAvailableDocumentBuilder();

        assertNotNull(a);
        assertNotNull(b);
        assertNotSame(a, b);
    }

    @Test
    void externalDtdIsNotLoadedAndEntityIsNotResolved() throws Exception
    {
        // Given a DOCTYPE pointing to an external DTD that would define a default attribute on <root>
        // We use a data: URL to avoid any network access. With external DTD loading enabled, this actually even
        // fails to load, at least with Xerces.
        String encodedDtd = "%3C!ELEMENT%20root%20EMPTY%3E%3C!ATTLIST%20root%20att%20CDATA%20%22default%22%3E";
        String xml = "<!DOCTYPE root SYSTEM \"data:application/xml," + encodedDtd + "\">\n<root/>";

        ExecutionContext econtext = new ExecutionContext();
        when(this.execution.getContext()).thenReturn(econtext);
        DocumentBuilder builder = this.provider.getAvailableDocumentBuilder();

        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        Element root = doc.getDocumentElement();

        // Then: since loading external DTDs is disabled, the default attribute should NOT be applied
        assertNotNull(root);
        assertFalse(root.hasAttribute("att"));
    }

    @Test
    void billionLaughsEntityExpansionIsPreventedBySecureProcessing() throws Exception
    {
        // Build a deeply nested entity expansion chain that would explode if fully expanded.
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE lolz [\n");
        sb.append(" <!ENTITY e0 \"a\">\n");
        for (int i = 1; i <= 7; i++) {
            sb.append(" <!ENTITY e").append(i).append(" \"");
            for (int j = 0; j < 10; j++) {
                sb.append("&e").append(i - 1).append(";");
            }
            sb.append("\">\n");
        }
        sb.append("]>\n<lolz>&e7;</lolz>");
        String xml = sb.toString();

        ExecutionContext econtext = new ExecutionContext();
        when(this.execution.getContext()).thenReturn(econtext);
        DocumentBuilder builder = this.provider.getAvailableDocumentBuilder();

        // Prevent the parser from printing errors to the console by setting a strict ErrorHandler
        builder.setErrorHandler(new ErrorHandler()
        {
            @Override
            public void warning(SAXParseException exception) throws SAXException
            {
                throw exception;
            }

            @Override
            public void error(SAXParseException exception) throws SAXException
            {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException
            {
                throw exception;
            }
        });

        // With FEATURE_SECURE_PROCESSING enabled, excessive entity expansion should be rejected
        assertThrows(SAXParseException.class, () ->
            builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
    }
}
