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
package org.xwiki.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Convert SAX events into a Document.
 *
 * @version $Id$
 */
public class Sax2Dom implements ContentHandler, LexicalHandler
{
    /**
     * "xml" namespace prefix.
     */
    public static final String XML_PREFIX = "xml";

    /**
     * "xmlns" namespace prefix.
     */
    public static final String XMLNS_PREFIX = "xmlns";

    /**
     * "xmlns" namespace prefix with :.
     */
    public static final String XMLNS_STRING = XMLNS_PREFIX + ':';

    /**
     * "xmlns" URL.
     */
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    /**
     * The root node.
     */
    private final Node rootNode;

    /**
     * The document used to create new nodes.
     */
    private final Document document;

    /**
     * The current nodes.
     */
    private final Stack<Node> nodes = new Stack<>();

    /**
     * The namespaces declarations.
     */
    private List<String> namespaceDecls;

    /**
     * Default constructor.
     *
     * @throws ParserConfigurationException failed to create a new {@link Document}
     */
    public Sax2Dom() throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        this.document = factory.newDocumentBuilder().newDocument();
        this.rootNode = this.document;
    }

    /**
     * @param root the root node to fill with SAX events
     * @throws ParserConfigurationException failed to create a new {@link Document}
     */
    public Sax2Dom(Node root) throws ParserConfigurationException
    {
        if (root instanceof Document) {
            this.document = (Document) root;
            this.rootNode = root;
        } else if (root != null) {
            this.document = root.getOwnerDocument();
            this.rootNode = root;
        } else {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            this.document = factory.newDocumentBuilder().newDocument();
            this.rootNode = this.document;
        }
    }

    /**
     * @return the root {@link Node}
     */
    public Node getRootNode()
    {
        return this.rootNode;
    }

    /**
     * @return the root {@link Element}
     */
    public Element getRootElement()
    {
        Element rootElement;

        if (this.rootNode instanceof Element) {
            rootElement = (Element) this.rootNode;
        } else if (this.rootNode instanceof Document) {
            rootElement = ((Document) this.rootNode).getDocumentElement();
        } else {
            rootElement = null;
        }

        return rootElement;
    }

    @Override
    public void characters(char[] ch, int start, int length)
    {
        Node currentNode = this.nodes.peek();

        // No text nodes can be children of root (DOM006 exception)
        if (currentNode != this.document) {
            final String text = String.valueOf(ch, start, length);
            currentNode.appendChild(this.document.createTextNode(text));
        }
    }

    @Override
    public void startDocument()
    {
        this.nodes.push(this.rootNode);
    }

    @Override
    public void endDocument()
    {
        this.nodes.pop();
    }

    @Override
    public void startElement(String namespace, String localName, String qName, Attributes atts)
    {
        final Element element = this.document.createElementNS(namespace, qName);

        // Add namespace declarations first
        if (this.namespaceDecls != null) {
            final int nDecls = this.namespaceDecls.size();
            for (int i = 0; i < nDecls; i += 2) {
                String prefix = this.namespaceDecls.get(i);
                String uri = this.namespaceDecls.get(i + 1);

                if (StringUtils.isEmpty(prefix)) {
                    element.setAttributeNS(XMLNS_URI, XMLNS_PREFIX, uri);
                } else {
                    element.setAttributeNS(XMLNS_URI, XMLNS_STRING + prefix, uri);
                }
            }
            this.namespaceDecls.clear();
        }

        // Add attributes to element
        final int nattrs = atts.getLength();
        for (int i = 0; i < nattrs; i++) {
            if (atts.getLocalName(i) == null) {
                element.setAttribute(atts.getQName(i), atts.getValue(i));
            } else {
                element.setAttributeNS(atts.getURI(i), atts.getQName(i), atts.getValue(i));
            }
        }

        // Append this new node onto current stack node
        this.nodes.peek().appendChild(element);

        // Push this node onto stack
        this.nodes.push(element);
    }

    @Override
    public void endElement(String namespace, String localName, String qName)
    {
        this.nodes.pop();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
    {
        if (this.namespaceDecls == null) {
            this.namespaceDecls = new ArrayList<>(2);
        }
        this.namespaceDecls.add(prefix);
        this.namespaceDecls.add(uri);
    }

    @Override
    public void endPrefixMapping(String prefix)
    {

    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
    {
    }

    @Override
    public void processingInstruction(String target, String data)
    {
        ProcessingInstruction pi = this.document.createProcessingInstruction(target, data);
        if (pi != null) {
            this.nodes.peek().appendChild(pi);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {
    }

    @Override
    public void skippedEntity(String name)
    {
    }

    @Override
    public void comment(char[] ch, int start, int length)
    {
        Comment comment = this.document.createComment(String.valueOf(ch, start, length));
        if (comment != null) {
            this.nodes.peek().appendChild(comment);
        }
    }

    // Lexical Handler methods- not implemented

    @Override
    public void startCDATA()
    {
    }

    @Override
    public void endCDATA()
    {
    }

    @Override
    public void startEntity(java.lang.String name)
    {
    }

    @Override
    public void endEntity(String name)
    {
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException
    {
    }

    @Override
    public void endDTD()
    {
    }
}
