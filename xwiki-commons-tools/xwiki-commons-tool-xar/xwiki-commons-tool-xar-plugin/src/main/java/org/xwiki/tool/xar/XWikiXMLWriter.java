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
     * @param output the stream where to write the XML
     * @param format the style to use when outputting the XML
     * @throws UnsupportedEncodingException in case encoding issue
     */
    public XWikiXMLWriter(OutputStream output, OutputFormat format) throws UnsupportedEncodingException
    {
        super(output, format);
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
}
