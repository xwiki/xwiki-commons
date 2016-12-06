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
package org.xwiki.tool.xar.internal;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Parse XWiki document from XML.
 *
 * @version $Id$
 */
public class XWikiDocument
{
    private static final String AUTHOR_TAG = "author";

    /**
     * @see #getReference()
     * @since 7.2M1
     */
    private String reference;

    /**
     * @see #getLocale()
     * @since 7.2M1
     */
    private String locale;

    /**
     * @see #getDefaultLanguage()
     */
    private String defaultLanguage;

    /**
     * @see #getCreator()
     */
    private String creator;

    /**
     * @see #getAuthor()
     */
    private String author;

    /**
     * @see #getContentAuthor()
     */
    private String contentAuthor;

    /**
     * @see #getAttachmentAuthors()
     * @since 7.0RC1
     */
    private List<String> attachmentAuthors;

    /**
     * @see #getVersion()
     */
    private String version;

    /**
     * @see #getParent()
     */
    private String parent;

    /**
     * @see #getComment()
     */
    private String comment;

    /**
     * @see #getMinorEdit()
     */
    private String minorEdit;

    /**
     * @see #getEncoding()
     */
    private String encoding;

    /**
     * @see #isHidden()
     */
    private boolean isHidden;

    /**
     * @see #getTitle()
     */
    private String title;

    /**
     * @see #getSyntaxId()
     */
    private String syntaxId;

    /**
     * @see #containsTranslations()
     */
    private boolean containsTranslations;

    /**
     * Parse XML file to extract document information.
     *
     * @param file the xml file
     * @throws DocumentException error when parsing XML file
     */
    public void fromXML(File file) throws DocumentException
    {
        SAXReader reader = new SAXReader();
        fromXML(reader.read(file));
        
    }

    /**
     * Parse XML file to extract document information.
     *
     * @param file the xml file
     * @throws DocumentException error when parsing XML file
     */
    public void fromXML(String file) throws DocumentException
    {
        SAXReader reader = new SAXReader();
        fromXML(reader.read(new StringReader(file)));
    }

    /**
     * Parse XML document to extract document information.
     *
     * @param domdoc the xml document
     * @throws DocumentException error when parsing XML file
     */
    public void fromXML(Document domdoc) throws DocumentException
    {
        this.encoding = domdoc.getXMLEncoding();

        Element rootElement = domdoc.getRootElement();

        this.reference = rootElement.attributeValue("reference");
        if (this.reference == null) {
            String name = readElement(rootElement, "name");
            String space = readElement(rootElement, "web");

            // If the reference, name and space don't exist we consider that we're not readin an XMKL that corresponds
            // to a wiki page.
            if (name == null && space == null) {
                throw new DocumentException(String.format("Content doesn't point to valid wiki page XML",
                    domdoc.getName()));
            }

            this.reference = space == null ? name : escapeSpaceOrPageName(space) + '.' + escapeSpaceOrPageName(name);
        }

        this.locale = rootElement.attributeValue("locale");
        if (this.locale == null) {
            // Fallback on old <language> element
            this.locale = readElement(rootElement, "language");
        }

        this.defaultLanguage = readElement(rootElement, "defaultLanguage");
        this.creator = readElement(rootElement, "creator");
        this.author = readElement(rootElement, AUTHOR_TAG);
        this.contentAuthor = readElement(rootElement, "contentAuthor");
        this.version = readElement(rootElement, "version");
        this.parent = readElement(rootElement, "parent");
        this.comment = readElement(rootElement, "comment");
        this.minorEdit = readElement(rootElement, "minorEdit");
        this.attachmentAuthors = readAttachmentAuthors(rootElement);
        this.isHidden = Boolean.parseBoolean(readElement(rootElement, "hidden"));
        this.title = readElement(rootElement, "title");
        this.syntaxId = readElement(rootElement, "syntaxId");

        // Does this document contain a XWiki.TranslationDocumentClass xobject?
        if (rootElement.selectNodes("//object/className[text() = 'XWiki.TranslationDocumentClass']").size() > 0) {
            this.containsTranslations = true;
        }
    }

    /**
     * Read an element from the XML.
     *
     * @param rootElement the root XML element under which to find the element
     * @param elementName the name of the element to read
     * @return null or the element value as a String
     */
    private String readElement(Element rootElement, String elementName)
    {
        String result = null;
        Element element = rootElement.element(elementName);
        if (element != null) {
            result = element.getText();
        }
        return result;
    }

    private List<String> readAttachmentAuthors(Element rootElement)
    {
        List<String> authors = new ArrayList<>();
        for (Object attachmentNode : rootElement.elements("attachment")) {
            authors.add(readElement((Element) attachmentNode, AUTHOR_TAG));
        }
        return authors;
    }

    /**
     * @return the document reference
     * @since 7.2M1
     */
    public String getReference()
    {
        return this.reference;
    }

    /**
     * @param reference the document reference
     * @since 7.2M1
     */
    public void setReference(String reference)
    {
        this.reference = reference;
    }

    /**
     * @return the language of the document.
     * @since 7.2M1
     */
    public String getLocale()
    {
        return this.locale;
    }

    /**
     * @param locale the locale of the document.
     * @since 7.2M1
     */
    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    /**
     * @return the default language of the document.
     */
    public String getDefaultLanguage()
    {
        return this.defaultLanguage;
    }

    /**
     * @param defaultLanguage the default language of the document.
     */
    public void setDefaultLanguage(String defaultLanguage)
    {
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * @return the creator of the document
     */
    public String getCreator()
    {
        return this.creator;
    }

    /**
     * @return the author of the document
     */
    public String getAuthor()
    {
        return this.author;
    }

    /**
     * @return the content author of the document
     */
    public String getContentAuthor()
    {
        return this.contentAuthor;
    }

    /**
     * @return the version of the document
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * @return the parent of the document
     */
    public String getParent()
    {
        return this.parent;
    }

    /**
     * @return the comment of the last save
     */
    public String getComment()
    {
        return this.comment;
    }

    /**
     * @return the minor edit value ("true" or "false")
     */
    public String getMinorEdit()
    {
        return this.minorEdit;
    }

    /**
     * @return the attachment authors and an empty list if there's no attachment
     * @since 7.0RC1
     */
    public List<String> getAttachmentAuthors()
    {
        return this.attachmentAuthors;
    }

    /**
     * @return the XML file encoding
     */
    public String getEncoding()
    {
        return this.encoding;
    }

    /**
     * @return true if the document is hidden or false otherwise
     */
    public boolean isHidden()
    {
        return this.isHidden;
    }

    /**
     * @return the document's title
     * @since 7.3RC1
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * @return true if the document contains a XWiki.TranslationDocumentClass xobject
     * @since 8.1M1
     */
    public boolean containsTranslations()
    {
        return this.containsTranslations;
    }

    /**
     * @return the document's syntax id
     * @since 8.1M1
     */
    public String getSyntaxId()
    {
        return this.syntaxId;
    }

    /**
     * @param name the name to escape
     * @return the escaped name
     */
    private String escapeSpaceOrPageName(String name)
    {
        return name != null ? name.replaceAll("[\\\\\\.]", "\\\\$0") : null;
    }

    /**
     * @param file the file containing the document.
     * @return the full name of the document or null, if the document is invalid
     * @since 7.2M1
     */
    public static String getReference(File file)
    {
        XWikiDocument doc;
        try {
            doc = new XWikiDocument();
            doc.fromXML(file);
        } catch (Exception e) {
            return null;
        }

        return doc.getReference();
    }
}
