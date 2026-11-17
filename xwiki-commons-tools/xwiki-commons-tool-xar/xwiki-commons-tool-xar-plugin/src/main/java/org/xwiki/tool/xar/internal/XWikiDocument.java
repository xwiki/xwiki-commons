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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.LocaleUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import static org.xwiki.tool.xar.internal.XMLUtils.getSAXReader;

/**
 * Parse XWiki document from XML.
 *
 * @version $Id$
 */
public class XWikiDocument
{
    /**
     * The description of the {@code originalMetadataAuthor} element, as a key of {@link #getRequiredFormatVersions()}.
     *
     * @since 17.10.16
     * @since 18.4.9
     * @since 18.10.0RC1
     */
    public static final String ORIGINAL_METADATA_AUTHOR_ELEMENT = "the <originalMetadataAuthor> element";

    /**
     * The highest XAR format version known by the XWiki version this plugin is released with, which is the XWiki
     * version an extension targets since it gets this plugin's version from its parent. XWiki imports a document
     * declaring a higher format, but ignores the elements it doesn't know, so such a document would lose content when
     * installed. Must be raised, and backported along, whenever XWiki Platform's {@code XarDocumentModel} gets a new
     * format version.
     *
     * @since 17.10.16
     * @since 18.4.9
     * @since 18.10.0RC1
     */
    public static final String HIGHEST_KNOWN_FORMAT_VERSION = XWikiDocument.FORMAT_VERSION_17;

    private static final String AUTHOR_TAG = "author";

    private static final String MIMETYPE_TAG = "mimetype";

    private static final String FILENAME_TAG = "filename";

    private static final String VERSION_TAG = "version";

    private static final String ORIGINAL_METADATA_AUTHOR_TAG = "originalMetadataAuthor";

    /**
     * The format version of a document whose root element has no {@code version} attribute.
     */
    private static final String DEFAULT_FORMAT_VERSION = "1.0";

    private static final String FORMAT_VERSION_17 = "1.7";

    private static final Pattern FORMAT_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)");

    /**
     * The elements and attributes that a version of the XAR format introduced, with that version. The format version
     * a document declares names the set of elements it can contain: XWiki doesn't read it when importing the
     * document, but a document must not use an element its declared format doesn't have, so that the declared
     * version keeps describing its content. The {@code reference} and {@code locale} attributes of the root element
     * (format 1.2) and the XML 1.1 declaration (format 1.3) are not listed: the format goal writes the XML 1.1
     * declaration on every document, and too many existing documents declaring an older format carry these
     * attributes.
     *
     * @see <a href="https://www.xwiki.org/xwiki/bin/view/documentation/xs/admin/exports/export-content-xar-format/format-specifications/document-xml/">the format versions</a>
     */
    private static final List<VersionedNode> VERSIONED_NODES = List.of(
        new VersionedNode("attachment/revisions", "the <revisions> element of an attachment", "1.4"),
        new VersionedNode(ORIGINAL_METADATA_AUTHOR_TAG, ORIGINAL_METADATA_AUTHOR_ELEMENT, "1.5"),
        new VersionedNode("enforceRequiredRights", "the <enforceRequiredRights> element", "1.6"),
        new VersionedNode("object/property/@type", "the type attribute of an object property", FORMAT_VERSION_17));

    /**
     * @see #getFormatVersion()
     */
    private String formatVersion;

    /**
     * @see #getRequiredFormatVersions()
     */
    private Map<String, String> requiredFormatVersions;

    /**
     * @see #getReference()
     * @since 7.2M1
     */
    private String reference;

    /**
     * @see #getLocale()
     * @since 7.2M1
     */
    private Locale locale;

    /**
     * @see #getDefaultLocale()
     */
    private Locale defaultLocale;

    /**
     * @see #getCreator()
     */
    private String creator;

    /**
     * @see #getEffectiveMetadataAuthor()
     */
    private String effectiveMetadataAuthor;

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
     * @see #getWikiMacroIds()
     */
    private final List<String> wikiMacroIds = new ArrayList<>();

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
     * @see #isOriginalMetadataAuthorPresent()
     */
    private boolean originalMetadataAuthorPresent;

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
        SAXReader reader = getSAXReader();
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
        SAXReader reader = getSAXReader();
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

        readFormatVersions(rootElement);

        this.locale = toLocale(rootElement.attributeValue("locale"), true);
        if (this.locale == null) {
            // Fallback on old <language> element
            this.locale = readLocaleElement(rootElement, "language");
            if (this.locale == null) {
                // The locale is ROOT by default
                this.locale = Locale.ROOT;
            }
        }

        this.defaultLocale = readLocaleElement(rootElement, "defaultLanguage");
        this.creator = readElement(rootElement, "creator");
        this.effectiveMetadataAuthor = readElement(rootElement, AUTHOR_TAG);
        this.contentAuthor = readElement(rootElement, "contentAuthor");
        this.version = readElement(rootElement, VERSION_TAG);
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
        this.originalMetadataAuthorPresent = isElementPresent(rootElement, ORIGINAL_METADATA_AUTHOR_TAG);
        this.objectPresent = isElementPresent(rootElement, "object");
        this.attachmentDatePresent = rootElement.selectSingleNode("//attachment/date") != null;

        readObjects(rootElement);
    }

    private void readObjects(Element rootElement)
    {
        // Does this document contain a XWiki.TranslationDocumentClass xobject?
        if (!rootElement.selectNodes("//object/className[text() = 'XWiki.TranslationDocumentClass']").isEmpty()) {
            this.containsTranslations = true;
            // Record the visibility
            for (Node node : rootElement
                .selectNodes("//object/className[text() = 'XWiki.TranslationDocumentClass']/../property/scope")) {
                this.translationVisibilities.add(node.getStringValue());
            }
        }

        // Record the ids of the wiki macros defined by XWiki.WikiMacroClass xobjects
        for (Node node : rootElement
            .selectNodes("//object[className = 'XWiki.WikiMacroClass']/property/id")) {
            this.wikiMacroIds.add(node.getStringValue().trim());
        }
    }

    private void readFormatVersions(Element rootElement)
    {
        this.formatVersion = readFormatVersion(rootElement);
        this.requiredFormatVersions = readRequiredFormatVersions(rootElement);
    }

    /**
     * @param rootElement the root element of the XML document
     * @return the version of the XAR format declared by the {@code version} attribute of the root element, or
     *         {@code 1.0} when that attribute is missing
     * @since 17.10.16
     * @since 18.4.9
     * @since 18.10.0RC1
     */
    public static String readFormatVersion(Element rootElement)
    {
        return rootElement.attributeValue(VERSION_TAG, DEFAULT_FORMAT_VERSION);
    }

    /**
     * Only the elements and attributes introduced by a format version that the declared version must include are
     * considered (see {@link #VERSIONED_NODES}), and only when they are direct children of the root element or, for
     * attachments and objects, of its direct children: an XClass property named after one of them doesn't count.
     *
     * @param rootElement the root element of the XML document
     * @return the elements and attributes of the document that were introduced by a version of the XAR format, each
     *         one (described for a user) mapped to that version
     * @since 17.10.16
     * @since 18.4.9
     * @since 18.10.0RC1
     */
    public static Map<String, String> readRequiredFormatVersions(Element rootElement)
    {
        Map<String, String> result = new LinkedHashMap<>();
        for (VersionedNode node : VERSIONED_NODES) {
            if (rootElement.selectSingleNode(node.xpath()) != null) {
                result.put(node.description(), node.version());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * @param version the format version to check
     * @return {@code true} if the version is of the form {@code <major>.<minor>}, {@code false} otherwise
     * @since 17.10.16
     * @since 18.4.9
     * @since 18.10.0RC1
     */
    public static boolean isValidFormatVersion(String version)
    {
        return FORMAT_VERSION_PATTERN.matcher(version).matches();
    }

    /**
     * @param version a valid format version
     * @param otherVersions other valid format versions
     * @return the highest of all these format versions
     * @throws IllegalArgumentException if one of the versions is not valid
     * @since 17.10.16
     * @since 18.4.9
     * @since 18.10.0RC1
     */
    public static String getHighestFormatVersion(String version, Collection<String> otherVersions)
    {
        String result = version;
        for (String otherVersion : otherVersions) {
            if (compareFormatVersions(result, otherVersion) < 0) {
                result = otherVersion;
            }
        }
        return result;
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
     * Read an element from the XML and convert it to {@link Locale}.
     *
     * @param rootElement the root XML element under which to find the element
     * @param elementName the name of the element to read
     * @return null or the element value as a String
     * @throws DocumentException if it is not a valid XML wiki page
     * @since 15.7RC1
     * @since 15.5.2
     * @since 14.10.15
     */
    public static Locale readLocaleElement(Element rootElement, String elementName) throws DocumentException
    {
        return toLocale(readElement(rootElement, elementName), true);
    }

    /**
     * Parse the locale string into a {@link Locale} instead. It also make sure the format of the String is the expected
     * one.
     * 
     * @param localeString the locale as a String
     * @param validate true when the format of the String should be validated
     * @return the valid {@link Locale} instance
     * @throws DocumentException when the format of the locale String is wrong
     */
    public static Locale toLocale(String localeString, boolean validate) throws DocumentException
    {
        if (localeString == null) {
            return null;
        }

        // Parse the locale String
        Locale locale;
        try {
            locale = LocaleUtils.toLocale(localeString);
        } catch (Exception e) {
            throw new DocumentException("Failed to parse the locale String [" + localeString + "]", e);
        }

        // Make sure the string uses the canonical format
        if (validate && !localeString.equals(locale.toString())) {
            throw new DocumentException("Wrong locale format for [" + localeString + "], the expected value is ["
                + locale.toString() + "]");
        }

        return locale;
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
     * Compare two versions of the XAR format.
     *
     * @param version1 the first format version (e.g. {@code 1.6})
     * @param version2 the second format version
     * @return a negative integer, zero, or a positive integer as the first version is lower than, equal to, or
     *         greater than the second
     * @throws IllegalArgumentException if one of the versions is not valid (see {@link #isValidFormatVersion(String)})
     * @since 17.10.16
     * @since 18.4.9
     * @since 18.10.0RC1
     */
    public static int compareFormatVersions(String version1, String version2)
    {
        int[] parsed1 = parseFormatVersion(version1);
        int[] parsed2 = parseFormatVersion(version2);
        int result = Integer.compare(parsed1[0], parsed2[0]);
        return result == 0 ? Integer.compare(parsed1[1], parsed2[1]) : result;
    }

    private static int[] parseFormatVersion(String version)
    {
        Matcher matcher = FORMAT_VERSION_PATTERN.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                String.format("Invalid XAR format version [%s], expected [<major>.<minor>]", version));
        }
        return new int[] { Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) };
    }

    /**
     * @return the version of the XAR format declared by the {@code version} attribute of the root element, or
     *         {@code 1.0} when that attribute is missing
     * @since 17.10.16
     * @since 18.4.9
     * @since 18.10.0RC1
     */
    public String getFormatVersion()
    {
        return this.formatVersion;
    }

    /**
     * @return the elements and attributes of the document that were introduced by a version of the XAR format, each
     *         one (described for a user) mapped to that version
     * @see #readRequiredFormatVersions(Element)
     * @since 17.10.16
     * @since 18.4.9
     * @since 18.10.0RC1
     */
    public Map<String, String> getRequiredFormatVersions()
    {
        return this.requiredFormatVersions;
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
     * @since 15.7RC1
     * @since 15.5.2
     * @since 14.10.15
     */
    public Locale getLocale()
    {
        return this.locale;
    }

    /**
     * @param locale the locale of the document.
     * @since 15.7RC1
     * @since 15.5.2
     * @since 14.10.15
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * @return the default language of the document.
     * @since 15.7RC1
     * @since 15.5.2
     * @since 14.10.15
     */
    public Locale getDefaultLocale()
    {
        return this.defaultLocale;
    }

    /**
     * @param defaultLocale the default language of the document.
     * @since 15.7RC1
     * @since 15.5.2
     * @since 14.10.15
     */
    public void setDefaultLocale(Locale defaultLocale)
    {
        this.defaultLocale = defaultLocale;
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
     * @return the ids of the wiki macros defined in this document, and an empty list if there are none
     * @since 18.10.0RC1
     */
    public List<String> getWikiMacroIds()
    {
        return this.wikiMacroIds;
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
     * @return {@code true} if the original metadata author is present in the document; false otherwise
     * @since 14.5
     */
    public boolean isOriginalMetadataAuthorPresent()
    {
        return originalMetadataAuthorPresent;
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

    /**
     * An element or attribute introduced by a version of the XAR format.
     *
     * @param xpath the XPath of the node, relative to the root element
     * @param description the description of the node, for a user
     * @param version the format version that introduced the node
     */
    private record VersionedNode(String xpath, String description, String version)
    {
    }
}
