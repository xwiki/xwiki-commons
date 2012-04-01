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
package org.xwiki.tool.xar;

import java.io.File;

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
    /**
     * @see #getName()
     */
    private String name;

    /**
     * @see #getSpace()
     */
    private String space;

    /**
     * @see #getLanguage()
     */
    private String language;

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
     * Parse XML file to extract document information.
     * 
     * @param file the xml file
     * @throws DocumentException error when parsing XML file
     */
    public void fromXML(File file) throws DocumentException
    {
        SAXReader reader = new SAXReader();
        Document domdoc = reader.read(file);

        this.encoding = domdoc.getXMLEncoding();

        Element rootElement = domdoc.getRootElement();
        this.name = readElement(rootElement, "name");
        this.space = readElement(rootElement, "web");
        this.language = readElement(rootElement, "language");
        this.defaultLanguage = readElement(rootElement, "defaultLanguage");
        this.creator = readElement(rootElement, "creator");
        this.author = readElement(rootElement, "author");
        this.contentAuthor = readElement(rootElement, "contentAuthor");
        this.version = readElement(rootElement, "version");
        this.parent = readElement(rootElement, "parent");
        this.comment = readElement(rootElement, "comment");
        this.minorEdit = readElement(rootElement, "minorEdit");
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

    /**
     * @return the name of the document.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the name of the document.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the space of the document.
     */
    public String getSpace()
    {
        return this.space;
    }

    /**
     * @param space the space of the document.
     */
    public void setSpace(String space)
    {
        this.space = space;
    }

    /**
     * @return the language of the document.
     */
    public String getLanguage()
    {
        return this.language;
    }

    /**
     * @param language the language of the document.
     */
    public void setLanguage(String language)
    {
        this.language = language;
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
     * @return the XML file encoding
     */
    public String getEncoding()
    {
        return this.encoding;
    }

    /**
     * @return the full name of the document.
     */
    public String getFullName()
    {
        return this.space == null ? this.name : this.space + "." + this.name;
    }

    /**
     * @param file the file containing the document.
     * @return the full name of the document or null, if the document is invalid
     */
    public static String getFullName(File file)
    {
        XWikiDocument doc;
        try {
            doc = new XWikiDocument();
            doc.fromXML(file);
        } catch (Exception e) {
            return null;
        }

        return doc.getFullName();
    }
}
