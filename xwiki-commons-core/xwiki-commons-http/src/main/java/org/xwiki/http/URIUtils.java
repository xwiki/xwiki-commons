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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.hc.core5.net.PercentCodec;
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
    private static final URLCodec FORMENCODED_CODEC = new URLCodec();

    // For retro compatibility reasons it's safer to use a decoder which convert + into white spaces
    private static final PercentCodec URI_CODEC = new PercentCodec();

    private URIUtils()
    {

    }

    /**
     * @param decoded the string to escape according to URI path specification
     * @return the UTF-8 escaped path element
     */
    public static String encodePathSegment(String decoded)
    {
        return URI_CODEC.encode(decoded);
    }

    /**
     * @param encoded the encoded string to parse
     * @return the decoded version of the string
     * @throws IOException when failing to parse the path
     */
    public static String decode(String encoded) throws IOException
    {
        try {
            return FORMENCODED_CODEC.decode(encoded);
        } catch (DecoderException e) {
            throw new IOException("Failed to decode string [" + encoded + "]", e);
        }
    }
}
