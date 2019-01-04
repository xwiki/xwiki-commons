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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Custom XML Writer to support XWiki's code style for XAR XML files.
 *
 * @version $Id$
 * @since 4.0M2
 */
public class XWikiXMLWriter extends XMLWriter
{
    /**
     * True if we use an output format.
     */
    private boolean useFormat;

    private String version;

    /**
     * @param output the stream where to write the XML
     * @throws UnsupportedEncodingException in case encoding issue
     */
    public XWikiXMLWriter(OutputStream output) throws UnsupportedEncodingException
    {
        super(output);
    }

    /**
     * @param output the stream where to write the XML
     * @param format the style to use when outputting the XML
     * @throws UnsupportedEncodingException in case encoding issue
     */
    public XWikiXMLWriter(OutputStream output, OutputFormat format) throws UnsupportedEncodingException
    {
        super(output, format);
        this.useFormat = true;
    }

    /**
     * @param xmlVersion the XML version to set in the declaration
     * @since 9.11.1
     * @since 10.0
     */
    public void setVersion(String xmlVersion)
    {
        this.version = xmlVersion;
    }

    @Override
    protected void writeComment(String text) throws IOException
    {
        super.writeComment(text);

        // Add a new line after the license declaration
        if (text.contains("See the NOTICE file distributed with this work for additional")) {
            println();
        }
    }

    @Override
    protected void writeNodeText(Node node) throws IOException
    {
        if (this.useFormat && node.getText().trim().length() == 0) {
            // Check if parent node contains non text nodes
            boolean containsNonTextNode = false;
            for (Node objectNode : node.getParent().content()) {
                if (objectNode.getNodeType() != Node.TEXT_NODE) {
                    containsNonTextNode = true;
                    break;
                }
            }
            if (containsNonTextNode) {
                // Don't do anything, i.e. don't print the current text node
            } else {
                super.writeNodeText(node);
            }
        } else {
            super.writeNodeText(node);
        }
    }

    @Override
    protected void writePrintln() throws IOException
    {
        // We need to reimplement this method because of a bug (bad logic) in the original writePrintln() which checks
        // the last output char to decide whether to print a NL or not:
        // ...3</a></b> --> ...3</a>\n</b>
        // but
        // ...3\n</a></b> --> ...3\n</a></b>
        // and
        // ...3\n</a>\n</b> --> ...3\n</a></b>
        if (this.useFormat) {
            this.writer.write(getOutputFormat().getLineSeparator());
        }
    }

    @Override
    protected void writeDeclaration() throws IOException
    {
        String encoding = getOutputFormat().getEncoding();

        // Only print of declaration is not suppressed
        if (!getOutputFormat().isSuppressDeclaration()) {
            this.writer.write(String.format("<?xml version=\"%s\"", this.version));

            if (!getOutputFormat().isOmitEncoding()) {
                this.writer.write(String.format(" encoding=\"%s\"", encoding));
            }

            this.writer.write("?>");

            if (getOutputFormat().isNewLineAfterDeclaration()) {
                println();
            }
        }
    }
}
