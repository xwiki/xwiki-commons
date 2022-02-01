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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Parse XWiki document from XML.
 *
 * @version $Id$
 */
public class XWikiDocument
{
    private static final String AUTHOR_TAG = "author";

    private static final String MIMETYPE_TAG = "mimetype";

    private static final String FILENAME_TAG = "filename";

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
     * @see #getEffectiveMetadataAuthor()
     */
    private String effectiveMetadataAuthor;

    /**
     * @see #getOriginalMetadataAuthor()
     */
    private String originalMetadataAuthor;

    /**
     * @see #getContentAuthor()
     */
    private String contentAuthor;

    /**
     * @see #getAttachmentData()
     * @since 10.6RC1
     */
    private List<Map<String, String>> attachmentData;

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
     * @see #getContent()
     */
    private String content;

    /**
     * @see #containsTranslations()
     */
    private boolean containsTranslations;

    /**
     * @see #getTranslationVisibilities()
     */
    private List<String> translationVisibilities = new ArrayList<>();

    /**
     * @see #isDatePresent()
     */
    private boolean datePresent;

    /**
     * @see #isContentUpdateDatePresent()
     */
    private boolean contentUpdateDatePresent;

    /**
     * @see #isCreationDatePresent()
     */
    private boolean creationDatePresent;

    /**
     * @see #isObjectPresent()
     */
    private boolean objectPresent;

    /**
     * @see #isAttachmentDatePresent()
     */
    private boolean attachmentDatePresent;

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

        this.reference = readDocumentReference(domdoc);

        this.locale = rootElement.attributeValue("locale");
        if (this.locale == null) {
            // Fallback on old <language> element
            this.locale = readElement(rootElement, "language");
        }

        this.defaultLanguage = readElement(rootElement, "defaultLanguage");
        this.creator = readElement(rootElement, "creator");
        this.effectiveMetadataAuthor = readElement(rootElement, AUTHOR_TAG);
        this.originalMetadataAuthor = readElement(rootElement, "originalMetadataAuthor");
        this.contentAuthor = readElement(rootElement, "contentAuthor");
        this.version = readElement(rootElement, "version");
        this.parent = readElement(rootElement, "parent");
        this.comment = readElement(rootElement, "comment");
        this.minorEdit = readElement(rootElement, "minorEdit");
        this.attachmentData = readAttachmentData(rootElement);
        this.isHidden = Boolean.parseBoolean(readElement(rootElement, "hidden"));
        this.title = readElement(rootElement, "title");
        this.syntaxId = readElement(rootElement, "syntaxId");
        this.content = readElement(rootElement, "content");

        this.datePresent = isElementPresent(rootElement, "date");
        this.contentUpdateDatePresent = isElementPresent(rootElement, "contentUpdateDate");
        this.creationDatePresent = isElementPresent(rootElement, "creationDate");
        this.objectPresent = isElementPresent(rootElement, "object");
        this.attachmentDatePresent = rootElement.selectSingleNode("//attachment/date") != null;

        // Does this document contain a XWiki.TranslationDocumentClass xobject?
        if (!rootElement.selectNodes("//object/className[text() = 'XWiki.TranslationDocumentClass']").isEmpty()) {
            this.containsTranslations = true;
            // Record the visibility
            for (Node node : rootElement
                .selectNodes("//object/className[text() = 'XWiki.TranslationDocumentClass']/../property/scope")) {
                this.translationVisibilities.add(node.getStringValue());
            }
        }
    }

    /**
     * @param domdoc the DOM document containing and XML wiki page
     * @return the reference of the wiki page
     * @throws DocumentException if it is not a valid XML wiki page
     * @since 10.8RC1
     */
    public static String readDocumentReference(Document domdoc) throws DocumentException
    {
        Element rootElement = domdoc.getRootElement();

        String result = rootElement.attributeValue("reference");
        if (result == null) {
            String name = readElement(rootElement, "name");
            String space = readElement(rootElement, "web");

            // If the reference, name and space don't exist we consider that we're not reading an XML that corresponds
            // to a wiki page.
            if (name == null && space == null) {
                throw new DocumentException(
                    String.format("Content doesn't point to valid wiki page XML [%s]", domdoc.getName()));
            }

            result = space == null ? name : escapeSpaceOrPageName(space) + '.' + escapeSpaceOrPageName(name);
        }

        return result;
    }

    /**
     * @param rootElement the root XML element under which to find the element
     * @param elementName the name of the element to read
     * @return {@code true} if the element is present; {@code false} otherwise
     * @since 10.8RC1
     */
    public static boolean isElementPresent(Element rootElement, String elementName)
    {
        Element element = rootElement.element(elementName);
        return element != null;
    }

    /**
     * Read an element from the XML.
     *
     * @param rootElement the root XML element under which to find the element
     * @param elementName the name of the element to read
     * @return null or the element value as a String
     * @throws DocumentException if it is not a valid XML wiki page
     * @since 10.8RC1
     */
    public static String readElement(Element rootElement, String elementName) throws DocumentException
    {
        String result = null;
        Element element = rootElement.element(elementName);
        if (element != null) {
            // Make sure the element does not have any child element
            if (!element.isTextOnly()) {
                throw new DocumentException("Unexpected non-text content found in element [" + elementName + "]");
            }

            result = element.getText();
        }
        return result;
    }

    /**
     * @param rootElement the root element of the XML document
     * @return the list of data for each attachment
     * @throws DocumentException if it is not a valid XML wiki page
     * @since 10.8RC1
     */
    public static List<Map<String, String>> readAttachmentData(Element rootElement) throws DocumentException
    {
        List<Map<String, String>> data = new ArrayList<>();
        for (Element attachmentNode : rootElement.elements("attachment")) {
            Map<String, String> map = new HashMap<>();
            String authorValue = readElement(attachmentNode, AUTHOR_TAG);
            if (authorValue != null) {
                map.put(AUTHOR_TAG, authorValue);
            }
            String mimetypeValue = readElement(attachmentNode, MIMETYPE_TAG);
            if (mimetypeValue != null) {
                map.put(MIMETYPE_TAG, mimetypeValue);
            }
            String filenameValue = readElement(attachmentNode, FILENAME_TAG);
            if (filenameValue != null) {
                map.put(FILENAME_TAG, filenameValue);
            }
            data.add(map);
        }
        return data;
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
     * @return the effective metadata author of the document
     */
    public String getEffectiveMetadataAuthor()
    {
        return this.effectiveMetadataAuthor;
    }

    /**
     * @return the original metadata author of the document
     */
    public String getOriginalMetadataAuthor()
    {
        return this.originalMetadataAuthor;
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
     * @return the attachment data (authors, mimetypes, etc) and an empty list if there's no attachment
     * @since 10.6RC1
     */
    public List<Map<String, String>> getAttachmentData()
    {
        return this.attachmentData;
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
     * @return the list of Translation xobject visibilities (WIKI, USER, GLOBAL, etc) and an empty list if no
     *         translation exist on this page
     * @since 10.1RC1
     */
    public List<String> getTranslationVisibilities()
    {
        return this.translationVisibilities;
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
     * @return the content of the document
     * @since 10.10RC1
     */
    public String getContent()
    {
        return this.content;
    }

    /**
     * @param name the name to escape
     * @return the escaped name
     * @since 10.8RC1
     */
    public static String escapeSpaceOrPageName(String name)
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

    /**
     * @return {@code true} if the date field is present; false otherwise
     * @since 10.8RC1
     */
    public boolean isDatePresent()
    {
        return datePresent;
    }

    /**
     * @return {@code true} if the contentUpdateDate field is present; false otherwise
     * @since 10.8RC1
     */
    public boolean isContentUpdateDatePresent()
    {
        return contentUpdateDatePresent;
    }

    /**
     * @return {@code true} if the creationDate field is present; false otherwise
     * @since 13.1RC1
     */
    public boolean isCreationDatePresent()
    {
        return creationDatePresent;
    }

    /**
     * @return {@code true} if there is object(s) in the document; false otherwise
     * @since 13.1RC1
     */
    public boolean isObjectPresent()
    {
        return objectPresent;
    }

    /**
     * @return {@code true} if there is attachment(s) in the document; false otherwise
     * @since 13.1RC1
     */
    public boolean isAttachmentPresent()
    {
        return !attachmentData.isEmpty();
    }

    /**
     * @return {@code true} if the date field is present for an attachment; false otherwise
     * @since 10.8RC1
     */
    public boolean isAttachmentDatePresent()
    {
        return attachmentDatePresent;
    }
}
