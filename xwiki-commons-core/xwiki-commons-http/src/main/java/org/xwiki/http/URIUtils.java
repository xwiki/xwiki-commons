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
package org.xwiki.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.net.URLCodec;
import org.xwiki.stability.Unstable;

/**
 * Provide various helpers around URIs.
 * 
 * @version $Id$
 * @since 17.10.0RC1
 * @since 17.4.8
 * @since 16.10.15
 */
@Unstable
public final class URIUtils
{
    static final BitSet SAFE_PATH_SEGMENT = new BitSet(256);

    static final BitSet SAFE_PATH = new BitSet(256);

    static {
        for (int i = 'a'; i <= 'z'; i++) {
            SAFE_PATH_SEGMENT.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            SAFE_PATH_SEGMENT.set(i);
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            SAFE_PATH_SEGMENT.set(i);
        }
        SAFE_PATH_SEGMENT.set('-');
        SAFE_PATH_SEGMENT.set('.');
        SAFE_PATH_SEGMENT.set('_');
        SAFE_PATH_SEGMENT.set('~');

        SAFE_PATH.set('/');
        SAFE_PATH.or(SAFE_PATH_SEGMENT);
    }

    private static final URLCodec FORMENCODED_CODEC = new URLCodec();

    private URIUtils()
    {

    }

    /**
     * Escape and encode a string regarded as the path component of an URI with UTF8 charset.
     * <p>
     * The difference between this method and {@link #encodePathSegment(String)} is that the path separators are kept
     * unescaped.
     * 
     * @param decoded the string to escape according to URI path specification
     * @return the UTF-8 escaped path element
     */
    public static String encodePath(String decoded)
    {
        if (decoded == null || decoded.isEmpty()) {
            return decoded;
        }

        return StringUtils.newStringUtf8(URLCodec.encodeUrl(SAFE_PATH, decoded.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Escape and encode a string regarded as the path component of an URI with UTF8 charset.
     * <p>
     * The difference between this method and {@link #encodePath(String)} is that the path separators is escaped.
     * 
     * @param decoded the string to escape according to URI path specification
     * @return the UTF-8 escaped path element
     */
    public static String encodePathSegment(String decoded)
    {
        return StringUtils
            .newStringUtf8(URLCodec.encodeUrl(SAFE_PATH_SEGMENT, decoded.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Decodes a URL safe string into its original form using UTF8 charset.
     * 
     * @param encoded the encoded string to parse
     * @return the decoded version of the string
     * @throws IOException when failing to parse the path
     */
    public static String decode(String encoded) throws IOException
    {
        try {
            return FORMENCODED_CODEC.decode(encoded, "utf8");
        } catch (DecoderException e) {
            throw new IOException("Failed to decode string [" + encoded + "]", e);
        }
    }
}
