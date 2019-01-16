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
package org.htmlcleaner;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * Generate a W3C Document from a SF's HTML Cleaner TagNode.
 *
 * Some code has been copy-pasted from SF's HTML Cleaner code (which is under a BDS license, see
 * http://htmlcleaner.sourceforge.net/license.php). Our goal is to remove this class completely if we can get SF's HTML
 * Cleaner to support the usage of a dedicated builder.
 *
 * Here's the reason why we want to be able to give a dedicated builder:
 * Note that creating the DocumentBuilder is not super fast but it's specifically more about the DocumentBuilderFactory
 * creation mainly because it's blocking all the threads which are doing stuff implying loading a class from the
 * classloader making it an important lock contention. I modified its behavior (and other similar tasks) after noticing
 * that there was often a bunch of threads waiting for this kind of lock.
 *
 * @version $Id$
 * @since 1.8.2
 */
public class XWikiDOMSerializer extends DomSerializer
{
    /**
     * @param props the HTML Cleaner properties set by the user to control the HTML cleaning.
     */
    public XWikiDOMSerializer(CleanerProperties props)
    {
        super(props);
    }

    /**
     * This method is an exact copy of {@link DomSerializer#createDocument(TagNode)} except that the {@link DocumentBuilder}
     * is given in parameter.
     * @param builder the {@link DocumentBuilder} instance to use, DocumentBuilder is not guaranteed to
     * be thread safe so at most the safe instance should be used only in the same thread
     * @param rootNode the HTML Cleaner root node to serialize
     * @return the W3C Document object
     */
    private Document createDocument(DocumentBuilder builder, TagNode rootNode)
    {
        DOMImplementation impl = builder.getDOMImplementation();

        Document document;

        //
        // Where a DOCTYPE is supplied in the input, ensure that this is in the output DOM. See issue #27
        //
        // Note that we may want to fix incorrect DOCTYPEs in future; there are some fairly
        // common patterns for errors with the older HTML4 doctypes.
        //
        if (rootNode.getDocType() != null){
            String qualifiedName = rootNode.getDocType().getPart1();
            String publicId = rootNode.getDocType().getPublicId();
            String systemId = rootNode.getDocType().getSystemId();

            //
            // If there is no qualified name, set it to html. See bug #153.
            //
            if (qualifiedName == null) qualifiedName = "html";

            DocumentType documentType = impl.createDocumentType(qualifiedName, publicId, systemId);

            //
            // While the qualified name is "HTML" for some DocTypes, we want the actual document root name to be "html". See bug #116
            //
            if (qualifiedName.equals("HTML")) qualifiedName = "html";
            document = impl.createDocument(rootNode.getNamespaceURIOnPath(""), qualifiedName, documentType);
        } else {
            document = builder.newDocument();
            Element rootElement = document.createElement(rootNode.getName());
            document.appendChild(rootElement);
        }

        //
        // Turn off error checking if we're allowing invalid attribute names, or if we've chosen to turn it off
        //
        if (props.isAllowInvalidAttributeNames() || strictErrorChecking == false){
            document.setStrictErrorChecking(false);
        }


        //
        // Copy across root node attributes - see issue 127. Thanks to rasifiel for the patch
        //
        Map<String, String> attributes =  rootNode.getAttributes();
        Iterator<Map.Entry<String, String>> entryIterator = attributes.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            String attrName = entry.getKey();
            String attrValue = entry.getValue();

            //
            // Fix any invalid attribute names
            //
            if (!props.isAllowInvalidAttributeNames()){
                attrName = Utils.sanitizeXmlAttributeName(attrName, props.getInvalidXmlAttributeNamePrefix());
            }

            if (attrName != null && (Utils.isValidXmlIdentifier(attrName) || props.isAllowInvalidAttributeNames())){

                if (escapeXml) {
                    attrValue = Utils.escapeXml(attrValue, props, true);
                }

                document.getDocumentElement().setAttribute(attrName, attrValue);

                //
                // Flag the attribute as an ID attribute if appropriate. Thanks to Chris173
                //
                if (attrName.equalsIgnoreCase("id")) {
                    document.getDocumentElement().setIdAttribute(attrName, true);
                }
            }

        }
        return document;
    }

    /**
     * Create the DOM given a rootNode and a document builder.
     * This method is a replica of {@link DomSerializer#createDOM(TagNode)} excepts that it requires to give a
     * DocumentBuilder.
     * @param documentBuilder the {@link DocumentBuilder} instance to use, DocumentBuilder is not guaranteed to
     * be thread safe so at most the safe instance should be used only in the same thread
     * @param rootNode the HTML Cleaner root node to serialize
     * @return the W3C Document object
     * @throws ParserConfigurationException if there's an error during serialization
     */
    public Document createDOM(DocumentBuilder documentBuilder, TagNode rootNode) throws ParserConfigurationException
    {
        Document document = createDocument(documentBuilder, rootNode);

        createSubnodes(document, (Element)document.getDocumentElement(), rootNode.getAllChildren());

        return document;
    }
}
