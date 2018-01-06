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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.BCodec;
import org.apache.commons.codec.net.QCodec;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(EscapeTool.class);

    /** Equals sign. */
    private static final String EQUALS = "=";

    /** And sign. */
    private static final String AND = "&";

    /**
     * Escapes the XML special characters in a <code>String</code> using numerical XML entities. This overrides the base
     * implementation from Velocity, which is over-zealous and escapes any non-ASCII character. Since XWiki works with
     * Unicode-capable encodings (UTF-8), there is no need to escape non-special characters.
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
     * Escapes the characters in a <code>String</code> using JSON String rules: escapes with backslash double quotes,
     * back and forward slashes, newlines, the control characters {@code \b}, {@code \t} and {@code \f}, and with
     * {@code \}{@code uXXXX} any non-ASCII characters. Unlike {@link #javascript(Object)}, it does not escape {@code '}
     * , which is not a special character in JSON, and it would be a syntax error to do so.
     *
     * @param string the string to escape, may be {@code null}; any non-string object will be converted to a string
     *            first, using {@code String.valueOf(obj)}
     * @return String with escaped values, {@code null} if {@code null} input
     * @since 6.1M1
     */
    public String json(Object string)
    {
        if (string == null) {
            return null;
        }
        return StringEscapeUtils.escapeJson(String.valueOf(string));
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

    /**
     * Properly escape a parameter map representing a query string, so that it can be safely used in an URL. Parameters
     * can have multiple values in which case the value in the map is either an array or a {@link Collection}. If the
     * parameter name is {@code null} (the key is {@code null}) then the parameter is ignored. {@code null} values are
     * serialized as an empty string.
     *
     * @param parametersMap Map representing the query string.
     * @return the safe query string representing the passed parameters
     * @since 5.2M1
     */
    public String url(Map<String, ?> parametersMap)
    {
        StringBuilder queryStringBuilder = new StringBuilder();
        for (Map.Entry<String, ?> entry : parametersMap.entrySet()) {
            if (entry.getKey() == null) {
                // Skip the parameter if its name is null.
                continue;
            }
            String cleanKey = this.url(entry.getKey());
            Object mapValues = entry.getValue();
            if (mapValues != null && mapValues.getClass().isArray()) {
                // A parameter with multiple values.
                Object[] values = (Object[]) mapValues;
                for (Object value : values) {
                    addQueryStringPair(cleanKey, value, queryStringBuilder);
                }
            } else if (mapValues != null && Collection.class.isAssignableFrom(mapValues.getClass())) {
                // A parameter with multiple values.
                Collection<?> values = (Collection<?>) mapValues;
                for (Object value : values) {
                    addQueryStringPair(cleanKey, value, queryStringBuilder);
                }
            } else {
                addQueryStringPair(cleanKey, mapValues, queryStringBuilder);
            }
        }
        return queryStringBuilder.toString();
    }

    /**
     * Method to add an key / value pair to a query String.
     *
     * @param cleanKey Already escaped key
     * @param rawValue Raw value associated to the key
     * @param queryStringBuilder String Builder containing the current query string
     */
    private void addQueryStringPair(String cleanKey, Object rawValue, StringBuilder queryStringBuilder)
    {
        // Serialize null values as an empty string.
        String valueAsString = rawValue == null ? "" : String.valueOf(rawValue);
        String cleanValue = this.url(valueAsString);
        if (queryStringBuilder.length() != 0) {
            queryStringBuilder.append(AND);
        }
        queryStringBuilder.append(cleanKey).append(EQUALS).append(cleanValue);
    }

    /**
     * Escapes a CSS identifier.
     * <p>
     * See https://drafts.csswg.org/cssom/#serialize-an-identifier.
     * </p>
     * 
     * @param identifier the identifier to escape
     * @return the escaped identifier
     * @since 6.4.7
     * @since 7.1.4
     * @since 7.4M1
     */
    public String css(String identifier)
    {
        try {
            return new CSSIdentifierSerializer().serialize(identifier);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to escape CSS identifier. {}", e.getMessage());
            return null;
        }
    }

    /**
     * We override the implementation so that we sync it with the encoding strategy we use for generating URLs. Namely
     * we encode all characters and we encode space as {@code %20} and not as {@code +} in the query string.
     *
     * @param string the url to encode
     * @return the encoded URL
     * @since 8.3M1
     */
    @Override
    public String url(Object string)
    {
        // TODO: Introduce a xwiki-commons-url module and move this code in it so that we can share it with
        // platform's XWikiServletURLFactory and functional test TestUtils class.
        String encodedURL = null;
        if (string != null) {
            try {
                encodedURL = URLEncoder.encode(String.valueOf(string), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // Should not happen (UTF-8 is always available)
                throw new RuntimeException("Missing charset [UTF-8]", e);
            }
            // The previous call will convert " " into "+" (and "+" into "%2B") so we need to convert "+" into "%20"
            // It's ok since %20 is allowed in both the URL path and the query string (and anchor).
            encodedURL = encodedURL.replaceAll("\\+", "%20");
        }
        return encodedURL;
    }
}
