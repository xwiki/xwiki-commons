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
import java.nio.charset.StandardCharsets;

/**
 * Hold Transformation configuration as specified by the user in its pom.xml.
 *
 * @version $Id$
 * @since 5.3M1
 */
public class Transformation
{
    /**
     * The action to apply.
     *
     * @version $Id$
     * @since 9.5RC1
     */
    public enum Action
    {
        /**
         * Replace the found node with passed XML.
         */
        REPLACE,

        /**
         * Remove the found node.
         */
        REMOVE,

        /**
         * Add passed XML as child of the found node.
         */
        INSERT_CHILD,

        /**
         * Add content of the text file as child-text of the found node.
         * @since 11.3RC1
         */
        INSERT_TEXT,

        /**
         * Add content of the binary base64-encoded as child of the found node.
         * @since 11.3RC1
         */
        INSERT_ATTACHMENT_CONTENT,

    }

    private Action action = Action.REPLACE;

    private String xpath;

    private String value;

    private File xml;

    private String file;

    private String artifact;

    private File content;

    private String charset = StandardCharsets.UTF_8.name();

    /**
     * @return the action to apply
     * @since 9.5RC1
     */
    public Action getAction()
    {
        return action;
    }

    /**
     * @param action the action to apply
     * @since 9.5RC1
     */
    public void setAction(Action action)
    {
        this.action = action;
    }

    /**
     * @return the optional id (in the format {@code groupId:artifactId} of the dependent XAR artifact where the page to
     *         be transformed is located. If not specified then the page is considered to be in the current project
     */
    public String getArtifact()
    {
        return this.artifact;
    }

    /**
     * @param artifact see {@link #getArtifact()}
     */
    public void setArtifact(String artifact)
    {
        this.artifact = artifact;
    }

    /**
     * @return the path relative to the {@code target/classes} directory of the XML file for which to apply a
     *         transformation
     */
    public String getFile()
    {
        return this.file;
    }

    /**
     * @param file see {@link #getFile()}
     */
    public void setFile(String file)
    {
        this.file = file;
    }

    /**
     * @return the XPath expression to locate the node for which to change the text value
     */
    public String getXpath()
    {
        if (action == Action.INSERT_ATTACHMENT_CONTENT) {
            return "/xwikidoc/attachment[filename/text()='"
                    + content.getName().replaceAll("'", "\\'") + "']";
        } else {
            return this.xpath;
        }
    }

    /**
     * @param xpath see {@link #getXpath()}
     */
    public void setXpath(String xpath)
    {
        this.xpath = xpath;
    }

    /**
     * @return the new value to set for the node specified by the XPath expression. Note that {@code $1} tokens are
     *         replaced by the current value
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * @param value see {@link #getValue()}
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @return the file containing the XML to insert
     * @since 9.5RC1
     */
    public File getXml()
    {
        return xml;
    }

    /**
     * @param xml the file containing the XML to insert
     * @since 9.5RC1
     */
    public void setXml(File xml)
    {
        this.xml = xml;
    }


    /**
     * @return the file containing the text to insert (action INSERT_TEXT)
     *         or the blob to insert (action INSERT_FILE_BASE64).
     * @since 11.3RC1
     */
    public File getContent()
    {
        return content;
    }

    /**
     * @param content the file containing the text or blob to insert
     * @since 11.3RC1
     */
    public void setContent(File content)
    {
        this.content = content;
    }

    /**
     * @return the charset used to read the file containing the text to insert
     *         (for action INSERT_TEXT only, optional, default value UTF-8)
     * @since 11.3RC1
     */
    public String getCharset()
    {
        return charset;
    }

    /**
     * @param charset the charset used to read the file containing the text to insert
     *         (for action INSERT_TEXT only, optional, default value UTF-8)
     * @since 11.3RC1
     */
    public void setCharset(String charset)
    {
        this.charset = charset;
    }
}
