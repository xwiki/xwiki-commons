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

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.xwiki.diff.Delta;
import org.xwiki.diff.Patch;
import org.xwiki.diff.xml.XMLDiffConfiguration;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.xml.XMLUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link DefaultXMLDiff}.
 *
 * @version $Id$
 */
@ComponentTest
public class DefaultXMLDiffTest
{
    @InjectMockComponents
    private DefaultXMLDiff defaultXMLDiff;

    @MockComponent
    private XMLDiffConfiguration config;

    private Document document;

    @BeforeEach
    public void setUp() throws Exception
    {
        DOMImplementationLS lsImpl =
            (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS 3.0");
        LSInput input = lsImpl.createLSInput();
        input.setStringData("<root><child/>text</root>");
        this.document = XMLUtils.parse(input);
    }

    @Test
    void nonSimilarNodeInsert() throws Exception
    {
        Node text = this.document.getDocumentElement().getLastChild();
        Map<Node, Patch<?>> patches = this.defaultXMLDiff.diff(null, text, this.config);
        assertEquals(1, patches.size());

        Patch<?> patch = patches.get(null);
        assertEquals(1, patch.size());

        Delta<?> delta = patch.get(0);
        assertEquals(Delta.Type.INSERT, delta.getType());
        assertEquals(Collections.emptyList(), delta.getPrevious().getElements());
        assertEquals(Collections.singletonList(text), delta.getNext().getElements());
    }

    @Test
    void nonSimilarNodeDelete() throws Exception
    {
        Node text = this.document.getDocumentElement().getLastChild();
        Map<Node, Patch<?>> patches = this.defaultXMLDiff.diff(text, null, this.config);
        assertEquals(1, patches.size());

        Patch<?> patch = patches.get(null);
        assertEquals(1, patch.size());

        Delta<?> delta = patch.get(0);
        assertEquals(Delta.Type.DELETE, delta.getType());
        assertEquals(Collections.singletonList(text), delta.getPrevious().getElements());
        assertEquals(Collections.emptyList(), delta.getNext().getElements());
    }

    @Test
    void nonSimilarNodeChange() throws Exception
    {
        Node child = this.document.getDocumentElement().getFirstChild();
        Node text = this.document.getDocumentElement().getLastChild();
        Map<Node, Patch<?>> patches = this.defaultXMLDiff.diff(child, text, this.config);
        assertEquals(1, patches.size());

        Patch<?> patch = patches.get(null);
        assertEquals(1, patch.size());

        Delta<?> delta = patch.get(0);
        assertEquals(Delta.Type.CHANGE, delta.getType());
        assertEquals(Collections.singletonList(child), delta.getPrevious().getElements());
        assertEquals(Collections.singletonList(text), delta.getNext().getElements());
    }
}
