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
/*
 This class was copied from https://github.com/css4j/carte-util
 See https://github.com/css4j/xml-dtd/issues/7 for the reason why we copied it instead of having a dependency on it.

 Copyright (c) 1998-2022, Carlos Amengual.
 SPDX-License-Identifier: BSD-3-Clause
 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt
 */
package org.xwiki.xml.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

/**
 * User agent utility methods.
 *
 * @author Carlos Amengual
 * @version $Id$
 */
public class AgentUtil
{
    /**
     * Find the character encoding in a content-type string.
     *
     * @param conType         the content-type string.
     * @param afterCommaIndex the index of the first comma in <code>conType</code>,
     *                        plus one.
     * @return the character encoding, or null if could not be found.
     */
    public static String findCharset(String conType, int afterCommaIndex)
    {
        int idx = conType.indexOf("charset", afterCommaIndex);
        if (idx != -1) {
            idx += 7;
            int lenm1 = conType.length() - 1;
            char c = '\0';
            while (idx < lenm1 && (c = conType.charAt(idx)) == ' ') {
                idx++;
            }
            if (idx < lenm1 && c == '=') {
                conType = conType.substring(idx + 1).trim();
                lenm1 = conType.length() - 1;
                if (lenm1 > 1) {
                    char c0 = conType.charAt(0);
                    char c1 = conType.charAt(lenm1);
                    if ((c0 == '"' && c1 == '"') || (c0 == '\'' && c1 == '\'')) {
                        conType = conType.substring(1, lenm1);
                    }
                }
                return conType;
            }
        }
        return null;
    }

    public static Reader inputStreamToReader(InputStream is, String conType, String contentEncoding,
        Charset defaultCharset) throws IOException
    {
        if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
            is = new GZIPInputStream(is);
        }
        String charset = null;
        if (conType != null) {
            int sepidx = conType.indexOf(';');
            if (sepidx != -1) {
                conType = conType.substring(0, sepidx);
                charset = AgentUtil.findCharset(conType, sepidx + 1);
            }
        }
        InputStreamReader isre;
        if (charset == null) {
            isre = new InputStreamReader(is, defaultCharset);
        } else {
            isre = new InputStreamReader(is, charset);
        }
        // Handle UTF-8 BOM
        PushbackReader re = new PushbackReader(isre, 1);
        int iread = re.read();
        if (iread == -1 || iread != 0xefbbbf) {
            re.unread(iread);
        }
        return re;
    }
}

