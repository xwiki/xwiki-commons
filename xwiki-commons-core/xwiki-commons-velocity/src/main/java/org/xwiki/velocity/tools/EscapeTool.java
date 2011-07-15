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
package org.xwiki.velocity.tools;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.BCodec;
import org.apache.commons.codec.net.QCodec;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.xwiki.xml.XMLUtils;

/**
 * <p>
 * Tool for working with escaping in Velocity templates. It provides methods to escape outputs for Velocity, Java,
 * JavaScript, HTML, XML and SQL.
 * </p>
 * <p>
 * Extends the default EscapeTool from velocity-tools since the XML escape performed by it doesn't work inside HTML
 * content, since {@code apos} is not a valid HTML entity name, and it always escapes non-ASCII characters, which
 * increases the HTML length considerably, while also making the source unreadable.
 * </p>
 * 
 * @version $Id$
 * @since 2.7RC1
 */
public class EscapeTool extends org.apache.velocity.tools.generic.EscapeTool
{
    /**
     * Escapes the XML special characters in a <code>String</code> using numerical XML entities.
     * 
     * @param content the text to escape, may be {@code null}
     * @return a new escaped {@code String}, {@code null} if {@code null} input
     */
    @Override
    public String xml(Object content)
    {
        return XMLUtils.escape(content);
    }

    /**
     * Encode a text using the Quoted-Printable format, as specified in section 6.7 of <a
     * href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>. UTF-8 is used as the character encoding, and no line
     * breaking is performed.
     * 
     * @param content the text to encode
     * @return the text converted into the Quoted-Printable format
     */
    public String quotedPrintable(Object content)
    {
        if (content != null) {
            try {
                return new QuotedPrintableCodec().encode(String.valueOf(content));
            } catch (EncoderException ex) {
                // Just return null
            }
        }
        return null;
    }

    /**
     * Encode a text using the Q encoding specified in <a href="http://www.ietf.org/rfc/rfc2047.txt">RFC 2047</a>. UTF-8
     * is used as the character encoding, and no line breaking is performed. The resulting text is already wrapped with
     * the encoded word markers, starting with {@code =?UTF-8?Q?} and ending with {@code ?=}.
     * 
     * @param content the text to encode
     * @return the text converted into an encoded word using the Q encoding
     */
    public String q(Object content)
    {
        if (content != null) {
            try {
                return new QCodec().encode(String.valueOf(content)).replace(' ', '_');
            } catch (EncoderException ex) {
                // Just return null
            }
        }
        return null;
    }

    /**
     * Encode a text using the B encoding specified in <a href="http://www.ietf.org/rfc/rfc2047.txt">RFC 2047</a>. UTF-8
     * is used as the character encoding, and no line breaking is performed. The resulting text is already wrapped with
     * the encoded word markers, starting with {@code =?UTF-8?B?} and ending with {@code ?=}.
     * 
     * @param content the text to encode
     * @return the text converted into an encoded word using the B encoding
     */
    public String b(Object content)
    {
        if (content != null) {
            try {
                return new BCodec().encode(String.valueOf(content));
            } catch (EncoderException ex) {
                // Just return null
            }
        }
        return null;
    }
}
